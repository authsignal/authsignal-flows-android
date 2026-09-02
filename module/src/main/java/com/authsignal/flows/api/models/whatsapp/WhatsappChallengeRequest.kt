package com.authsignal.flows.api.models.whatsapp

import kotlinx.serialization.Serializable

@Serializable
data class WhatsappChallengeRequest(
  val phoneNumber: String? = null,
)
