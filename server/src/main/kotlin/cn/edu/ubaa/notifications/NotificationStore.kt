package cn.edu.ubaa.notifications

import cn.edu.ubaa.auth.GlobalRedisRuntime
import cn.edu.ubaa.model.dto.AppNotificationDto
import cn.edu.ubaa.model.dto.AppNotificationsResponse
import cn.edu.ubaa.model.dto.WebPushSubscriptionDto
import cn.edu.ubaa.utils.sha256Base64Url
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlin.time.Clock
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface NotificationStore {
  suspend fun list(username: String): AppNotificationsResponse

  suspend fun append(
      username: String,
      type: String,
      title: String,
      body: String,
      actionUrl: String? = null,
      payload: Map<String, String> = emptyMap(),
  ): AppNotificationDto

  suspend fun markRead(username: String, id: String): AppNotificationsResponse

  suspend fun savePushSubscription(
      username: String,
      subscription: WebPushSubscriptionDto,
  ): String

  suspend fun deletePushSubscription(username: String, endpointHash: String)

  suspend fun pushSubscriptions(username: String): List<StoredPushSubscription>
}

@Serializable
data class StoredPushSubscription(
    val endpointHash: String,
    val endpoint: String,
    val p256dh: String,
    val auth: String,
    val createdAt: String,
)

class RedisNotificationStore(
    private val redis: RedisAsyncCommands<String, String> =
        GlobalRedisRuntime.instance.asyncCommands,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val maxNotifications: Int = 100,
) : NotificationStore {
  override suspend fun list(username: String): AppNotificationsResponse =
      readNotifications(username).toResponse()

  override suspend fun append(
      username: String,
      type: String,
      title: String,
      body: String,
      actionUrl: String?,
      payload: Map<String, String>,
  ): AppNotificationDto {
    val now = Clock.System.now().toString()
    val notification =
        AppNotificationDto(
            id = sha256Base64Url("$username:$now:$type:$title:$body"),
            type = type,
            title = title,
            body = body,
            createdAt = now,
            actionUrl = actionUrl,
            payload = payload,
        )
    val updated = (listOf(notification) + readNotifications(username)).take(maxNotifications)
    redis.set(notificationKey(username), json.encodeToString(updated)).await()
    return notification
  }

  override suspend fun markRead(username: String, id: String): AppNotificationsResponse {
    val now = Clock.System.now().toString()
    val updated =
        readNotifications(username).map { notification ->
          if (notification.id == id && notification.readAt == null) {
            notification.copy(readAt = now)
          } else {
            notification
          }
        }
    redis.set(notificationKey(username), json.encodeToString(updated)).await()
    return updated.toResponse()
  }

  override suspend fun savePushSubscription(
      username: String,
      subscription: WebPushSubscriptionDto,
  ): String {
    val endpointHash = sha256Base64Url(subscription.endpoint)
    val existing = pushSubscriptions(username).filterNot { it.endpointHash == endpointHash }
    val updated =
        existing +
            StoredPushSubscription(
                endpointHash = endpointHash,
                endpoint = subscription.endpoint,
                p256dh = subscription.p256dh,
                auth = subscription.auth,
                createdAt = Clock.System.now().toString(),
            )
    redis.set(pushKey(username), json.encodeToString(updated)).await()
    return endpointHash
  }

  override suspend fun deletePushSubscription(username: String, endpointHash: String) {
    val updated = pushSubscriptions(username).filterNot { it.endpointHash == endpointHash }
    redis.set(pushKey(username), json.encodeToString(updated)).await()
  }

  override suspend fun pushSubscriptions(username: String): List<StoredPushSubscription> =
      redis.get(pushKey(username)).await()?.let { raw ->
        runCatching { json.decodeFromString<List<StoredPushSubscription>>(raw) }.getOrNull()
      } ?: emptyList()

  private suspend fun readNotifications(username: String): List<AppNotificationDto> =
      redis.get(notificationKey(username)).await()?.let { raw ->
        runCatching { json.decodeFromString<List<AppNotificationDto>>(raw) }.getOrNull()
      } ?: emptyList()

  private fun List<AppNotificationDto>.toResponse(): AppNotificationsResponse =
      AppNotificationsResponse(
          notifications = this,
          unreadCount = count { it.readAt == null },
      )

  private fun notificationKey(username: String): String = "notifications:user:$username"

  private fun pushKey(username: String): String = "notifications:push:$username"
}

object GlobalNotificationStore {
  val instance: NotificationStore by lazy { RedisNotificationStore() }
}
