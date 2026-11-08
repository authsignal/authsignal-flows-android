package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.inapp.AddInAppCredentialRequest
import com.authsignal.flows.api.models.inapp.CredentialResponse
import com.authsignal.flows.api.models.inapp.InAppChallengeResponse
import com.authsignal.flows.api.models.inapp.SignedCredentialRequest
import com.authsignal.flows.extensions.toBase64UrlString

internal class InAppApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun getCredential(publicKey: String): CredentialResponse {
    val encodedKey = publicKey.toByteArray().toBase64UrlString()

    val url = "$apiUrl/v1/client/user-authenticators/in-app?publicKey=$encodedKey"

    return getRequestWithBasicAuth(url)
  }

  suspend fun addCredential(challengeToken: String, publicKey: String, deviceName: String): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/user-authenticators/in-app"

    val body = AddInAppCredentialRequest(publicKey = publicKey, deviceName = deviceName, devicePlatform = "android")

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun challenge(challengeToken: String): InAppChallengeResponse {
    val url = "$apiUrl/v1/client/flows/challenge/in-app"

    return postRequest(url = url, challengeToken = challengeToken)
  }

  suspend fun verify(challengeToken: String, publicKey: String, signature: String): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/verify/in-app"

    val body = SignedCredentialRequest(publicKey = publicKey, signature = signature)

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }
}
