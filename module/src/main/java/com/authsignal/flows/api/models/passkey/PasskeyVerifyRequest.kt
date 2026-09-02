package com.authsignal.flows.api.models.passkey

import com.authsignal.flows.models.PasskeyAuthenticationCredential
import kotlinx.serialization.Serializable

@Serializable
data class PasskeyVerifyRequest(
  val authenticationResponse: PasskeyAuthenticationCredential,
)
