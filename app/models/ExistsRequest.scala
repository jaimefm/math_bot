package models
import play.api.libs.json.Json

object ExistsRequest {
  implicit val existsRequestFormat = Json.format[ExistsRequest]
}

final case class ExistsRequest(username: String)
