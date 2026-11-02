package com.authsignal.flows.api.models.passkey

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyRegistrationOptionsRequest(
  val username: String? = null,
  val displayName: String? = null,
)
