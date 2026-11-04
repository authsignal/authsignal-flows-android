package com.authsignal.flows.security

import com.authsignal.flows.api.models.session.SessionTokens
import kotlinx.serialization.Serializable

@Serializable
internal data class StoredSession(
  val accessToken: String,
  val refreshToken: String,
  val accessTokenExpiresAt: Long,
) {
  companion object {
    fun from(tokens: SessionTokens): StoredSession {
      return StoredSession(
        accessToken = tokens.accessToken,
        refreshToken = tokens.refreshToken,
        accessTokenExpiresAt = System.currentTimeMillis() + tokens.accessTokenExpiresInSeconds * 1000,
      )
    }
  }
}
