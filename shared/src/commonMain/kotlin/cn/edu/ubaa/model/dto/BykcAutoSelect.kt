package cn.edu.ubaa.model.dto

import kotlinx.serialization.Serializable

@Serializable
enum class BykcAutoSelectJobStatus {
  PENDING,
  RUNNING,
  SUCCEEDED,
  FAILED,
  CANCELLED,
}

@Serializable
data class BykcAutoSelectJobDto(
    val id: String,
    val courseId: Long,
    val courseName: String,
    val scheduledAt: String,
    val status: BykcAutoSelectJobStatus,
    val createdAt: String,
    val updatedAt: String,
    val attempts: Int = 0,
    val message: String? = null,
)

@Serializable data class BykcAutoSelectJobsResponse(val jobs: List<BykcAutoSelectJobDto>)

@Serializable data class BykcAutoSelectRequest(val scheduledAt: String? = null)

