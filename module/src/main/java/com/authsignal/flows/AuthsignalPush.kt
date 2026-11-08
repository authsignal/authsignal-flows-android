package com.authsignal.flows

import android.os.Build
import com.authsignal.flows.api.AppCredentialApiClient
import com.authsignal.flows.api.AppCredentialType
import com.authsignal.flows.api.PushApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.api.models.push.PushChallenge
import com.authsignal.flows.api.models.push.PushChallengeResponse
import com.authsignal.flows.api.models.push.PushChallengeStatus
import com.authsignal.flows.api.models.push.PushVerifyResponse
import com.authsignal.flows.api.models.push.RespondToPushChallengeRequest
import com.authsignal.flows.api.models.push.UpdatePushCredentialRequest
import com.authsignal.flows.models.AppCredential
import com.authsignal.flows.security.KeyManager
import com.authsignal.flows.security.Signer
import java.security.KeyStore

class AuthsignalPush internal constructor(
  tenantId: String,
  apiUrl: String,
  private val flow: FlowContext,
) {
  private val api = PushApiClient(tenantId, apiUrl)
  private val appCredentials = AppCredentialApiClient(tenantId, apiUrl)
  private val keyManager = KeyManager("push")

  suspend fun challenge(): PushChallengeResponse {
    return api.challenge(TokenCache.requireChallengeToken())
  }

  suspend fun verify(): PushVerifyResponse {
    val response = api.verify(TokenCache.requireChallengeToken())

    val action = response.action
    val challengeToken = response.challengeToken
    val user = response.user

    if (response.status == PushChallengeStatus.APPROVED && action != null && challengeToken != null && user != null) {
      flow.update(VerifyResponse(action = action, challengeToken = challengeToken, user = user))
    }

    return response
  }

  suspend fun getCredential(): AppCredential? {
    val publicKey = keyManager.getPublicKey() ?: return null

    val response = api.getCredential(publicKey)

    return AppCredential(
      credentialId = response.userAuthenticatorId,
      createdAt = response.verifiedAt,
      userId = response.userId,
      lastAuthenticatedAt = response.lastVerifiedAt,
      expiresAt = response.expiresAt,
    )
  }

  suspend fun addCredential(
    pushToken: String? = null,
    userAuthenticationRequired: Boolean = false,
    timeout: Int = 0,
    authorizationType: Int = 0,
  ): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val publicKey = keyManager.getOrCreatePublicKey(
      userAuthenticationRequired = userAuthenticationRequired,
      timeout = timeout,
      authorizationType = authorizationType,
    )

    val response = api.addCredential(challengeToken, publicKey, getDeviceName(), pushToken)

    flow.update(response)

    return response
  }

  suspend fun removeCredential() {
    val key = requireKey()

    val signedRequest = appCredentials.createSignedRequest(
      AppCredentialType.PUSH,
      keyManager.derivePublicKey(key),
      key,
    )

    appCredentials.removeCredential(AppCredentialType.PUSH, signedRequest)

    keyManager.deleteKey()
  }

  suspend fun updateCredential(pushToken: String? = null, resetExpiry: Boolean = false) {
    val key = requireKey()

    val publicKey = keyManager.derivePublicKey(key)

    val signingMessage = api.getSigningMessage(publicKey)

    val challengeId = signingMessage.challengeId
    val message = signingMessage.message

    if (challengeId == null || message == null) {
      throw AuthsignalFlowsError.ApiError("Invalid signing message response.")
    }

    api.updateCredential(
      UpdatePushCredentialRequest(
        challengeId = challengeId,
        publicKey = publicKey,
        signature = Signer.sign(message, key),
        pushToken = pushToken,
        resetExpiry = if (resetExpiry) true else null,
      )
    )
  }

  suspend fun getChallenge(): PushChallenge? {
    val key = requireKey()

    val signedRequest = appCredentials.createSignedRequest(
      AppCredentialType.PUSH,
      keyManager.derivePublicKey(key),
      key,
    )

    val response = api.getPendingChallenge(signedRequest)

    val challengeId = response.challengeId ?: return null

    return PushChallenge(
      challengeId = challengeId,
      actionCode = response.actionCode,
      deviceId = response.deviceId,
      userAgent = response.userAgent,
      ipAddress = response.ipAddress,
      expiresAt = response.expiresAt,
    )
  }

  suspend fun updateChallenge(challengeId: String, approved: Boolean, verificationCode: String? = null) {
    val key = requireKey()

    api.respondToChallenge(
      RespondToPushChallengeRequest(
        publicKey = keyManager.derivePublicKey(key),
        challengeId = challengeId,
        signature = Signer.sign(challengeId, key),
        approved = approved,
        verificationCode = verificationCode,
      )
    )
  }

  private fun requireKey(): KeyStore.PrivateKeyEntry {
    return keyManager.getKey() ?: throw AuthsignalFlowsError.CredentialNotFound("No credential found on this device.")
  }

  private fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL

    return if (model.startsWith(manufacturer)) model else "$manufacturer $model"
  }
}
