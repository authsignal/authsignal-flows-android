package com.authsignal.flows.api.models.flow

import kotlinx.serialization.Serializable

@Serializable
data class VerifyFlowRequest(
  val codeVerifier: String,
)
