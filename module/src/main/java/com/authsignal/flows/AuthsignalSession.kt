package com.authsignal.flows

import android.content.Context
import com.authsignal.flows.api.SessionApiClient
import com.authsignal.flows.api.models.session.SessionTokens
import com.authsignal.flows.logger.Logger
import com.authsignal.flows.security.SessionStore
import com.authsignal.flows.security.StoredSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthsignalSession internal constructor(context: Context, tenantId: String, apiUrl: String) {
  private val api = SessionApiClient(tenantId, apiUrl)
  private val store = SessionStore(context)
  private val mutex = Mutex()

  /**
   * Returns a valid access token, refreshing it first if it's about to expire.
   *
   * Returns null if there is no session, or if the session can no longer be refreshed.
   *
   * @throws AuthsignalFlowsError.NetworkError if a refresh fails to reach the API. The session is kept.
   */
  suspend fun getAccessToken(): String? = mutex.withLock {
    val storedSession = store.load() ?: return@withLock null

    if (storedSession.accessTokenExpiresAt - REFRESH_LEEWAY_MILLIS > System.currentTimeMillis()) {
      return@withLock storedSession.accessToken
    }

    refresh(storedSession)?.accessToken
  }

  /**
   * Ends the session. Local state is always cleared, even if the session can't be revoked on the server.
   */
  suspend fun revoke() {
    val storedSession = mutex.withLock {
      store.load()?.also { store.clear() }
    } ?: return

    try {
      api.revoke(storedSession.refreshToken)
    } catch (e: AuthsignalFlowsError) {
      Logger.e("Error revoking session: ${e.message}")
    }
  }

  internal suspend fun save(tokens: SessionTokens) = mutex.withLock {
    store.save(StoredSession.from(tokens))
  }

  private suspend fun refresh(storedSession: StoredSession): StoredSession? {
    return try {
      val refreshedSession = StoredSession.from(api.refresh(storedSession.refreshToken))

      store.save(refreshedSession)

      refreshedSession
    } catch (e: AuthsignalFlowsError.FlowExpired) {
      // The API reports a revoked or expired refresh token as 'invalid_token'
      store.clear()

      null
    }
  }

  private companion object {
    const val REFRESH_LEEWAY_MILLIS = 30_000L
  }
}
