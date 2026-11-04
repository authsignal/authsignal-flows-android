package com.authsignal.flows.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class PinManager(context: Context) {
  private val service = "authsignal_pin"
  private val keyAlias = "authsignal_pin_key"
  private val prefs = context.getSharedPreferences(service, Context.MODE_PRIVATE)

  private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
    load(null)
  }

  init {
    if (!keyStore.containsAlias(keyAlias)) {
      generateKey()
    }
  }

  fun createPin(pin: String, username: String) {
    val (encrypted, iv) = encrypt(pin)

    prefs.edit().apply {
      putString("${username}_pin", Base64.encodeToString(encrypted, Base64.DEFAULT))
      putString("${username}_iv", Base64.encodeToString(iv, Base64.DEFAULT))
      apply()
    }
  }

  fun validatePin(pin: String, username: String): Boolean {
    val encryptedPin = prefs.getString("${username}_pin", null) ?: return false
    val iv = prefs.getString("${username}_iv", null) ?: return false

    return try {
      val encrypted = Base64.decode(encryptedPin, Base64.DEFAULT)
      val ivBytes = Base64.decode(iv, Base64.DEFAULT)

      decrypt(encrypted, ivBytes) == pin.trim()
    } catch (e: Exception) {
      false
    }
  }

  fun getAllUsernames(): List<String> {
    return prefs.all.keys
      .filter { it.endsWith("_pin") }
      .map { it.removeSuffix("_pin") }
  }

  fun deletePin(username: String) {
    prefs.edit().apply {
      remove("${username}_pin")
      remove("${username}_iv")
      apply()
    }
  }

  fun validateFormat(pin: String): Boolean {
    return pin.length >= 4 && pin.all { it.isDigit() }
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
}
