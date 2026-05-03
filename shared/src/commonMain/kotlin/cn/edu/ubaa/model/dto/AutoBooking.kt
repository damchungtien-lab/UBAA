package cn.edu.ubaa.model.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class AutoBookingTask(
    val id: String,
    val courseId: Long,
    val courseName: String,
    val coursePosition: String? = null,
    val courseTeacher: String? = null,
    val courseSelectStartDate: LocalDateTime? = null,
    val courseSelectEndDate: LocalDateTime? = null,
    val createdAt: Long,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val resultMessage: String? = null,
)
