package cn.edu.ubaa.api

import cn.edu.ubaa.model.dto.AppNotificationsResponse
import cn.edu.ubaa.model.dto.WebPushSubscriptionDto
import cn.edu.ubaa.model.dto.WebPushSubscriptionResponse
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NotificationApi(private val apiClient: ApiClient = ApiClientProvider.shared) {
  suspend fun getNotifications(): Result<AppNotificationsResponse> =
      safeApiCall { apiClient.getClient().get("api/v1/notifications") }

  suspend fun markRead(id: String): Result<AppNotificationsResponse> =
      safeApiCall { apiClient.getClient().post("api/v1/notifications/$id/read") }

  suspend fun savePushSubscription(
      subscription: WebPushSubscriptionDto
  ): Result<WebPushSubscriptionResponse> =
      safeApiCall {
        apiClient.getClient().post("api/v1/notifications/push-subscriptions") {
          contentType(ContentType.Application.Json)
          setBody(subscription)
        }
      }

  suspend fun deletePushSubscription(endpointHash: String): Result<WebPushSubscriptionResponse> =
      safeApiCall {
        apiClient.getClient().delete("api/v1/notifications/push-subscriptions/$endpointHash")
      }
}
