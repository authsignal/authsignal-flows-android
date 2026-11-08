package com.authsignal.flows.api.models.push

import kotlinx.serialization.Serializable

@Serializable
data class RespondToPushChallengeRequest(
  val publicKey: String,
  val challengeId: String,
  val signature: String,
  val approved: Boolean,
  val verificationCode: String? = null,
)

@Serializable
internal data class RespondToPushChallengeResponse(
  val isVerified: Boolean,
)
