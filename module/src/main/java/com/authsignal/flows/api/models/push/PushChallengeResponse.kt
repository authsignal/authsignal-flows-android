package com.authsignal.flows.api.models.push

import kotlinx.serialization.Serializable

@Serializable
data class PushChallengeResponse(
  val verificationCode: String? = null,
)
