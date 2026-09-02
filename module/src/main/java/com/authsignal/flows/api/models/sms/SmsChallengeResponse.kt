package com.authsignal.flows.api.models.sms

import kotlinx.serialization.Serializable

@Serializable
data class SmsChallengeResponse(
  val retryAfterSeconds: Int? = null,
)
