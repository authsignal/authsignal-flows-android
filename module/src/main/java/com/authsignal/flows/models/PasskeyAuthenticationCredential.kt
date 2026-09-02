package com.authsignal.flows.models

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyAuthenticationCredential(
  val id: String,
  val rawId: String,
  val type: String,
  val authenticatorAttachment: String? = null,
  val response: PasskeyAuthenticationCredentialResponse,
)

@Serializable
data class PasskeyAuthenticationCredentialResponse(
  val authenticatorData: String,
  val clientDataJSON: String,
  val signature: String,
  val userHandle: String = "",
)
