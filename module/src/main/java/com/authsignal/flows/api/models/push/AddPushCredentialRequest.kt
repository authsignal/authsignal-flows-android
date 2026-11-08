package com.authsignal.flows.api.models.push

import kotlinx.serialization.Serializable

@Serializable
data class AddPushCredentialRequest(
  val publicKey: String,
  val deviceName: String,
  val devicePlatform: String,
  val pushToken: String? = null,
)
