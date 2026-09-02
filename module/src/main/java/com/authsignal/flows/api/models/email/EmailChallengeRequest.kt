package com.authsignal.flows.api.models.email

import kotlinx.serialization.Serializable

@Serializable
data class EmailChallengeRequest(
  val email: String? = null,
)
