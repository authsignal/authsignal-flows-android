package com.authsignal.flows

import com.authsignal.flows.logger.Logger

internal object TokenCache {
  private var _challengeToken: String? = null

  var challengeToken: String?
    get() = _challengeToken
    set(value) {
      if (!value.isNullOrEmpty()) {
        _challengeToken = value
      }
    }

  var codeVerifier: String? = null

  fun requireChallengeToken(): String {
    val token = _challengeToken

    if (token == null) {
      Logger.e("Error: A challenge token has not been set. Call 'setChallengeToken' first.")

      throw AuthsignalFlowsError.FlowNotInitialized()
    }

    return token
  }
}
