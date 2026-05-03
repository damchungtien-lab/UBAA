package cn.edu.ubaa.vault

import cn.edu.ubaa.auth.JwtAuth.jwtUsername
import cn.edu.ubaa.auth.respondError
import cn.edu.ubaa.model.dto.VaultSaveRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.vaultRouting(
    storeProvider: () -> VaultStore = { GlobalVaultStore.instance },
) {
  val store by lazy(storeProvider)
  route("/api/v1/vault") {
    get {
      val username = call.jwtUsername ?: return@get call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      val record = store.get(username)
      if (record == null) {
        call.respondError(HttpStatusCode.NotFound, "vault_not_initialized", "密码保险库尚未初始化")
      } else {
        call.respond(HttpStatusCode.OK, record)
      }
    }

    put {
      val username = call.jwtUsername ?: return@put call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      val request =
          runCatching { call.receive<VaultSaveRequest>() }
              .getOrElse {
                return@put call.respondError(HttpStatusCode.BadRequest, "invalid_request")
              }
      if (
          request.cipherText.isBlank() ||
              request.params.salt.isBlank() ||
              request.params.nonce.isBlank() ||
              request.params.iterations < 100_000
      ) {
        return@put call.respondError(HttpStatusCode.BadRequest, "invalid_request")
      }
      try {
        call.respond(HttpStatusCode.OK, store.save(username, request))
      } catch (_: VaultRevisionConflictException) {
        call.respondError(HttpStatusCode.Conflict, "vault_revision_conflict", "保险库已有更新，请刷新后再保存")
      }
    }

    delete {
      val username =
          call.jwtUsername
              ?: return@delete call.respondError(HttpStatusCode.Unauthorized, "invalid_token")
      store.delete(username)
      call.respond(HttpStatusCode.OK, mapOf("message" to "vault_deleted"))
    }
  }
}
