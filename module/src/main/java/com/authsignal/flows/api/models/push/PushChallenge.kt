package com.authsignal.flows.api.models.push

import kotlinx.serialization.Serializable

@Serializable
data class PushChallenge(
  val challengeId: String,
  val actionCode: String? = null,
  val deviceId: String? = null,
  val userAgent: String? = null,
  val ipAddress: String? = null,
  val expiresAt: Long? = null,
)

@Serializable
internal data class PendingPushChallengeResponse(
  val challengeId: String? = null,
  val actionCode: String? = null,
  val deviceId: String? = null,
  val userAgent: String? = null,
  val ipAddress: String? = null,
  val expiresAt: Long? = null,
)
