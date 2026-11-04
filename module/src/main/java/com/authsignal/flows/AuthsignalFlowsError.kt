package com.authsignal.flows

sealed class AuthsignalFlowsError(message: String) : Exception(message) {
  abstract val code: String

  /**
   * Thrown when an operation is attempted before [com.authsignal.flows.AuthsignalFlows.setChallengeToken]
   * has been called.
   */
  class FlowNotInitialized : AuthsignalFlowsError(
    "The flow has not been initialized. Provide a client ID or call 'setChallengeToken' first."
  ) {
    override val code: String = "flow_not_initialized"
  }

  /**
   * Thrown when the flow's challenge token has expired. Challenge methods start a new
   * flow automatically when a client ID is configured, but `verify` calls throw this.
   */
  class FlowExpired : AuthsignalFlowsError(
    "The flow has expired. Start a new challenge to continue."
  ) {
    override val code: String = "flow_expired"
  }

  /**
   * Thrown by `verify` calls when the provided verification code is incorrect.
   */
  class InvalidCode(message: String) : AuthsignalFlowsError(message) {
    override val code: String = "invalid_code"
  }

  /**
   * Thrown when the user cancels a passkey request.
   */
  class UserCanceled : AuthsignalFlowsError(
    "The request was canceled by the user."
  ) {
    override val code: String = "user_canceled"
  }

  /**
   * Thrown when the user cancels a passkey request.
   */
  class CredentialNotFound : AuthsignalFlowsError(
    "The device has no passkeys available."
  ) {
    override val code: String = "credential_not_found"
  }

  /**
   * Thrown during passkey registration when an existing credential already
   * matches one of the excluded credentials for this device.
   */
  class MatchedExcludedCredential : AuthsignalFlowsError(
    "An existing credential is already available for this device."
  ) {
    override val code: String = "matched_excluded_credential"
  }

  /**
   * Thrown when the underlying HTTP request fails (e.g. no connectivity).
   */
  class NetworkError(message: String) : AuthsignalFlowsError(message) {
    override val code: String = "network_error"
  }

  /**
   * Thrown for generic API failures that aren't otherwise represented by a
   * more specific error type.
   */
  class ApiError(message: String) : AuthsignalFlowsError(message) {
    override val code: String = "api_error"
  }

  /**
   * Thrown when a response body could not be decoded into the expected shape.
   */
  class DecodingError(message: String) : AuthsignalFlowsError(message) {
    override val code: String = "decoding_error"
  }

  /**
   * Catch-all for any error code returned by the API that doesn't have a
   * dedicated subtype above.
   */
  class Unexpected(override val code: String, message: String) : AuthsignalFlowsError(message)

  companion object {
    fun fromApi(code: String, message: String): AuthsignalFlowsError {
      return when (code) {
        "invalid_code" -> InvalidCode(message)
        "invalid_token" -> FlowExpired()
        else -> Unexpected(code, message)
      }
    }
  }
}
