package actors.messages

import actors.messages.PreparedStepData.InitialRobotState
import models.{ToolList, _}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import types.{LevelName, StepName, TokenId}

case class PreparedStepData(
    tokenId: TokenId,
    level: LevelName,
    step: StepName,
    gridMap: List[List[GridPart]],
    description: String,
    mainMax: Int,
    initialRobotState: InitialRobotState,
    stagedEnabled: Boolean,
    activeEnabled: Boolean,
    lambdas: ResponseLambdas,
    toolList: ToolList,
    specialParameters: List[String],
    problem: Problem,
    prevStep: String,
    nextStep: String,
    initFocus: List[String],
    freeHint: Option[String],
    stepControl: StepControl
)

object PreparedStepData {
  import models.Problem._
  import actors.VideoHintActor.embedURL

  def apply(playerToken: PlayerToken, rawStepData: RawStepData): PreparedStepData = {
    new PreparedStepData(
      tokenId = playerToken.token_id,
      level = rawStepData.level,
      step = rawStepData.step,
      gridMap = buildGrid(rawStepData.gridMap),
      description = makeDescription(rawStepData),
      mainMax = rawStepData.mainMax,
      initialRobotState = setInitialRobot(rawStepData),
      stagedEnabled = rawStepData.stagedEnabled,
      activeEnabled = rawStepData.activeEnabled,
      lambdas = ResponseLambdas(playerToken.lambdas.getOrElse(Lambdas())),
      toolList = ToolList(),
      specialParameters = rawStepData.specialParameters,
      problem = problemGen(rawStepData),
      prevStep = rawStepData.prevStep,
      nextStep = rawStepData.nextStep,
      initFocus = createInitFocus(rawStepData.initFocus),
      freeHint = freeHintUrl(rawStepData.freeHint),
      stepControl = new StepControl(rawStepData, playerToken.lambdas.getOrElse(Lambdas()))
    )
  }

  case class InitialRobotState(location: Map[String, Int], orientation: String, holding: List[String])

  import daos.DefaultCommands._

  def freeHintUrl(idOpt: Option[String]): Option[String] = idOpt match {
    case Some(id) => Some(embedURL(id))
    case None => None
  }

  def findRobotCoords(grid: List[String], coords: Map[String, Int] = Map("x" -> 0, "y" -> 0)): Map[String, Int] =
    grid match {
      case r :: _ if r contains "(R)" => Map("x" -> coords("x"), "y" -> prepRow(r).indexOf("(R)"))
      case _ :: rest => findRobotCoords(rest, Map("x" -> (coords("x") + 1), "y" -> 0))
    }

  def setInitialRobot(rawStepData: RawStepData): InitialRobotState =
    InitialRobotState(location = findRobotCoords(rawStepData.gridMap),
                      orientation = rawStepData.robotOrientation.toString,
                      holding = List.empty[String])

  def createInitFocus(initFocus: List[String]): List[String] = initFocus.map { a =>
    cmds.find(_.name.getOrElse("") == a) match {
      case Some(token) => token.created_id
      case None =>
        a match {
          case id if id == "open-staged" => "open-staged"
          case id => id
        }
    }
  }

  def problemGen(rawStepData: RawStepData): Problem = makeProblem(rawStepData.problem)

  def parseCamelCase(camelStr: String): String = camelStr.toList match {
    case Nil => camelStr.toString
    case l :: rest =>
      if (l.isUpper) " " + l.toString + parseCamelCase(rest.mkString(""))
      else l.toString + parseCamelCase(rest.mkString(""))
  }

  def makeDescription(rawStepData: RawStepData): String = {
    "<p>" +
    s"${rawStepData.description
      .split("!!")
      .map {
        case a if a == "[P]" => problemGen(rawStepData).problem.getOrElse("0")
        case a if a == "[S]" => parseCamelCase(rawStepData.step)
        case a if a == "[L]" => parseCamelCase(rawStepData.level)
        case a if a contains "[img]" =>
          a.replace("[img]", "<img ") + " />"
        case a => a
      }
      .mkString(" ")
      .split("\n")
      .mkString(" <br /> ")}" +
    s"</p>"
  }

  private def prepRow(row: String): List[String] = row.split(" ").toList.filterNot(_ == "")

  def buildGrid(gridMap: List[String]): List[List[GridPart]] = {
    gridMap map { row =>
      prepRow(row) map { key =>
        GridPart.apply(key)
      }
    }
  }

  implicit val initialRobotStateWrites: Writes[InitialRobotState] = (
    (JsPath \ "location").write[Map[String, Int]] and
    (JsPath \ "orientation").write[String] and
    (JsPath \ "holding").write[List[String]]
  )(unlift(InitialRobotState.unapply))

  val stepDataReads: Reads[PreparedStepData] = (
    (JsPath \ "playerToken").read[PlayerToken] and
    (JsPath \ "rawStepData").read[RawStepData]
  )(PreparedStepData(_, _))

  val stepDataWrites: Writes[PreparedStepData] = (
    (JsPath \ "tokenId").write[String] and
    (JsPath \ "level").write[String] and
    (JsPath \ "step").write[String] and
    (JsPath \ "gridMap").write[List[List[GridPart]]] and
    (JsPath \ "description").write[String] and
    (JsPath \ "mainMax").write[Int] and
    (JsPath \ "initialRobotState").write[InitialRobotState] and
    (JsPath \ "stagedEnabled").write[Boolean] and
    (JsPath \ "activeEnabled").write[Boolean] and
    (JsPath \ "lambdas").write[ResponseLambdas] and
    (JsPath \ "toolList").write[ToolList] and
    (JsPath \ "specialParameters").write[List[String]] and
    (JsPath \ "problem").write[Problem] and
    (JsPath \ "prevStep").write[String] and
    (JsPath \ "nextStep").write[String] and
    (JsPath \ "initFocus").write[List[String]] and
    (JsPath \ "freeHint").writeNullable[String] and
    OWrites[StepControl](_ => Json.obj())
  )(unlift(PreparedStepData.unapply))

  implicit val stepDataFormat: Format[PreparedStepData] =
    Format(stepDataReads, stepDataWrites)
}
