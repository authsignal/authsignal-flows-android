package com.authsignal.flows

class AuthsignalFlows(tenantId: String, apiUrl: String) {
  val email: AuthsignalEmail = AuthsignalEmail(tenantId, apiUrl)
  val sms: AuthsignalSms = AuthsignalSms(tenantId, apiUrl)
  val whatsapp: AuthsignalWhatsapp = AuthsignalWhatsapp(tenantId, apiUrl)
  val passkey: AuthsignalPasskey = AuthsignalPasskey(tenantId, apiUrl)

  fun setChallengeToken(challengeToken: String) {
    TokenCache.challengeToken = challengeToken
  }
}
