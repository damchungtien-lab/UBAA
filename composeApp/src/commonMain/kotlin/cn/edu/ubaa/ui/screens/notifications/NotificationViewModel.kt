package cn.edu.ubaa.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.ubaa.api.NotificationApi
import cn.edu.ubaa.model.dto.AppNotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<AppNotificationDto> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null,
)

class NotificationViewModel(
    private val api: NotificationApi = NotificationApi(),
) : ViewModel() {
  private var loadedOnce = false
  private val _uiState = MutableStateFlow(NotificationUiState())
  val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

  fun ensureLoaded(forceRefresh: Boolean = false) {
    if (!forceRefresh && loadedOnce) return
    refresh()
  }

  fun refresh() {
    loadedOnce = true
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      api.getNotifications()
          .onSuccess { response ->
            _uiState.value =
                NotificationUiState(
                    isLoading = false,
                    notifications = response.notifications,
                    unreadCount = response.unreadCount,
                )
          }
          .onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "通知加载失败",
                )
          }
    }
  }

  fun markRead(id: String) {
    viewModelScope.launch {
      api.markRead(id).onSuccess { response ->
        _uiState.value =
            _uiState.value.copy(
                notifications = response.notifications,
                unreadCount = response.unreadCount,
                error = null,
            )
      }
    }
  }
}
