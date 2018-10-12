package models

case class Auth0LegacyUser(
                          user_id:  String,
                          email : String,
                          name : String,
                          nickname: String,
                          created_at : String,
                          last_login: String,
                          logins_count: Int,
                          migrated : Option[Boolean] = None
                          )