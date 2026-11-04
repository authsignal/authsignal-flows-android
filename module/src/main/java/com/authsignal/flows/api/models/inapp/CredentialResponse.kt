package com.authsignal.flows.api.models.inapp

import kotlinx.serialization.Serializable

@Serializable
data class CredentialResponse(
  val userAuthenticatorId: String,
  val userId: String,
  val verifiedAt: String,
  val lastVerifiedAt: String? = null,
  val expiresAt: String? = null,
)
