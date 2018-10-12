package models

case class JwtToken(iss: String, sub: String, email: String, name: String, picture: Option[String] = None) {
  def getIssuerShortName: String = {
    iss match {
      case "https://accounts.google.com" => "google-oauth2"
      case "https://github.com" => "github" // TODO: Find the value that auth0 uses.
      case "https://mathbot.com" => "mathbot"
      case "https://auth0.mathbot.com" => "auth0"
    }
  }
  def playerTokenId = s"${getIssuerShortName}|$sub"
}
