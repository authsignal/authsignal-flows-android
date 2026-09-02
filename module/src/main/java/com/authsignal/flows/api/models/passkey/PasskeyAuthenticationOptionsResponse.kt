package com.authsignal.flows.api.models.passkey

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyAuthenticationOptionsResponse(
  val authenticationOptions: PasskeyAuthenticationOptions,
)

@Serializable
data class PasskeyAuthenticationOptions(
  val challenge: String,
  val rpId: String,
  val allowCredentials: List<PasskeyAuthenticationOptionsCredential>,
  val userVerification: String = "required",
)

@Serializable
data class PasskeyAuthenticationOptionsCredential(
  val id: String,
  val type: String,
)
