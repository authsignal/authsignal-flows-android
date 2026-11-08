package com.authsignal.flows.api.models.push

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePushCredentialRequest(
  val challengeId: String,
  val publicKey: String,
  val signature: String,
  val pushToken: String? = null,
  val resetExpiry: Boolean? = null,
)

@Serializable
internal data class SigningMessageResponse(
  val challengeId: String? = null,
  val message: String? = null,
)

@Serializable
internal data class UpdatePushCredentialResponse(
  val userAuthenticatorId: String,
)
