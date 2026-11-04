package com.authsignal.flows.api.models.inapp

import kotlinx.serialization.Serializable

@Serializable
data class AddInAppCredentialRequest(
  val publicKey: String,
  val deviceName: String,
  val devicePlatform: String,
)
