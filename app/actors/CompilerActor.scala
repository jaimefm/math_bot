package actors

import actors.LevelGenerationActor.{ GetGridMap, GetStep }
import actors.StatsActor.{ StatsDoneUpdating, UpdateStats }
import actors.messages._
import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import compiler.operations.NoOperation
import compiler.processor.{ Frame, Processor, Register }
import compiler.{ Compiler, GridAndProgram, Point }
import configuration.CompilerConfiguration
import controllers.MathBotCompiler
import javax.inject.Inject
import loggers.MathBotLogger
import daos.PlayerTokenDAO
import models.{ FuncToken, GridMap, Problem, Stats }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import types.TokenId

import scala.concurrent.duration._

class CompilerActor @Inject()(out: ActorRef, tokenId: TokenId)(
    playerTokenDAO: PlayerTokenDAO,
    statsActor: ActorRef,
    levelActor: ActorRef,
    logger: MathBotLogger,
    config: CompilerConfiguration
) extends Actor {

  import MathBotCompiler._

  case class ProgramState(stream: Stream[Frame],
                          iterator: Iterator[Frame],
                          grid: GridMap,
                          program: GridAndProgram,
                          clientFrames: List[ClientFrame] = List.empty[ClientFrame],
                          stepCount: Int = 0,
                          leftover: Option[Frame] = None,
                          exitOnSuccess: Boolean = false) {
    def addSteps(steps: Int): ProgramState = this.copy(stepCount = this.stepCount + steps)
    def withLeftover(l: Frame): ProgramState = this.copy(leftover = Some(l))
  }
  implicit val timeout: Timeout = 5000.minutes

  private val className = s"CompilerActor(${context.self.path.toSerializationFormat})"

  private def createFrames(leadingFrames: List[Frame]) = {

    val frames = leadingFrames.map(_.withMinimalGrid()).map(ClientFrame(_))

    frames
  }

  private def checkForSuccess(programState: ProgramState, frame: Frame): Boolean =
    programState.grid.success(frame, programState.program.problem)

  private def createLastFrame(programState: ProgramState, frame: Frame) = {
    // Check to see if the last frame generated by the processor matches the problem check
    for {
      // Update stats, and get the updated stats
      stats <- (statsActor ? UpdateStats(success = checkForSuccess(programState, frame), tokenId = tokenId))
        .mapTo[Either[Stats, ActorFailed]]
        .map(_.left.get)
      // Gather the prepared step data
      stepData <- (levelActor ? GetStep(stats.level, stats.step, Some(tokenId)))
        .mapTo[Either[PreparedStepData, ActorFailed]]
        .map(_.left.get)
    } yield {
      if (checkForSuccess(programState, frame)) {
        List(MathBotCompiler.ClientFrame.success(frame, stats, stepData))
      } else {
        List(MathBotCompiler.ClientFrame.failure(frame, stats, stepData))
      }
    }
  }

  private def createLastFromFromNothing() = {
    for {
      // Update stats, and get the updated stats
      stats <- (statsActor ? UpdateStats(success = false, tokenId = tokenId))
        .mapTo[Either[Stats, ActorFailed]]
        .map(_.left.get)
      // Gather the prepared step data
      stepData <- (levelActor ? GetStep(stats.level, stats.step, Some(tokenId)))
        .mapTo[Either[PreparedStepData, ActorFailed]]
        .map(_.left.get)
    } yield {
      MathBotCompiler.ClientFrame(ClientRobotState(Point(0, 0), "0", List.empty[String]),
                                  "failure",
                                  Some(stats),
                                  Some(stepData))
    }
  }

  private def clientFramePrint(frame: ClientFrame) = {
    s"location: ${frame.robotState.location}\n" +
    s"register: ${frame.robotState.holding.mkString(", ")}\n" +
    "grid: \n" +
    frame.robotState.grid
      .map(cel => cel.cells.map(c => s"  (${c.location.x}, ${c.location.y} -> ${c.items.mkString(", ")}"))
      .map(_.mkString("\n"))
      .getOrElse(" No Grid")
  }

  private def sendFrames(programState: ProgramState, clientFrames: List[ClientFrame]): Unit = {
    out ! CompilerOutput(clientFrames, programState.grid.problem)
  }

  override def receive: Receive = createCompile()

  def createCompile(): Receive = {
    case CompilerExecute(steps, problem) =>
      self ! CompilerCreate(steps, problem)

    case CompilerCreate(steps, problem) =>
      logger.LogInfo(className, "Creating new compiler.")

      for {
        tokenList <- playerTokenDAO.getToken(tokenId)
        grid <- (levelActor ? GetGridMap(tokenList)).mapTo[GridMap]
      } yield {
        for {
          token <- tokenList
          main = token.lambdas.head.main
          funcs = token.lambdas.head.activeFuncs ++ token.lambdas.head.inactiveActives.getOrElse(List.empty[FuncToken])
          commands = token.lambdas.head.cmds
          program <- Compiler.compile(main, funcs, commands, grid, problem)
        } yield {
          val processor = Processor(program)
          val stream = processor.execute()
          context.become(
            compileContinue(
              ProgramState(stream = stream,
                           iterator = stream.iterator,
                           grid = grid,
                           program = program,
                           exitOnSuccess = grid.evalEachFrame)
            )
          )
          self ! CompilerContinue(steps)
        }
      }
    case CompilerContinue(_) =>
      // We see this in production and don't know why the client is still sending continues after the actor
      // has signaled end of program.  So we try to create a fake last frame.
      logger.LogDebug("CompilerActor", "Continue received during create state.")
      for {
        lastFrame <- createLastFromFromNothing()
      } yield out ! CompilerOutput(List(lastFrame), Problem(""))
  }

  def compileContinue(currentCompiler: ProgramState): Receive = {

    case CompilerExecute(steps, _) =>
      self ! CompilerContinue(steps)

    case CompilerContinue(steps) =>
      logger.LogInfo(className,
                     s"Stepping compiler for $steps steps with actor ${context.self.path.toSerializationFormat}")
      // filter out non-robot frames (eg function calls and program start)
      val maxStepsReached = config.maxProgramSteps < currentCompiler.stepCount + steps
      val takeSteps =
        if (maxStepsReached)
          config.maxProgramSteps - currentCompiler.stepCount
        else
          steps + (if (currentCompiler.leftover.isEmpty) 1 else 0) // Add an additional step for a leftover
      val robotFrames = currentCompiler.iterator
        .filter(f => {
          f.robotLocation.isDefined
        })
        .take(takeSteps)
        .toList

      // The reason for a leftover frame.  Since the processor can run in a forever loop, we are left to
      // deciding when the last requested frame (say the 4th frame in a request for 4 steps) is the end
      // of program or not.  By asking for an additional frame (a 5th in the example) we can determine if the
      // program is still running.  If we get less than the requested amount, we know the program exited.
      // However, we have to keep that extra frame around fro when the client requests another 4 frames.
      //
      // If you are curious about this topic in general, google "the halting problem" :-)

      val frames = currentCompiler.leftover match {
        case Some(leftover) => leftover +: robotFrames
        case None => robotFrames
      }

      val (executeSomeFrames, programCompleted) = if (currentCompiler.exitOnSuccess) {
        // Generate a temporary index for the program frames and search for a success frame.
        // Truncate the frames to the first successful frame
        val checkedFrames = frames.zipWithIndex
          .map(frame => (frame._1, checkForSuccess(currentCompiler, frame._1), frame._2))

        checkedFrames.find(f => f._2).map(_._3) match {
          case Some(index) => (
            checkedFrames.takeWhile(_._3 <= index).map(_._1), // Just the frames up until success
            true // program completion
            )
          case _ => (
            frames, // All the frames
            robotFrames.length < takeSteps || maxStepsReached // If the program stopped early or hit max frame count
          )
        }
      } else {
        (
          frames, // All the frames
          robotFrames.length < takeSteps || maxStepsReached // If the program stopped early or hit max frame count
        )
      }

      executeSomeFrames match {
        case List(frame) =>
          // When the compiler generates a single frame, assume its the last frame.
          for {
            lastFrame <- createLastFrame(currentCompiler, frame)
          } yield sendFrames(currentCompiler, lastFrame)
          context.become(createCompile())
        case leadingFrames :+ leftover if !programCompleted =>
          // More than one frame, send the leading frames and save the last frame for the next request
          sendFrames(currentCompiler, createFrames(leadingFrames))
          context.become(
            compileContinue(currentCompiler.withLeftover(leftover).addSteps(leadingFrames.length))
          )
        case leadingFrames :+ last if programCompleted =>
          // When short the requested frames, assume the program has finished and compute the last frame too **
          for {
            lastFrame <- createLastFrame(currentCompiler, last)
          } yield sendFrames(currentCompiler, createFrames(leadingFrames) ++ lastFrame)
          context.become(
            createCompile()
          )
          context.become(createCompile())
        case Nil =>
          // It's an empty program, or one that consists of only empty functions
          for {
            lastFrame <- createLastFrame(currentCompiler,
                                         Frame(NoOperation, Register(), currentCompiler.program.grid, None, None))
          } yield sendFrames(currentCompiler, lastFrame)
          context.become(createCompile())
      }

    case _: CompilerHalt =>
      logger.LogInfo(className, "Compiler halted")
      context.become(createCompile())
      out ! CompilerHalted()

    case Left(_: StatsDoneUpdating) =>
      logger.LogInfo(className, s"Stats updated successfully. token_id:$tokenId")

    case Right(invalidJson: ActorFailed) =>
      logger.LogFailure(className, invalidJson.msg)
      self ! invalidJson.msg

    case _ => out ! ActorFailed("Unknown command submitted to compiler")
  }

  override def postStop() : Unit = {
    logger.LogInfo(className, "Actor Stopped")
  }
}

object CompilerActor {
  def props(out: ActorRef,
            tokenId: String,
            playerTokenDAO: PlayerTokenDAO,
            statsActor: ActorRef,
            levelActor: ActorRef,
            logger: MathBotLogger,
            config: CompilerConfiguration) =
    Props(new CompilerActor(out, tokenId)(playerTokenDAO, statsActor, levelActor, logger, config))
}
