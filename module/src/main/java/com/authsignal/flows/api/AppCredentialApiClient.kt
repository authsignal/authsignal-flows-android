package com.authsignal.flows.api

import com.authsignal.flows.api.models.appcredential.SignedRequest
import com.authsignal.flows.api.models.appcredential.SigningChallengeRequest
import com.authsignal.flows.api.models.appcredential.SigningChallengeResponse
import com.authsignal.flows.api.models.inapp.RemoveCredentialResponse
import com.authsignal.flows.security.Signer
import java.security.KeyStore

internal enum class AppCredentialType(val path: String) {
  IN_APP("in-app"),
  PUSH("push"),
}

internal class AppCredentialApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun createSignedRequest(
    credentialType: AppCredentialType,
    publicKey: String,
    key: KeyStore.PrivateKeyEntry,
  ): SignedRequest {
    val challenge: SigningChallengeResponse = postRequest(
      url = "$apiUrl/v1/client/flows/user-authenticators/${credentialType.path}/sign",
      body = SigningChallengeRequest(publicKey = publicKey),
    )

    return SignedRequest(
      publicKey = publicKey,
      signingChallengeId = challenge.signingChallengeId,
      signature = Signer.sign(challenge.message, key),
    )
  }

  suspend fun removeCredential(
    credentialType: AppCredentialType,
    signedRequest: SignedRequest,
  ): RemoveCredentialResponse {
    return postRequest(
      url = "$apiUrl/v1/client/flows/user-authenticators/${credentialType.path}/remove",
      body = signedRequest,
    )
  }
}
