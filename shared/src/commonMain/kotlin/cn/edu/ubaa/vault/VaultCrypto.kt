package cn.edu.ubaa.vault

import cn.edu.ubaa.model.dto.VaultCipherParamsDto
import cn.edu.ubaa.model.dto.VaultPlainStateDto
import cn.edu.ubaa.model.dto.VaultRecordDto
import cn.edu.ubaa.model.dto.VaultSaveRequest
import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.PBKDF2
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.random.CryptographyRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
object VaultCrypto {
  const val DEFAULT_ITERATIONS: Int = 600_000

  private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
  }

  suspend fun encrypt(
      masterPassword: String,
      state: VaultPlainStateDto,
      baseRevision: Long?,
      iterations: Int = DEFAULT_ITERATIONS,
  ): VaultSaveRequest {
    require(masterPassword.isNotBlank()) { "Master password is required" }
    val salt = CryptographyRandom.nextBytes(16)
    val nonce = CryptographyRandom.nextBytes(12)
    val keyBytes = deriveKey(masterPassword, salt, iterations)
    val cipherText =
        aesKey(keyBytes)
            .cipher()
            .encryptWithIv(
                iv = nonce,
                plaintext = json.encodeToString(state).encodeToByteArray(),
            )
    return VaultSaveRequest(
        baseRevision = baseRevision,
        cipherText = Base64.encode(cipherText),
        params =
            VaultCipherParamsDto(
                iterations = iterations,
                salt = Base64.encode(salt),
                nonce = Base64.encode(nonce),
            ),
    )
  }

  suspend fun decrypt(
      masterPassword: String,
      record: VaultRecordDto,
  ): VaultPlainStateDto {
    require(masterPassword.isNotBlank()) { "Master password is required" }
    val salt = Base64.decode(record.params.salt)
    val nonce = Base64.decode(record.params.nonce)
    val cipherText = Base64.decode(record.cipherText)
    val keyBytes = deriveKey(masterPassword, salt, record.params.iterations)
    val plaintext =
        aesKey(keyBytes).cipher().decryptWithIv(iv = nonce, ciphertext = cipherText)
    return json.decodeFromString(plaintext.decodeToString())
  }

  private suspend fun deriveKey(
      masterPassword: String,
      salt: ByteArray,
      iterations: Int,
  ): ByteArray =
      CryptographyProvider.Default
          .get(PBKDF2)
          .secretDerivation(
              digest = SHA256,
              iterations = iterations,
              outputSize = 256.bits,
              salt = salt,
          )
          .deriveSecretToByteArray(masterPassword.encodeToByteArray())

  private suspend fun aesKey(keyBytes: ByteArray) =
      CryptographyProvider.Default
          .get(AES.GCM)
          .keyDecoder()
          .decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
}
