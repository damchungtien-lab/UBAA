package cn.edu.ubaa.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.ubaa.api.VaultApi
import cn.edu.ubaa.model.dto.VaultPlainEntryDto
import cn.edu.ubaa.model.dto.VaultPlainStateDto
import cn.edu.ubaa.model.dto.VaultRecordDto
import cn.edu.ubaa.model.dto.VaultSaveRequest
import cn.edu.ubaa.model.dto.VaultSaveResponse
import cn.edu.ubaa.vault.VaultCrypto
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VaultUiState(
    val isLoading: Boolean = false,
    val record: VaultRecordDto? = null,
    val unlocked: Boolean = false,
    val entries: List<VaultPlainEntryDto> = emptyList(),
    val error: String? = null,
    val message: String? = null,
)

class VaultViewModel(
    private val loadVaultRequest: suspend () -> Result<VaultRecordDto?> = { VaultApi().getVault() },
    private val saveVaultRequest: suspend (VaultSaveRequest) -> Result<VaultSaveResponse> = {
      VaultApi().saveVault(it)
    },
    private val deleteVaultRequest: suspend () -> Result<Map<String, String>> = {
      VaultApi().deleteVault()
    },
) : ViewModel() {
  private val _uiState = MutableStateFlow(VaultUiState())
  val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()
  private var masterPassword: String? = null

  fun load() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
      loadVaultRequest()
          .onSuccess { record ->
            masterPassword = null
            _uiState.value =
                VaultUiState(
                    isLoading = false,
                    record = record,
                    unlocked = false,
                    entries = emptyList(),
                )
          }
          .onFailure { error ->
            _uiState.value =
                _uiState.value.copy(isLoading = false, error = error.message ?: "淇濋櫓搴撳姞杞藉け璐?")
          }
    }
  }

  fun unlock(password: String) {
    val record = _uiState.value.record ?: return
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
      runCatching { VaultCrypto.decrypt(password, record) }
          .onSuccess { state ->
            masterPassword = password
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    unlocked = true,
                    entries = state.entries,
                    error = null,
                )
          }
          .onFailure {
            _uiState.value =
                _uiState.value.copy(isLoading = false, error = "涓诲瘑鐮佷笉姝ｇ‘鎴栦繚闄╁簱宸叉崯鍧?")
          }
    }
  }

  fun initialize(password: String) {
    masterPassword = password
    _uiState.value =
        _uiState.value.copy(unlocked = true, entries = emptyList(), error = null, message = null)
    save()
  }

  fun upsertEntry(
      id: String?,
      title: String,
      username: String,
      password: String,
      url: String?,
      note: String?,
  ) {
    val now = Clock.System.now().toString()
    val entry =
        VaultPlainEntryDto(
            id = id ?: "vault-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt()}",
            title = title.trim(),
            username = username.trim(),
            password = password,
            url = url?.trim()?.takeIf(String::isNotBlank),
            note = note?.trim()?.takeIf(String::isNotBlank),
            updatedAt = now,
        )
    val current = _uiState.value.entries.filterNot { it.id == entry.id }
    _uiState.value =
        _uiState.value.copy(entries = (current + entry).sortedBy { it.title }, message = null)
  }

  fun deleteEntry(id: String) {
    _uiState.value = _uiState.value.copy(entries = _uiState.value.entries.filterNot { it.id == id })
  }

  fun save() {
    val password = masterPassword
    if (password.isNullOrBlank()) {
      _uiState.value = _uiState.value.copy(error = "璇峰厛瑙ｉ攣淇濋櫓搴?")
      return
    }
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
      val current = _uiState.value
      runCatching {
            VaultCrypto.encrypt(
                masterPassword = password,
                state = VaultPlainStateDto(entries = current.entries),
                baseRevision = current.record?.revision,
            )
          }
          .fold(
              onSuccess = { request ->
                saveVaultRequest(request)
                    .onSuccess { response ->
                      _uiState.value =
                          _uiState.value.copy(
                              isLoading = false,
                              record = response.record,
                              unlocked = true,
                              message = "淇濋櫓搴撳凡淇濆瓨",
                          )
                    }
                    .onFailure { error ->
                      _uiState.value =
                          _uiState.value.copy(
                              isLoading = false,
                              error = error.message ?: "淇濋櫓搴撲繚瀛樺け璐?",
                          )
                    }
              },
              onFailure = { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "淇濋櫓搴撳姞瀵嗗け璐?",
                    )
              },
          )
    }
  }

  fun lock() {
    masterPassword = null
    _uiState.value = _uiState.value.copy(unlocked = false, entries = emptyList(), message = null)
  }

  fun resetVault() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null, message = null)
      deleteVaultRequest()
          .onSuccess {
            masterPassword = null
            _uiState.value =
                VaultUiState(
                    isLoading = false,
                    unlocked = false,
                    message = "淇濋櫓搴撳凡閲嶇疆",
                )
          }
          .onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "淇濋櫓搴撻噸缃け璐?",
                )
          }
    }
  }
}
