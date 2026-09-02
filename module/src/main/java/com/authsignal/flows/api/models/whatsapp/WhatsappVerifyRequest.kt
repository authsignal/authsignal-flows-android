package com.authsignal.flows.api.models.whatsapp

import kotlinx.serialization.Serializable

@Serializable
data class WhatsappVerifyRequest(
  val verificationCode: String,
)
