package com.authsignal.flows.api

import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.appcredential.SignedRequest
import com.authsignal.flows.api.models.inapp.CredentialResponse
import com.authsignal.flows.api.models.push.AddPushCredentialRequest
import com.authsignal.flows.api.models.push.PendingPushChallengeResponse
import com.authsignal.flows.api.models.push.PushChallengeResponse
import com.authsignal.flows.api.models.push.PushVerifyResponse
import com.authsignal.flows.api.models.push.RespondToPushChallengeRequest
import com.authsignal.flows.api.models.push.RespondToPushChallengeResponse
import com.authsignal.flows.api.models.push.SigningMessageResponse
import com.authsignal.flows.api.models.push.UpdatePushCredentialRequest
import com.authsignal.flows.api.models.push.UpdatePushCredentialResponse
import com.authsignal.flows.extensions.toBase64UrlString

internal class PushApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun getCredential(publicKey: String): CredentialResponse {
    return getRequestWithBasicAuth("$apiUrl/v1/client/user-authenticators/push?publicKey=${encode(publicKey)}")
  }

  suspend fun addCredential(
    challengeToken: String,
    publicKey: String,
    deviceName: String,
    pushToken: String?,
  ): VerifyResponse {
    val url = "$apiUrl/v1/client/flows/user-authenticators/push"

    val body = AddPushCredentialRequest(
      publicKey = publicKey,
      deviceName = deviceName,
      devicePlatform = "android",
      pushToken = pushToken,
    )

    return postRequest(url = url, challengeToken = challengeToken, body = body)
  }

  suspend fun getSigningMessage(publicKey: String): SigningMessageResponse {
    return postRequestWithBasicAuth("$apiUrl/v1/client/user-authenticators/push/sign?publicKey=${encode(publicKey)}")
  }

  suspend fun updateCredential(body: UpdatePushCredentialRequest): UpdatePushCredentialResponse {
    return patchRequestWithBasicAuth("$apiUrl/v1/client/user-authenticators/push", body)
  }

  suspend fun challenge(challengeToken: String): PushChallengeResponse {
    return postRequest(url = "$apiUrl/v1/client/flows/challenge/push", challengeToken = challengeToken)
  }

  suspend fun verify(challengeToken: String): PushVerifyResponse {
    return postRequest(url = "$apiUrl/v1/client/flows/verify/push", challengeToken = challengeToken)
  }

  suspend fun getPendingChallenge(signedRequest: SignedRequest): PendingPushChallengeResponse {
    val publicKey = encode(signedRequest.publicKey)

    val url = "$apiUrl/v1/client/flows/user-authenticators/push/challenge" +
      "?publicKey=$publicKey&signingChallengeId=${signedRequest.signingChallengeId}"

    return getRequest(url, mapOf("X-Authsignal-Signature" to signedRequest.signature))
  }

  suspend fun respondToChallenge(body: RespondToPushChallengeRequest): RespondToPushChallengeResponse {
    return postRequest(url = "$apiUrl/v1/client/flows/user-authenticators/push/challenge", body = body)
  }

  private fun encode(publicKey: String): String {
    return publicKey.toByteArray().toBase64UrlString()
  }
}
