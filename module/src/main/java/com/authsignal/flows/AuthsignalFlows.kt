package com.authsignal.flows

class AuthsignalFlows(tenantId: String, apiUrl: String, clientId: String? = null) {
  private val session = FlowSession(tenantId, apiUrl, clientId)

  val email: AuthsignalEmail = AuthsignalEmail(tenantId, apiUrl, session)
  val sms: AuthsignalSms = AuthsignalSms(tenantId, apiUrl, session)
  val whatsapp: AuthsignalWhatsapp = AuthsignalWhatsapp(tenantId, apiUrl)
  val passkey: AuthsignalPasskey = AuthsignalPasskey(tenantId, apiUrl, session)

  fun setChallengeToken(challengeToken: String) {
    TokenCache.challengeToken = challengeToken
    TokenCache.codeVerifier = null
    TokenCache.isCompleted = false
  }
}
