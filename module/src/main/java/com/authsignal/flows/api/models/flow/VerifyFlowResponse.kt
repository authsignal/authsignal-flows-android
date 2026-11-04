package com.authsignal.flows.api.models.flow

import com.authsignal.flows.api.models.session.SessionTokens
import kotlinx.serialization.Serializable

@Serializable
data class VerifyFlowResponse(
  val session: SessionTokens? = null,
)
