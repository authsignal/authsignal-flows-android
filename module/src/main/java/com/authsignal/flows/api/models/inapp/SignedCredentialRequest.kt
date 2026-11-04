package com.authsignal.flows.api.models.inapp

import kotlinx.serialization.Serializable

@Serializable
data class SignedCredentialRequest(
  val publicKey: String,
  val signature: String,
)
