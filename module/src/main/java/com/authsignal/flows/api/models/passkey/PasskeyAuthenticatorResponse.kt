package com.authsignal.flows.api.models.passkey

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyAuthenticatorResponse(
  val credentialId: String,
  val verifiedAt: String,
)
