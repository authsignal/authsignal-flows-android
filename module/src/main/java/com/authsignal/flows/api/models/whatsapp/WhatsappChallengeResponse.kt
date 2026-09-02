package com.authsignal.flows.api.models.whatsapp

import kotlinx.serialization.Serializable

@Serializable
data class WhatsappChallengeResponse(
  val retryAfterSeconds: Int? = null,
)
