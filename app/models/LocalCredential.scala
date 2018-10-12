package models

import utils.SecureIdentifier

case class LocalCredential(accountId: SecureIdentifier,
                           legacyAuthId: Option[String],
                           username: String,
                           name: String,
                           picture: Option[String] = None,
                           salt: SecureIdentifier,
                           hash: Array[Byte],
                           iterations: Int,
                           blockSize: Int,
                           hashSize: Int,
                           admin: Option[Boolean] = None,
                           recoveryId: Option[SecureIdentifier] = None)
