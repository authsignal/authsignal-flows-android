package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.passkey.PasskeyAuthenticationOptionsResponse
import com.authsignal.flows.api.models.passkey.PasskeyVerifyRequest
import com.authsignal.flows.models.PasskeyAuthenticationCredential

internal class PasskeyApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun authenticationOptions(challengeToken: String): PasskeyAuthenticationOptionsResponse {
    val url = "$apiUrl/v1/client/flows/challenge/passkey"

    return postRequest(url = url, challengeToken = challengeToken)
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
