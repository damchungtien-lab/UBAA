package cn.edu.ubaa.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.ubaa.api.VaultEntry
import cn.edu.ubaa.api.VaultStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class VaultUiState(
    val isConfigured: Boolean = false,
    val isUnlocked: Boolean = false,
    val entries: List<VaultEntry> = emptyList(),
    val error: String? = null,
    val message: String? = null,
)

class VaultViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private var currentPassword: String? = null

    init {
        _uiState.value = _uiState.value.copy(isConfigured = VaultStore.isConfigured())
    }

    fun setMasterPassword(password: String) {
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "密码不能为空")
            return
        }
        if (password.length < 4) {
            _uiState.value = _uiState.value.copy(error = "密码至少需要4位")
            return
        }
        VaultStore.setMasterPassword(password)
        currentPassword = password
        _uiState.value = _uiState.value.copy(
            isConfigured = true,
            isUnlocked = true,
            entries = emptyList(),
            error = null,
            message = "密码保管箱已创建",
        )
    }

    fun unlock(password: String) {
        if (!VaultStore.verifyMasterPassword(password)) {
            _uiState.value = _uiState.value.copy(error = "密码错误")
            return
        }
        currentPassword = password
        loadEntries()
    }

    fun lock() {
        currentPassword = null
        _uiState.value = _uiState.value.copy(
            isUnlocked = false,
            entries = emptyList(),
            error = null,
            message = null,
        )
    }

    private fun loadEntries() {
        val password = currentPassword ?: return
        val entries = VaultStore.getAllEntries(password)
        if (entries == null) {
            _uiState.value = _uiState.value.copy(error = "加载数据失败", isUnlocked = false)
            currentPassword = null
            return
        }
        _uiState.value = _uiState.value.copy(
            isUnlocked = true,
            entries = entries,
            error = null,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    fun saveEntry(
        id: String?,
        title: String,
        systemName: String,
        username: String,
        password: String,
        url: String,
        notes: String,
    ): Boolean {
        val masterPassword = currentPassword ?: return false
        if (title.isBlank()) return false
        val now = Clock.System.now().toEpochMilliseconds()
        val entry = VaultEntry(
            id = id ?: Uuid.random().toString(),
            title = title,
            systemName = systemName,
            username = username,
            password = password,
            url = url,
            notes = notes,
            createdAt = if (id == null) now else 0L,
            updatedAt = now,
        )
        val success = if (id == null) {
            VaultStore.addEntry(entry, masterPassword)
        } else {
            VaultStore.updateEntry(entry, masterPassword)
        }
        if (success) {
            loadEntries()
            _uiState.value = _uiState.value.copy(message = "保存成功")
        } else {
            _uiState.value = _uiState.value.copy(error = "保存失败")
        }
        return success
    }

    fun deleteEntry(entryId: String) {
        val masterPassword = currentPassword ?: return
        if (VaultStore.deleteEntry(entryId, masterPassword)) {
            loadEntries()
            _uiState.value = _uiState.value.copy(message = "已删除")
        } else {
            _uiState.value = _uiState.value.copy(error = "删除失败")
        }
    }

    fun changeMasterPassword(oldPassword: String, newPassword: String): Boolean {
        if (VaultStore.changeMasterPassword(oldPassword, newPassword)) {
            currentPassword = newPassword
            _uiState.value = _uiState.value.copy(message = "密码已更改", error = null)
            return true
        }
        _uiState.value = _uiState.value.copy(error = "密码更改失败")
        return false
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetVault() {
        VaultStore.resetVault()
        currentPassword = null
        _uiState.value = VaultUiState(isConfigured = false)
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
