package com.authsignal.flows

import com.authsignal.flows.api.FlowApiClient
import com.authsignal.flows.api.models.FlowState
import com.authsignal.flows.api.models.VerifyResponse
import com.authsignal.flows.extensions.toBase64UrlString
import com.authsignal.flows.logger.Logger
import java.security.MessageDigest
import java.security.SecureRandom

internal class FlowContext(
  tenantId: String,
  apiUrl: String,
  private val clientId: String?,
  private val session: AuthsignalSession,
) {
  private val api = FlowApiClient(tenantId, apiUrl)

  suspend fun <T> withChallengeToken(operation: suspend (String) -> T): T {
    val challengeToken = currentOrNewChallengeToken()

    return try {
      operation(challengeToken)
    } catch (e: AuthsignalFlowsError.FlowExpired) {
      val clientId = clientId ?: throw e

      // Only flows started by the SDK have a code verifier; others came from 'setChallengeToken'
      if (TokenCache.codeVerifier == null) {
        throw e
      }

      Logger.i("The flow has expired. Starting a new flow.")

      operation(startFlow(clientId))
    }
  }

  suspend fun update(response: VerifyResponse) {
    TokenCache.update(response)

    // Flows started with 'setChallengeToken' are verified by the integrator's backend
    val codeVerifier = TokenCache.codeVerifier

    if (response.action.state != FlowState.CHALLENGE_SUCCEEDED || codeVerifier == null) {
      return
    }

    val verifyResponse = api.verify(challengeToken = response.challengeToken, codeVerifier = codeVerifier)

    val tokens = verifyResponse.session
      ?: throw AuthsignalFlowsError.Unexpected("session_unavailable", "The flow completed without a session.")

    session.save(tokens)
  }

  private suspend fun currentOrNewChallengeToken(): String {
    val challengeToken = TokenCache.challengeToken

    if (challengeToken != null && !TokenCache.isCompleted) {
      return challengeToken
    }

    val clientId = clientId ?: return TokenCache.requireChallengeToken()

    return startFlow(clientId)
  }

  private suspend fun startFlow(clientId: String): String {
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
