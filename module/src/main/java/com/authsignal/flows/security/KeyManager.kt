package com.authsignal.flows.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.authsignal.flows.AuthsignalFlowsError
import com.authsignal.flows.extensions.toBase64String
import com.authsignal.flows.logger.Logger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.X509EncodedKeySpec

internal class KeyManager(keySuffix: String) {
  private val keyTag = "authsignal_signing_key_$keySuffix"

  fun getOrCreatePublicKey(
    userAuthenticationRequired: Boolean,
    timeout: Int,
    authorizationType: Int,
    username: String? = null,
  ): String {
    return getPublicKey(username) ?: createKey(userAuthenticationRequired, timeout, authorizationType, username)
  }

  fun getKey(username: String? = null): KeyStore.PrivateKeyEntry? {
    return try {
      val keyStore = KeyStore.getInstance("AndroidKeyStore")

      keyStore.load(null)

      keyStore.getEntry(getUserKeyTag(username), null) as? KeyStore.PrivateKeyEntry
    } catch (e: Exception) {
      null
    }
  }

  fun getPublicKey(username: String? = null): String? {
    return getKey(username)?.let { derivePublicKey(it) }
  }

  fun derivePublicKey(key: KeyStore.PrivateKeyEntry): String {
    return X509EncodedKeySpec(key.certificate.publicKey.encoded).encoded.toBase64String()
  }

  fun deleteKey(username: String? = null): Boolean {
    return try {
      val keyStore = KeyStore.getInstance("AndroidKeyStore")

      keyStore.load(null)

      val userKeyTag = getUserKeyTag(username)

      if (keyStore.containsAlias(userKeyTag)) {
        keyStore.deleteEntry(userKeyTag)
      }

      true
    } catch (e: Exception) {
      Logger.e("Error deleting key: ${e.message}")

      false
    }
  }

  private fun createKey(
    userAuthenticationRequired: Boolean,
    timeout: Int,
    authorizationType: Int,
    username: String?,
  ): String {
    val paramsBuilder = KeyGenParameterSpec.Builder(getUserKeyTag(username), KeyProperties.PURPOSE_SIGN)
      .setDigests(KeyProperties.DIGEST_SHA256)
      .setUserAuthenticationRequired(userAuthenticationRequired)

    if (userAuthenticationRequired && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      paramsBuilder.setUserAuthenticationParameters(timeout, authorizationType)
    }

    return try {
      val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

      generator.initialize(paramsBuilder.build())

      generator.generateKeyPair().public.encoded.toBase64String()
    } catch (e: Exception) {
      Logger.e("Error creating key pair: ${e.message}")

      throw AuthsignalFlowsError.Unexpected("create_key_pair_failed", e.message ?: "Error creating key pair.")
    }
  }

  private fun getUserKeyTag(username: String?): String {
    val cleanUsername = username?.trim()

    return cleanUsername?.let { "${keyTag}_$it" } ?: keyTag
  }
}
