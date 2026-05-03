package cn.edu.ubaa.api

import cn.edu.ubaa.model.dto.AutoBookingTask
import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

object AutoBookingStore {
  private const val KEY_PREFIX = "auto_booking_"
  private const val TASK_LIST_KEY = "auto_booking_task_list"

  private var _settings: Settings? = null
  var settings: Settings
    get() = _settings ?: Settings().also { _settings = it }
    set(value) {
      _settings = value
    }

  private val json = Json { ignoreUnknownKeys = true }

  fun getAllTasks(): List<AutoBookingTask> {
    val taskIds =
        settings
            .getStringOrNull(TASK_LIST_KEY)
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() } ?: return emptyList()
    return taskIds.mapNotNull { id ->
      settings.getStringOrNull("$KEY_PREFIX$id")?.let {
        try {
          json.decodeFromString<AutoBookingTask>(it)
        } catch (_: Exception) {
          null
        }
      }
    }
  }

  fun getTask(id: String): AutoBookingTask? {
    return settings.getStringOrNull("$KEY_PREFIX$id")?.let {
      try {
        json.decodeFromString<AutoBookingTask>(it)
      } catch (_: Exception) {
        null
      }
    }
  }

  fun saveTask(task: AutoBookingTask) {
    settings.putString("$KEY_PREFIX${task.id}", json.encodeToString(task))
    val taskIds = getAllTaskIds()
    if (task.id !in taskIds) {
      settings.putString(TASK_LIST_KEY, (taskIds + task.id).joinToString(","))
    }
  }

  /** 保存任务到本地和 Supabase。 Supabase 用于服务端定时执行；本地用于即时 UI 更新和离线访问。 */
  suspend fun saveTaskWithSupabase(
      task: AutoBookingTask,
      userId: String,
      accessToken: String,
      refreshToken: String,
  ) {
    saveTask(task) // 本地存储

    // 同步到 Supabase
    try {
      val row = buildJsonObject {
        put("id", task.id)
        put("user_id", userId)
        put("course_id", task.courseId)
        put("course_name", task.courseName)
        put("course_position", task.coursePosition ?: "")
        put("course_teacher", task.courseTeacher ?: "")
        put("course_select_start_date", task.courseSelectStartDate?.toString() ?: "")
        put("course_select_end_date", task.courseSelectEndDate?.toString() ?: "")
        put("access_token", accessToken)
        put("refresh_token", refreshToken)
        put("is_completed", false)
        put("is_failed", false)
      }
      SupabaseClient.insert("auto_booking_tasks", row)
    } catch (_: Exception) {
      // Supabase 同步失败不影响本地存储
    }
  }

  suspend fun syncTaskCompletionFromSupabase(taskId: String) {
    try {
      val result =
          SupabaseClient.select("auto_booking_tasks", filters = mapOf("id" to "eq.$taskId"))
      result.onSuccess { data ->
        val rows = data["data"]?.jsonArray
        val row = rows?.firstOrNull()?.jsonObject ?: return
        val isCompleted = row["is_completed"]?.jsonPrimitive?.boolean ?: return
        val isFailed = row["is_failed"]?.jsonPrimitive?.boolean ?: false
        val resultMessage = row["result_message"]?.jsonPrimitive?.content

        val task = getTask(taskId) ?: return
        saveTask(
            task.copy(
                isCompleted = isCompleted,
                isFailed = isFailed,
                resultMessage = resultMessage,
            )
        )
      }
    } catch (_: Exception) {}
  }

  fun updateTaskCompletion(taskId: String, success: Boolean, message: String?) {
    val task = getTask(taskId) ?: return
    saveTask(task.copy(isCompleted = true, isFailed = !success, resultMessage = message))
  }

  fun removeTask(id: String) {
    settings.remove("$KEY_PREFIX$id")
    val taskIds = getAllTaskIds().filter { it != id }
    settings.putString(TASK_LIST_KEY, taskIds.joinToString(","))
  }

  suspend fun removeTaskWithSupabase(id: String) {
    removeTask(id)
    try {
      SupabaseClient.delete("auto_booking_tasks", mapOf("id" to "eq.$id"))
    } catch (_: Exception) {}
  }

  fun markTaskFailed(id: String, message: String) {
    val task = getTask(id) ?: return
    saveTask(task.copy(isCompleted = true, isFailed = true, resultMessage = message))
  }

  fun clearAllTasks() {
    getAllTaskIds().forEach { settings.remove("$KEY_PREFIX$it") }
    settings.remove(TASK_LIST_KEY)
  }

  private fun getAllTaskIds(): List<String> {
    return settings
        .getStringOrNull(TASK_LIST_KEY)
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() } ?: emptyList()
  }
}
