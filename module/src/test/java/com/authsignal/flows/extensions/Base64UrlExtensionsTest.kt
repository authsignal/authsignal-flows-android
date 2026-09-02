package com.authsignal.flows.extensions

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// `android.util.Base64` is a framework class backed by a stub `android.jar`
// in plain JVM unit tests, so this runs under Robolectric to get a real
// (shadowed) implementation instead of a "not mocked" stub.
@RunWith(RobolectricTestRunner::class)
class Base64UrlExtensionsTest {
  @Test
  fun `round trips arbitrary bytes through base64url`() {
    val original = byteArrayOf(0, 1, 2, 3, 4, 5, -1, -2, 127, -128, 42, 99, 10, 11, 12)

    val encoded = original.toBase64UrlString()
    val decoded = encoded.fromBase64Url()

    assertArrayEquals(original, decoded)
  }

  @Test
  fun `encoded string is url safe and unpadded`() {
    // This byte sequence produces '+', '/' and padding in standard base64.
    val original = byteArrayOf(-5, -1, -66)

    val encoded = original.toBase64UrlString()

    assertFalse(encoded.contains("+"))
    assertFalse(encoded.contains("/"))
    assertFalse(encoded.contains("="))
  }

  @Test
  fun `decoding the encoded string returns the original string content`() {
    val original = "authsignal-flows-android"
    val encoded = original.toByteArray().toBase64UrlString()

    val decoded = String(encoded.fromBase64Url())

    assertEquals(original, decoded)
  }
}
