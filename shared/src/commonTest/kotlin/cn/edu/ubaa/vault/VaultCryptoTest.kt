package cn.edu.ubaa.vault

import cn.edu.ubaa.model.dto.VaultPlainEntryDto
import cn.edu.ubaa.model.dto.VaultPlainStateDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class VaultCryptoTest {
  @Test
  fun vaultCryptoRoundTrip() = runTest {
    val state =
        VaultPlainStateDto(
            entries =
                listOf(
                    VaultPlainEntryDto(
                        id = "entry-1",
                        title = "VPN",
                        username = "student",
                        password = "correct horse",
                        url = "https://example.com",
                        note = "campus",
                        updatedAt = "2026-05-03T10:00:00Z",
                    )
                )
        )

    val request =
        VaultCrypto.encrypt(
            masterPassword = "master-password",
            state = state,
            baseRevision = null,
            iterations = 10_000,
        )
    val decrypted =
        VaultCrypto.decrypt(
            masterPassword = "master-password",
            record =
                cn.edu.ubaa.model.dto.VaultRecordDto(
                    revision = 1,
                    updatedAt = "2026-05-03T10:01:00Z",
                    cipherText = request.cipherText,
                    params = request.params,
                ),
        )

    assertEquals(state, decrypted)
  }

  @Test
  fun vaultCryptoRejectsWrongPassword() = runTest {
    val request =
        VaultCrypto.encrypt(
            masterPassword = "master-password",
            state = VaultPlainStateDto(),
            baseRevision = null,
            iterations = 10_000,
        )
    val result = runCatching {
      VaultCrypto.decrypt(
          masterPassword = "wrong-password",
          record =
              cn.edu.ubaa.model.dto.VaultRecordDto(
                  revision = 1,
                  updatedAt = "2026-05-03T10:01:00Z",
                  cipherText = request.cipherText,
                  params = request.params,
              ),
      )
    }
    assertTrue(result.isFailure)
  }
}
