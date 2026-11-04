package com.authsignal.flows.api.models.session

import kotlinx.serialization.Serializable

@Serializable
data class SessionTokens(
  val accessToken: String,
  val refreshToken: String,
  val accessTokenExpiresInSeconds: Long,
)
