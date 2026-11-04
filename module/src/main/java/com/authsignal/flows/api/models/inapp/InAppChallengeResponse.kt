package com.authsignal.flows.api.models.inapp

import kotlinx.serialization.Serializable

@Serializable
data class InAppChallengeResponse(
  val challenge: String,
)
