package com.authsignal.flows.api.models.inapp

import kotlinx.serialization.Serializable

@Serializable
data class RemoveCredentialResponse(
  val removedAuthenticatorId: String? = null,
)
