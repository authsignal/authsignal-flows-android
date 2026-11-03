package com.authsignal.flows

import com.authsignal.flows.api.SessionApiClient
import com.authsignal.flows.extensions.toBase64UrlString
import com.authsignal.flows.logger.Logger
import java.security.MessageDigest
import java.security.SecureRandom

internal class FlowSession(tenantId: String, apiUrl: String, private val clientId: String?) {
  private val api = SessionApiClient(tenantId, apiUrl)

  suspend fun <T> withChallengeToken(operation: suspend (String) -> T): T {
    val challengeToken = currentOrNewChallengeToken()

    return try {
      operation(challengeToken)
    } catch (e: AuthsignalFlowsError.SessionExpired) {
      val clientId = clientId ?: throw e

      Logger.i("The session has expired. Starting a new session.")

      operation(startSession(clientId))
    }
  }

  private suspend fun currentOrNewChallengeToken(): String {
    val challengeToken = TokenCache.challengeToken

    if (challengeToken != null && !TokenCache.isCompleted) {
      return challengeToken
    }

    val clientId = clientId ?: return TokenCache.requireChallengeToken()

    return startSession(clientId)
  }

  private suspend fun startSession(clientId: String): String {
    val codeVerifier = generateCodeVerifier()

    val response = api.start(
      actionCode = ACTION_CODE,
      clientId = clientId,
      codeChallenge = codeChallenge(codeVerifier),
    )

    TokenCache.challengeToken = response.challengeToken
    TokenCache.codeVerifier = codeVerifier
    TokenCache.isCompleted = false

    return response.challengeToken
  }

  private fun generateCodeVerifier(): String {
    val bytes = ByteArray(32)

    SecureRandom().nextBytes(bytes)

    return bytes.toBase64UrlString()
  }

  private fun codeChallenge(codeVerifier: String): String {
    return MessageDigest.getInstance("SHA-256")
      .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
      .toBase64UrlString()
  }

  private companion object {
    // Temporary until the sign-in action code can be configured on the app client
    const val ACTION_CODE = "signInOrSignUpV2"
  }
}
