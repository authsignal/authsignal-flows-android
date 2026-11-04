package com.authsignal.flows.api

import com.authsignal.flows.api.models.session.RefreshTokenRequest
import com.authsignal.flows.api.models.session.SessionTokens
import kotlinx.serialization.encodeToString

internal class SessionApiClient(tenantId: String, apiUrl: String) : BaseApiClient(tenantId, apiUrl) {
  suspend fun refresh(refreshToken: String): SessionTokens {
    val url = "$apiUrl/v1/client/sessions/refresh"

    val body = RefreshTokenRequest(refreshToken = refreshToken)

    return postRequest(url = url, body = body)
  }

  suspend fun revoke(refreshToken: String) {
    val url = "$apiUrl/v1/client/sessions/revoke"

    val body = RefreshTokenRequest(refreshToken = refreshToken)

    executeRequest(url, challengeToken = null, json.encodeToString(body))
  }
}
