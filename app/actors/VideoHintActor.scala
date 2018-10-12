package actors

import java.time.Instant

import actors.messages.{ActorFailed, RawStepData}
import akka.actor.{Actor, ActorRef, Props}
import com.google.inject.Inject
import models._
import daos.{PlayerTokenDAO, VideoHintDAO}
import play.api.Environment
import play.api.libs.json.{Json, OFormat}
import play.api.libs.ws.WSClient
import types.{LevelName, StepName, TokenId, URL}

import scala.concurrent.ExecutionContext.Implicits.global

object VideoHintActor {
  final case class GetVideo(tokenId: TokenId)

  final case class GetPlayerToken(tokenId: TokenId)

  final case class GetVideoHint(playerToken: PlayerToken, stats: Stats)

  final case class UpdateExistingVideo(playerToken: PlayerToken,
                                       videoHint: HintsTaken,
                                       hintTaken: HintTaken,
                                       stats: Stats,
                                       videoIds: List[String])

  final case class InsertNewVideo(playerToken: PlayerToken, videoHint: HintsTaken, stats: Stats, videoIds: List[String])

  final case class InsertNewVideoRecord(playerToken: PlayerToken, stats: Stats, videoIds: List[String])

  final case class GetHintsTaken(tokenId: TokenId)

  final case class HintPrepared(tokenId: TokenId, videoUrl: URL, remainingTime: RemainingTime)

  final case class RemainingTimeList(tokenId: TokenId, list: List[RemainingTime])

  final case class RemainingTime(stars: Int, level: LevelName, step: StepName, remainingTime: Long)

  final case class GetRemainingTime(tokenId: TokenId, level: LevelName, step: StepName)

  final case class UpdateStars(remainingTimeList: RemainingTimeList)

  final case class ResetStars(tokenId: TokenId, hintsTaken: HintsTaken, level: LevelName, step: StepName)

  final case class NoVideos(tokenId: TokenId, level: LevelName, step: StepName)

  def embedURL(videoId: String): URL = s"https://www.youtube.com/embed/$videoId?rel=0"

  def generateTimestamp: Long = Instant.now.getEpochSecond

  def calculateStars(stars: Int, videoCount: Int) = {
    val starRatio = Math.ceil(3.toDouble / videoCount).toInt
    Math.max(0, stars - starRatio)
  }

  private val starExpiration = 3600 // bump up to 3600

  def calculateRemainingTime(timeStamp: Long, expLength: Int): Long = {
    val expiredTime = timeStamp + expLength
    val timeNow = generateTimestamp
    val remainingTime = expiredTime - timeNow

    if (remainingTime <= 0) {
      0
    } else {
      remainingTime / 60
    }
  }

  def props(out: ActorRef,
            playerTokenDAO: PlayerTokenDAO,
            videoHintDAO: VideoHintDAO,
            ws: WSClient,
            environment: Environment) =
    Props(new VideoHintActor(out, playerTokenDAO, videoHintDAO, ws, environment))
}

