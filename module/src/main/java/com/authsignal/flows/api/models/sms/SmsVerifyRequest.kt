package com.authsignal.flows.api.models.sms

import kotlinx.serialization.Serializable

@Serializable
data class SmsVerifyRequest(
  val verificationCode: String,
)
