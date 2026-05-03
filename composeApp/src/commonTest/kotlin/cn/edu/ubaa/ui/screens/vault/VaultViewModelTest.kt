package cn.edu.ubaa.ui.screens.vault

import cn.edu.ubaa.model.dto.VaultCipherParamsDto
import cn.edu.ubaa.model.dto.VaultRecordDto
import cn.edu.ubaa.model.dto.VaultSaveRequest
import cn.edu.ubaa.model.dto.VaultSaveResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModelTest {
  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `load keeps first-time vault state locked`() = runTest {
    setMainDispatcher(testScheduler)
    val viewModel =
        VaultViewModel(
            loadVaultRequest = { Result.success(null) },
            saveVaultRequest = { failSave(it) },
        )

    viewModel.load()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.unlocked)
    assertNull(viewModel.uiState.value.record)
    assertEquals(emptyList(), viewModel.uiState.value.entries)
  }

  @Test
  fun `reset returns vault to locked setup state`() = runTest {
    setMainDispatcher(testScheduler)
    val viewModel =
        VaultViewModel(
            loadVaultRequest = { Result.success(existingRecord()) },
            saveVaultRequest = { request ->
              Result.success(
                  VaultSaveResponse(
                      existingRecord(
                          revision = (request.baseRevision ?: 0L) + 1L,
                          cipherText = request.cipherText,
                          params = request.params,
                      )
                  )
              )
            },
            deleteVaultRequest = { Result.success(mapOf("message" to "vault_deleted")) },
        )

    viewModel.resetVault()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.unlocked)
    assertNull(viewModel.uiState.value.record)
    assertEquals("淇濋櫓搴撳凡閲嶇疆", viewModel.uiState.value.message)
  }

  @Test
  fun `reset failure clears loading and exposes error`() = runTest {
    setMainDispatcher(testScheduler)
    val viewModel =
        VaultViewModel(
            loadVaultRequest = { Result.success(existingRecord()) },
            saveVaultRequest = { failSave(it) },
            deleteVaultRequest = { Result.failure(IllegalStateException("delete failed")) },
        )

    viewModel.resetVault()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoading)
    assertEquals("delete failed", viewModel.uiState.value.error)
  }

  private fun existingRecord(
      revision: Long = 1L,
      cipherText: String = "cipher",
      params: VaultCipherParamsDto = defaultParams(),
  ): VaultRecordDto =
      VaultRecordDto(
          revision = revision,
          updatedAt = "2026-05-03T10:00:00Z",
          cipherText = cipherText,
          params = params,
      )

  private fun defaultParams(): VaultCipherParamsDto =
      VaultCipherParamsDto(
          iterations = 600_000,
          salt = "c2FsdA==",
          nonce = "bm9uY2U=",
      )

  private fun failSave(request: VaultSaveRequest): Result<VaultSaveResponse> {
    error("Unexpected save request in test: $request")
  }

  private fun setMainDispatcher(scheduler: TestCoroutineScheduler) {
    Dispatchers.setMain(StandardTestDispatcher(scheduler))
  }
}
