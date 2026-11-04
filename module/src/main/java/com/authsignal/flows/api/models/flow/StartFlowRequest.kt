package com.authsignal.flows.api.models.flow

import kotlinx.serialization.Serializable

@Serializable
data class StartFlowRequest(
  val actionCode: String,
  val clientId: String,
  val codeChallenge: String,
)
