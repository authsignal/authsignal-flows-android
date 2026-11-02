package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.passkey.PasskeyAuthenticationOptionsResponse
import com.authsignal.flows.api.models.passkey.PasskeyRegistrationOptionsRequest
import com.authsignal.flows.api.models.passkey.PasskeyRegistrationOptionsResponse
import com.authsignal.flows.api.models.passkey.PasskeyRegistrationRequest
import com.authsignal.flows.api.models.passkey.PasskeyVerifyRequest
import com.authsignal.flows.models.PasskeyAuthenticationCredential
import com.authsignal.flows.models.PasskeyRegistrationCredential

internal class PasskeyApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun authenticationOptions(challengeToken: String): PasskeyAuthenticationOptionsResponse {
    val url = "$apiUrl/v1/client/flows/challenge/passkey"

    return postRequest(url = url, challengeToken = challengeToken)
  }

  suspend fun registrationOptions(
    challengeToken: String,
    username: String? = null,
    displayName: String? = null,
  ): PasskeyRegistrationOptionsResponse {
    val url = "$apiUrl/v1/client/flows/user-authenticators/passkey/registration-options"

    val body = PasskeyRegistrationOptionsRequest(username = username, displayName = displayName)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun register(
    challengeToken: String,
    credential: PasskeyRegistrationCredential,
  ): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/user-authenticators/passkey"

    val body = PasskeyRegistrationRequest(registrationCredential = credential)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun verify(
    challengeToken: String,
    credential: PasskeyAuthenticationCredential,
  ): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/verify/passkey"

    val body = PasskeyVerifyRequest(authenticationResponse = credential)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }
}
