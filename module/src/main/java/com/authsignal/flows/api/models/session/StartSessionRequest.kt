package com.authsignal.flows.api.models.session

import kotlinx.serialization.Serializable

@Serializable
data class StartSessionRequest(
  val actionCode: String,
  val clientId: String,
  val codeChallenge: String,
)
