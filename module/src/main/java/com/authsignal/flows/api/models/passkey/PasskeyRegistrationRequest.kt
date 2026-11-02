package com.authsignal.flows.api.models.passkey

import com.authsignal.flows.models.PasskeyRegistrationCredential
import kotlinx.serialization.Serializable

@Serializable
data class PasskeyRegistrationRequest(
  val registrationCredential: PasskeyRegistrationCredential,
)
