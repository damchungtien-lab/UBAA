package cn.edu.ubaa.ui.screens.bykc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.edu.ubaa.api.AuthTokensStore
import cn.edu.ubaa.api.AutoBookingStore
import cn.edu.ubaa.api.BykcApi
import cn.edu.ubaa.model.dto.AutoBookingTask
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class AutoBookingUiState(
    val tasks: List<AutoBookingTask> = emptyList(),
    val activeTasks: List<AutoBookingTask> = emptyList(),
    val completedTasks: List<AutoBookingTask> = emptyList(),
    val isPolling: Boolean = false,
    val lastPollMessage: String? = null,
    val syncEnabled: Boolean = false,
)

class AutoBookingViewModel(
    private val userId: String = "",
    private val bykcApi: BykcApi = BykcApi(),
) : ViewModel() {
  private val _uiState = MutableStateFlow(AutoBookingUiState())
  val uiState: StateFlow<AutoBookingUiState> = _uiState.asStateFlow()

  private var pollingJob: Job? = null

  init {
    loadTasks()
    syncFromSupabase()
    startPolling()
  }

  private fun loadTasks() {
    val tasks = AutoBookingStore.getAllTasks()
    _uiState.value =
        _uiState.value.copy(
            tasks = tasks,
            activeTasks = tasks.filter { !it.isCompleted },
            completedTasks = tasks.filter { it.isCompleted },
        )
  }

  private fun syncFromSupabase() {
    viewModelScope.launch {
      val tasks = AutoBookingStore.getAllTasks()
      if (tasks.isEmpty()) return@launch
      var changed = false
      for (task in tasks) {
        if (!task.isCompleted) {
          AutoBookingStore.syncTaskCompletionFromSupabase(task.id)
          val updated = AutoBookingStore.getTask(task.id)
          if (updated != null && updated.isCompleted != task.isCompleted) {
            changed = true
          }
        }
      }
      if (changed) loadTasks()
    }
  }

  @OptIn(ExperimentalUuidApi::class)
  fun addAutoBookTask(
      courseId: Long,
      courseName: String,
      coursePosition: String?,
      courseTeacher: String?,
      courseSelectStartDate: kotlinx.datetime.LocalDateTime?,
      courseSelectEndDate: kotlinx.datetime.LocalDateTime?,
  ): Boolean {
    val existingTask = _uiState.value.tasks.find { it.courseId == courseId && !it.isCompleted }
    if (existingTask != null) return false

    val task =
        AutoBookingTask(
            id = Uuid.random().toString(),
            courseId = courseId,
            courseName = courseName,
            coursePosition = coursePosition,
            courseTeacher = courseTeacher,
            courseSelectStartDate = courseSelectStartDate,
            courseSelectEndDate = courseSelectEndDate,
            createdAt = Clock.System.now().toEpochMilliseconds(),
        )

    // Save locally first for immediate UI update
    AutoBookingStore.saveTask(task)

    // Sync to Supabase for server-side execution
    val tokens = AuthTokensStore.get()
    if (tokens != null && userId.isNotBlank()) {
      viewModelScope.launch {
        AutoBookingStore.saveTaskWithSupabase(
            task = task,
            userId = userId,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
        )
        _uiState.value = _uiState.value.copy(syncEnabled = true)
      }
    }

    loadTasks()
    return true
  }

  fun removeTask(taskId: String) {
    AutoBookingStore.removeTask(taskId)
    viewModelScope.launch { AutoBookingStore.removeTaskWithSupabase(taskId) }
    loadTasks()
  }

  fun startPolling() {
    if (pollingJob?.isActive == true) return
    pollingJob =
        viewModelScope.launch {
          _uiState.value = _uiState.value.copy(isPolling = true)
          while (isActive) {
            checkAndExecuteTasks()
            // Sync from Supabase every 30 seconds (in case server executed tasks)
            syncFromSupabase()
            delay(10_000L)
          }
        }
  }

  fun stopPolling() {
    pollingJob?.cancel()
    pollingJob = null
    _uiState.value = _uiState.value.copy(isPolling = false)
  }

  private suspend fun checkAndExecuteTasks() {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val pendingTasks = AutoBookingStore.getAllTasks().filter { !it.isCompleted }

    if (pendingTasks.isEmpty()) {
      stopPolling()
      return
    }

    for (task in pendingTasks) {
      val startTime = task.courseSelectStartDate ?: continue
      val endTime = task.courseSelectEndDate

      if (now < startTime) continue
      if (endTime != null && now > endTime) {
        AutoBookingStore.markTaskFailed(task.id, "选课时段已过")
        loadTasks()
        continue
      }

      val result = bykcApi.selectCourse(task.courseId)
      result.fold(
          onSuccess = {
            AutoBookingStore.updateTaskCompletion(task.id, true, it.message)
            _uiState.value = _uiState.value.copy(lastPollMessage = "自动选课成功: ${task.courseName}")
            loadTasks()
          },
          onFailure = {
            AutoBookingStore.updateTaskCompletion(task.id, false, it.message)
            _uiState.value = _uiState.value.copy(lastPollMessage = "自动选课失败: ${task.courseName}")
            loadTasks()
          },
      )
    }
  }

  override fun onCleared() {
    super.onCleared()
    stopPolling()
  }
}
