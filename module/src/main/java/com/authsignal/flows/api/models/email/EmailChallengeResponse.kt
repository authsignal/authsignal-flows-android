package com.authsignal.flows.api.models.email

import kotlinx.serialization.Serializable

@Serializable
data class EmailChallengeResponse(
  val retryAfterSeconds: Int? = null,
)
