package com.authsignal.flows.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.authsignal.flows.logger.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class SessionStore(context: Context) {
  private val service = "authsignal_session"
  private val keyAlias = "authsignal_session_key"
  private val prefs = context.getSharedPreferences(service, Context.MODE_PRIVATE)

  private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
    load(null)
  }

  init {
    if (!keyStore.containsAlias(keyAlias)) {
      generateKey()
    }
  }

  fun load(): StoredSession? {
    val encryptedSession = prefs.getString(SESSION_KEY, null) ?: return null
    val iv = prefs.getString(IV_KEY, null) ?: return null

    return try {
      val encrypted = Base64.decode(encryptedSession, Base64.DEFAULT)
      val ivBytes = Base64.decode(iv, Base64.DEFAULT)

      Json.decodeFromString<StoredSession>(decrypt(encrypted, ivBytes))
    } catch (e: Exception) {
      // e.g. preferences restored from a backup onto a device without the original key
      Logger.e("Error loading session: ${e.message}")

      clear()

      null
    }
  }

  fun save(session: StoredSession) {
    val (encrypted, iv) = encrypt(Json.encodeToString(session))

    prefs.edit().apply {
      putString(SESSION_KEY, Base64.encodeToString(encrypted, Base64.DEFAULT))
      putString(IV_KEY, Base64.encodeToString(iv, Base64.DEFAULT))
      apply()
    }
  }

  fun clear() {
    prefs.edit().apply {
      remove(SESSION_KEY)
      remove(IV_KEY)
      apply()
    }
  }

  private fun generateKey() {
    val keyGenerator = KeyGenerator.getInstance(
      KeyProperties.KEY_ALGORITHM_AES,
      "AndroidKeyStore"
    )

    val keySpec = KeyGenParameterSpec.Builder(
      keyAlias,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setUserAuthenticationRequired(false)
      .build()

    keyGenerator.init(keySpec)
    keyGenerator.generateKey()
  }

  private fun getSecretKey(): SecretKey {
    return keyStore.getKey(keyAlias, null) as SecretKey
  }

  private fun encrypt(data: String): Pair<ByteArray, ByteArray> {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
    val iv = cipher.iv
    val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    return Pair(encrypted, iv)
  }

  private fun decrypt(encrypted: ByteArray, iv: ByteArray): String {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val spec = GCMParameterSpec(128, iv)
    cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
    val decrypted = cipher.doFinal(encrypted)
    return String(decrypted, Charsets.UTF_8)
  }

  private companion object {
    const val SESSION_KEY = "session"
    const val IV_KEY = "session_iv"
  }
}
