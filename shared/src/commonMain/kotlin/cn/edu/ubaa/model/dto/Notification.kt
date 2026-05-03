package cn.edu.ubaa.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppNotificationDto(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val createdAt: String,
    val readAt: String? = null,
    val actionUrl: String? = null,
    val payload: Map<String, String> = emptyMap(),
)

@Serializable
data class AppNotificationsResponse(
    val notifications: List<AppNotificationDto>,
    val unreadCount: Int,
)

@Serializable
data class WebPushSubscriptionDto(
    val endpoint: String,
    val p256dh: String,
    val auth: String,
)

@Serializable data class WebPushSubscriptionResponse(val endpointHash: String)

