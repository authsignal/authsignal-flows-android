package com.authsignal.flows.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyResponse(
  val action: FlowAction,
  val challengeToken: String,
  val user: FlowUser,
)

@Serializable
data class FlowAction(
  val state: FlowState,
  val nextStep: ActionStep? = null,
  val completedSteps: List<CompletedActionStep>? = null,
)

@Serializable
data class ActionStep(
  val stepType: ActionStepType,
  val verificationMethods: List<VerificationMethod>,
)

@Serializable
data class CompletedActionStep(
  val stepType: ActionStepType,
  val verificationMethod: VerificationMethod? = null,
  val userAuthenticatorId: String? = null,
)

@Serializable
data class FlowUser(
  val userId: String,
  val email: String? = null,
  val phoneNumber: String? = null,
  val username: String? = null,
  val displayName: String? = null,
  val authenticators: List<FlowUserAuthenticator>,
)

@Serializable
data class FlowUserAuthenticator(
  val userAuthenticatorId: String,
  val verificationMethod: VerificationMethod,
  val email: String? = null,
  val phoneNumber: String? = null,
  val username: String? = null,
  val displayName: String? = null,
)

@Serializable
enum class ActionStepType {
  @SerialName("VERIFICATION_REQUIRED")
  VERIFICATION_REQUIRED,

  @SerialName("ENROLLMENT_REQUIRED")
  ENROLLMENT_REQUIRED,

  @SerialName("ENROLLMENT_OPTIONAL")
  ENROLLMENT_OPTIONAL,
}

@Serializable
enum class VerificationMethod {
  @SerialName("EMAIL_OTP")
  EMAIL_OTP,

  @SerialName("SMS")
  SMS,

  @SerialName("WHATSAPP")
  WHATSAPP,

  @SerialName("PASSKEY")
  PASSKEY,

  @SerialName("IN_APP")
  IN_APP,

  @SerialName("QR_CODE")
  QR_CODE,

  @SerialName("PUSH")
  PUSH,

  @SerialName("AUTHENTICATOR_APP")
  AUTHENTICATOR_APP,
}

@Serializable
enum class FlowState {
  @SerialName("CHALLENGE_REQUIRED")
  CHALLENGE_REQUIRED,

  @SerialName("CHALLENGE_FAILED")
  CHALLENGE_FAILED,

  @SerialName("CHALLENGE_SUCCEEDED")
  CHALLENGE_SUCCEEDED,
}
