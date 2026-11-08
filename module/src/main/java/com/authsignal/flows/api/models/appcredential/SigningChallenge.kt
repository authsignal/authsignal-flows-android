package com.authsignal.flows.api.models.appcredential

import kotlinx.serialization.Serializable

@Serializable
internal data class SigningChallengeRequest(
  val publicKey: String,
)

@Serializable
internal data class SigningChallengeResponse(
  val signingChallengeId: String,
  val message: String,
)

@Serializable
internal data class SignedRequest(
  val publicKey: String,
  val signingChallengeId: String,
  val signature: String,
)
