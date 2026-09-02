package com.authsignal.flows.api.models.email

import kotlinx.serialization.Serializable

@Serializable
data class EmailVerifyRequest(
  val verificationCode: String,
)
