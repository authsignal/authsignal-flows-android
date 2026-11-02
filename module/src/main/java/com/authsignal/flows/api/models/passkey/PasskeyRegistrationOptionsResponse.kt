package com.authsignal.flows.api.models.passkey

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyRegistrationOptionsResponse(
  val registrationOptions: PasskeyRegistrationOptions,
)

@Serializable
data class PasskeyRegistrationOptions(
  val challenge: String,
  val rp: PasskeyRegistrationOptionsRelyingParty,
  val user: PasskeyRegistrationOptionsUser,
  val timeout: Int? = null,
  val pubKeyCredParams: List<PasskeyRegistrationOptionsPubKeyCredParams>,
  val attestation: String? = null,
  val excludeCredentials: List<PasskeyRegistrationOptionsCredential> = emptyList(),
  val authenticatorSelection: PasskeyRegistrationOptionsAuthenticatorSelection,
)

@Serializable
data class PasskeyRegistrationOptionsRelyingParty(
  val id: String,
  val name: String,
)

@Serializable
data class PasskeyRegistrationOptionsUser(
  val id: String,
  val name: String,
  val displayName: String,
)

@Serializable
data class PasskeyRegistrationOptionsPubKeyCredParams(
  val alg: Int,
  val type: String,
)

@Serializable
data class PasskeyRegistrationOptionsAuthenticatorSelection(
  val authenticatorAttachment: String? = null,
  val requireResidentKey: Boolean? = null,
  val residentKey: String? = null,
  val userVerification: String? = null,
)

@Serializable
data class PasskeyRegistrationOptionsCredential(
  val id: String,
  val type: String,
)
