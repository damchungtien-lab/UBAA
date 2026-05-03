@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package cn.edu.ubaa.api

import kotlin.io.encoding.Base64

object VaultCrypto {
  private const val KEY_LENGTH = 32

  fun encrypt(plainText: String, password: String): String {
    val key = deriveKey(password, KEY_LENGTH)
    val data = plainText.encodeToByteArray()
    val encrypted =
        data.mapIndexed { i, b -> (b.toInt() xor key[i % key.size].toInt()).toByte() }.toByteArray()
    return encodeBase64(encrypted)
  }

  fun decrypt(encryptedBase64: String, password: String): String {
    val key = deriveKey(password, KEY_LENGTH)
    val data = decodeBase64(encryptedBase64)
    val decrypted =
        data.mapIndexed { i, b -> (b.toInt() xor key[i % key.size].toInt()).toByte() }.toByteArray()
    return decrypted.decodeToString()
  }

  fun hashPassword(password: String): String {
    val key = deriveKey(password, KEY_LENGTH)
    return encodeBase64(key)
  }

  private fun deriveKey(password: String, length: Int): ByteArray {
    val bytes = password.encodeToByteArray()
    val result = ByteArray(length)
    var hash = 0
    for (i in 0 until length * 4) {
      hash = ((hash shl 5) - hash) + bytes[i % bytes.size].toInt() + i * 0x9E3779B9.toInt()
      hash = hash xor (hash ushr 16)
      if (i < length) {
        result[i] = ((hash xor (hash ushr 8)) and 0xFF).toByte()
      }
    }
    return result
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun encodeBase64(data: ByteArray): String = Base64.encode(data)

  @OptIn(ExperimentalEncodingApi::class)
  private fun decodeBase64(data: String): ByteArray = Base64.decode(data)
}
