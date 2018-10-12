package models
import types.TokenId
import utils.SecureIdentifier

case class AdminAuth(tokenId: TokenId, adminAuthId: String, createdAt: Long)
