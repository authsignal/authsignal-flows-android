package com.authsignal.flows.security

import com.authsignal.flows.AuthsignalFlowsError
import com.authsignal.flows.extensions.toBase64String
import com.authsignal.flows.logger.Logger
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature
import kotlin.math.floor

internal object Signer {
  fun sign(message: String, key: PrivateKeyEntry): String {
    return try {
      val signer = Signature.getInstance("SHA256withECDSA")

      signer.initSign(key.privateKey)
      signer.update(message.toByteArray(Charsets.UTF_8))

      signer.sign().toBase64String()
    } catch (e: Exception) {
      Logger.e("Error signing message: ${e.message}")

      throw AuthsignalFlowsError.Unexpected("signing_error", e.message ?: "Error signing message.")
    }
  }

  fun signWithTimeBasedMessage(key: PrivateKeyEntry): String {
    val secondsSinceEpoch = (System.currentTimeMillis() / 1000).toDouble()

    val message = floor(secondsSinceEpoch / (60 * 10)).toString()

    return sign(message, key)
  }
}
