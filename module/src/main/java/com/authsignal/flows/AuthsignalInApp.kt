package com.authsignal.flows

import android.content.Context
import android.os.Build
import com.authsignal.flows.api.InAppApiClient
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.models.AppCredential
import com.authsignal.flows.security.KeyManager
import com.authsignal.flows.security.PinManager
import com.authsignal.flows.security.Signer

class AuthsignalInApp internal constructor(
  context: Context,
  tenantId: String,
  apiUrl: String,
  private val flow: FlowContext,
) {
  private val api = InAppApiClient(tenantId, apiUrl)
  private val keyManager = KeyManager("in_app")
  private val pinManager = PinManager(context)

  /**
   * Returns the credential for this device, or null if the device has no in-app key.
   */
  suspend fun getCredential(username: String? = null): AppCredential? {
    val publicKey = keyManager.getPublicKey(username) ?: return null

    val response = api.getCredential(publicKey)

    return AppCredential(
      credentialId = response.userAuthenticatorId,
      createdAt = response.verifiedAt,
      userId = response.userId,
      lastAuthenticatedAt = response.lastVerifiedAt,
      expiresAt = response.expiresAt,
    )
  }

  /**
   * Creates a key on this device and adds it as an in-app authenticator for the current flow.
   */
  suspend fun addCredential(
    userAuthenticationRequired: Boolean = false,
    timeout: Int = 0,
    authorizationType: Int = 0,
    username: String? = null,
  ): VerifyResponse {
    val challengeToken = TokenCache.requireChallengeToken()

    val publicKey = keyManager.getOrCreatePublicKey(
      userAuthenticationRequired = userAuthenticationRequired,
      timeout = timeout,
      authorizationType = authorizationType,
      username = username,
    )

    val response = api.addCredential(challengeToken, publicKey, getDeviceName())

    flow.update(response)

    return response
  }

  /**
   * Removes the in-app authenticator for this device and deletes the key.
   *
   * @throws AuthsignalFlowsError.CredentialNotFound if the device has no in-app key.
   */
  suspend fun removeCredential(username: String? = null) {
    val key = keyManager.getKey(username) ?: throw credentialNotFound()

    val signature = Signer.signWithTimeBasedMessage(key)

    api.removeCredential(keyManager.derivePublicKey(key), signature)

    keyManager.deleteKey(username)
  }

  /**
   * Verifies the current flow step by signing a challenge with this device's key.
   * Starts a new flow if a client ID is configured and no flow is in progress.
   *
   * @throws AuthsignalFlowsError.CredentialNotFound if the device has no in-app key.
   */
  suspend fun verify(username: String? = null): VerifyResponse {
    val key = keyManager.getKey(username) ?: throw credentialNotFound()

    val publicKey = keyManager.derivePublicKey(key)

    val response = flow.withChallengeToken { challengeToken ->
      val challengeResponse = api.challenge(challengeToken)

      val signature = Signer.sign(challengeResponse.challenge, key)

      api.verify(challengeToken, publicKey, signature)
    }

    flow.update(response)

    return response
  }

  /**
   * Adds an in-app credential for [username] and protects it with a PIN stored on this device.
   *
   * @throws AuthsignalFlowsError.InvalidPinFormat if the PIN isn't at least 4 digits.
   */
  suspend fun createPin(pin: String, username: String): VerifyResponse {
    if (!pinManager.validateFormat(pin)) {
      throw AuthsignalFlowsError.InvalidPinFormat()
    }

    val response = addCredential(username = username)

    pinManager.createPin(pin, username)

    return response
  }

  /**
   * Checks the PIN for [username], then verifies the current flow step with its in-app credential.
   *
   * @throws AuthsignalFlowsError.InvalidPin if the PIN doesn't match.
   */
  suspend fun verifyPin(pin: String, username: String): VerifyResponse {
    if (!pinManager.validatePin(pin, username)) {
      throw AuthsignalFlowsError.InvalidPin()
    }

    return verify(username)
  }

  /**
   * Removes the in-app credential for [username] and deletes its PIN.
   */
  suspend fun deletePin(username: String) {
    try {
      removeCredential(username)
    } catch (e: AuthsignalFlowsError) {
      if (e.code != "credential_not_found" && e.code != "invalid_credential") {
        throw e
      }

      // The credential was already removed, so only local state is left to clean up
      keyManager.deleteKey(username)
    }

    pinManager.deletePin(username)
  }

  fun getAllPinUsernames(): List<String> {
    return pinManager.getAllUsernames()
  }

  private fun credentialNotFound(): AuthsignalFlowsError.CredentialNotFound {
    return AuthsignalFlowsError.CredentialNotFound("No credential found on this device.")
  }

  private fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL

    return if (model.startsWith(manufacturer)) model else "$manufacturer $model"
  }
}
