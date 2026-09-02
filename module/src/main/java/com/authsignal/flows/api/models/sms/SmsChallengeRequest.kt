package com.authsignal.flows.api.models.sms

import kotlinx.serialization.Serializable

@Serializable
data class SmsChallengeRequest(
  val phoneNumber: String? = null,
)
