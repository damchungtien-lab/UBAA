package cn.edu.ubaa.notifications

import cn.edu.ubaa.auth.JwtAuth.jwtUsername
import cn.edu.ubaa.auth.respondError
import cn.edu.ubaa.model.dto.WebPushSubscriptionDto
import cn.edu.ubaa.model.dto.WebPushSubscriptionResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.notificationRouting(
    storeProvider: () -> NotificationStore = { GlobalNotificationStore.instance },
) {
  val store by lazy(storeProvider)
  route("/api/v1/notifications") {
    get {
      val username = call.jwtUsername ?: return@get call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      call.respond(HttpStatusCode.OK, store.list(username))
    }

    post("/{id}/read") {
      val username =
          call.jwtUsername ?: return@post call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      val id =
          call.parameters["id"]?.takeIf(String::isNotBlank)
              ?: return@post call.respondError(HttpStatusCode.BadRequest, "invalid_request")
      call.respond(HttpStatusCode.OK, store.markRead(username, id))
    }

    post("/push-subscriptions") {
      val username =
          call.jwtUsername ?: return@post call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      val request =
          runCatching { call.receive<WebPushSubscriptionDto>() }
              .getOrElse {
                return@post call.respondError(HttpStatusCode.BadRequest, "invalid_request")
              }
      if (request.endpoint.isBlank() || request.p256dh.isBlank() || request.auth.isBlank()) {
        return@post call.respondError(HttpStatusCode.BadRequest, "invalid_request")
      }
      val endpointHash = store.savePushSubscription(username, request)
      call.respond(HttpStatusCode.OK, WebPushSubscriptionResponse(endpointHash))
    }

    delete("/push-subscriptions/{endpointHash}") {
      val username =
          call.jwtUsername
              ?: return@delete call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      val endpointHash =
          call.parameters["endpointHash"]?.takeIf(String::isNotBlank)
              ?: return@delete call.respondError(HttpStatusCode.BadRequest, "invalid_request")
      store.deletePushSubscription(username, endpointHash)
      call.respond(HttpStatusCode.OK, WebPushSubscriptionResponse(endpointHash))
    }
  }
}
