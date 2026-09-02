package com.authsignal.flows.models

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyRegistrationCredential(
  val id: String,
  val rawId: String,
  val type: String,
  val authenticatorAttachment: String? = null,
  val response: PasskeyRegistrationCredentialResponse,
)

@Serializable
data class PasskeyRegistrationCredentialResponse(
  val attestationObject: String? = null,
  val clientDataJSON: String,
)
