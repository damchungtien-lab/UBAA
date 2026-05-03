package cn.edu.ubaa

import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class NewProtectedRoutesTest {
  @Test
  fun newProtectedRoutesRequireJwt() = testApplication {
    application { module() }

    assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/vault").status)
    assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/notifications").status)
    assertEquals(
        HttpStatusCode.Unauthorized,
        client.get("/api/v1/bykc/auto-select/jobs").status,
    )
    assertEquals(
        HttpStatusCode.Unauthorized,
        client.post("/api/v1/bykc/courses/1/auto-select").status,
    )
    assertEquals(
        HttpStatusCode.Unauthorized,
        client.delete("/api/v1/bykc/courses/1/auto-select").status,
    )
  }
}