class VideoHintActor @Inject()(out: ActorRef,
                               playerTokenDAO: PlayerTokenDAO,
                               videoHintDAO: VideoHintDAO,
                               ws: WSClient,
                               environment: Environment)
    extends Actor {
  import VideoHintActor._

  val levelGenerator: LevelGenerator = new LevelGenerator(environment)

  /*
   * For future implementation with youtube api...
   * */
  private val CHANNEL_ID: String = "UCCRrOAkZKCMB3wEIGEmdMEQ"
  private val KEY: String = sys.env.getOrElse("MB_YOUTUBE_KEY", "none")
  private val YOUTUBE_URL: URL = "https://www.googleapis.com/youtube/v3/"

  override def receive: Receive = {
    /*
     * GetVideo - entry point for getting a video
     * */
    case GetVideo(tokenId) =>
      self ! GetPlayerToken(tokenId)
    /*
     * GetPlayerToken - gets player token from db
     * */
    case GetPlayerToken(tokenId) =>
      for {
        playerTokenOpt <- playerTokenDAO.getToken(tokenId)
      } yield
        playerTokenOpt match {
          case Some(playerToken) =>
            for {
              stats <- playerToken.stats
            } yield self ! GetVideoHint(playerToken, stats)
          case None => out ! ActorFailed(s"Unable to locate tokenId $tokenId")
        }
    /*
     * # GetVideoHint - gets video hint from db and controls traffic
     * If video hint found in db - videos already watched for game
     * ---- If level/step found - video watched for level/step - continue to UpdateExistingVideo
     * ---- If level/step not found - video never watched for level/step - continue to InsertNewVideo
     * If video hint not found - no videos ever watched for game - continue to InsertNewVideoRecord
     * */
    case GetVideoHint(playerToken, stats) =>
      for {
        videoHintOpt <- videoHintDAO.getHints(playerToken.token_id)
      } yield
        levelGenerator.getRawStepData(stats.level, stats.step) match {
          case Some(rawStepData) =>
            rawStepData.videoHints match {
              case videoIds if videoIds.nonEmpty =>
                videoHintOpt match {
                  // If videoHint field exists in VideoHintControls table
                  case Some(hintsTaken) =>
                    // Test if level/step exist in videos watched
                    hintsTaken.videosWatched.find(v => v.level == stats.level && v.step == stats.step) match {
                      case Some(hintTaken) =>
                        self ! UpdateExistingVideo(playerToken, hintsTaken, hintTaken, stats, videoIds)
                      case None => self ! InsertNewVideo(playerToken, hintsTaken, stats, videoIds)
                    }
                  // if not in database insert new record into database
                  case None => self ! InsertNewVideoRecord(playerToken, stats, videoIds)
                }
              case _ => out ! NoVideos(playerToken.token_id, stats.level, stats.step)
            }
          case None => out ! NoVideos(playerToken.token_id, stats.level, stats.step)
        }
    /*
     * UpdateExistingVideo - if video has already been viewed for this level/step
     * */
    case UpdateExistingVideo(playerToken, hintsTaken, hintTaken, stats, videoIds) =>
      val level = stats.level
      val step = stats.step
      // Increment hint count
      val hintCount =
        if (hintTaken.hintCount < videoIds.length) hintTaken.hintCount + 1 else videoIds.length
      // Create new timestamp
      val timeStamp = generateTimestamp
      // update stars
      val stars = calculateStars(hintTaken.stars, videoIds.length)
      // Handle updating hintsTaken
      val updatedHintsTaken = hintsTaken.copy(videosWatched = hintsTaken.videosWatched.map {
        case ht if ht.level == level && ht.step == step =>
          ht.copy(timeStamp = timeStamp, hintCount = hintCount, stars = stars)
        case ht => ht
      })
      videoHintDAO.update(updatedHintsTaken)
      // respond to client
      val videoUrl = embedURL(videoIds(hintCount - 1))
      out ! HintPrepared(playerToken.token_id,
                         videoUrl,
                         RemainingTime(stars, level, step, calculateRemainingTime(timeStamp, starExpiration)))
    /*
     * InsertNewVideo - if video has never been viewed for this level/step
     * */
    case InsertNewVideo(playerToken, hintsTaken, stats, videoIds) =>
      val level = stats.level
      val step = stats.step
      // Initialize hint count
      val hintCount = 1
      // Create new timestamp
      val timestamp = generateTimestamp
      // stars
      val stars = calculateStars(3, videoIds.length)
      // Handle creating hintTaken
      val hintTaken = HintTaken(level, step, timestamp, hintCount, stars)
      // Handle updating hintsTaken
      val updatedHintsTaken = hintsTaken.copy(videosWatched = hintTaken :: hintsTaken.videosWatched)
      videoHintDAO.update(updatedHintsTaken)
      // respond to client
      val videoUrl = embedURL(videoIds(hintCount - 1))
      out ! HintPrepared(playerToken.token_id,
                         videoUrl,
                         RemainingTime(stars, level, step, calculateRemainingTime(timestamp, starExpiration)))
    /*
     * InsertNewVideoRecord - if no videos have ever been watched during game
     * */
    case InsertNewVideoRecord(playerToken, stats, videoIds) =>
      val level = stats.level
      val step = stats.step
      // Initialize hint count
      val hintCount = 1
      // Generate timestamp
      val timeStamp = generateTimestamp
      // stars
      val stars = calculateStars(3, videoIds.length)
      // Handle creating hintTaken
      val hintTaken = HintTaken(level, step, timeStamp, hintCount, stars)
      // Handle creating videoHint record
      val videoHint = HintsTaken(playerToken.token_id, List(hintTaken))
      // Handle inserting video hint into db
      videoHintDAO.insert(videoHint)
      // respond to client
      val videoUrl = embedURL(videoIds(hintCount - 1))
      out ! HintPrepared(playerToken.token_id,
                         videoUrl,
                         RemainingTime(stars, level, step, calculateRemainingTime(timeStamp, starExpiration)))
    /*
     * GetHintsTaken - returns a list of hints taken with a remaining time in minutes
     * also filters out expired videos.
     * */
    case GetHintsTaken(tokenId) =>
      videoHintDAO.getHints(tokenId) map {
        case Some(hintsTaken) =>
          val filterExpired =
            hintsTaken.videosWatched.filterNot(ht => calculateRemainingTime(ht.timeStamp, starExpiration) == 0)
          videoHintDAO.update(hintsTaken.copy(videosWatched = filterExpired))
          out ! RemainingTimeList(tokenId, filterExpired.map { ht =>
            RemainingTime(ht.stars, ht.level, ht.step, calculateRemainingTime(ht.timeStamp, starExpiration))
          })
        case None => out ! RemainingTimeList(tokenId, List.empty[RemainingTime])
      }
    case actorFailed: ActorFailed => out ! actorFailed
  }
}
