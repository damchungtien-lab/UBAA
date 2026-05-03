package cn.edu.ubaa.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

@Serializable
data class SupabaseConfig(
    val url: String,
    val anonKey: String,
)

object SupabaseClient {
  var config: SupabaseConfig? = null

  private val client by lazy {
    HttpClient {
      install(ContentNegotiation) {
        json(
            Json {
              ignoreUnknownKeys = true
              isLenient = true
            }
        )
      }
      defaultRequest {
        config?.let { cfg ->
          url(cfg.url)
          header("apikey", cfg.anonKey)
          header("Authorization", "Bearer ${cfg.anonKey}")
          contentType(ContentType.Application.Json)
        }
      }
    }
  }

  fun configure(url: String, anonKey: String) {
    config = SupabaseConfig(url, anonKey)
  }

  suspend fun insert(table: String, data: JsonObject): Result<Unit> {
    return try {
      val resp =
          client.post("rest/v1/$table") {
            setBody(data)
            header("Prefer", "return=minimal")
            header("Content-Profile", "public")
          }
      if (resp.status == HttpStatusCode.Created || resp.status == HttpStatusCode.NoContent) {
        Result.success(Unit)
      } else {
        Result.failure(Exception("Insert failed: ${resp.status}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun select(
      table: String,
      columns: String = "*",
      filters: Map<String, String> = emptyMap(),
  ): Result<JsonObject> {
    return try {
      val resp =
          client.get("rest/v1/$table") {
            header("Accept-Profile", "public")
            parameter("select", columns)
            filters.forEach { (key, value) -> parameter(key, value) }
          }
      if (resp.status == HttpStatusCode.OK) {
        val body = resp.body<String>()
        val array = Json.parseToJsonElement(body).jsonArray
        // Wrap in {data: [...]} format for compatibility
        val wrapped = buildJsonObject { put("data", array) }
        Result.success(wrapped)
      } else {
        Result.failure(Exception("Select failed: ${resp.status}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun update(table: String, data: JsonObject, filters: Map<String, String>): Result<Unit> {
    return try {
      val resp =
          client.patch("rest/v1/$table") {
            setBody(data)
            header("Prefer", "return=minimal")
            header("Content-Profile", "public")
            filters.forEach { (key, value) -> parameter(key, value) }
          }
      if (resp.status == HttpStatusCode.NoContent) {
        Result.success(Unit)
      } else {
        Result.failure(Exception("Update failed: ${resp.status}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun delete(table: String, filters: Map<String, String>): Result<Unit> {
    return try {
      val resp =
          client.delete("rest/v1/$table") {
            header("Prefer", "return=minimal")
            header("Content-Profile", "public")
            filters.forEach { (key, value) -> parameter(key, value) }
          }
      if (resp.status == HttpStatusCode.NoContent || resp.status == HttpStatusCode.OK) {
        Result.success(Unit)
      } else {
        Result.failure(Exception("Delete failed: ${resp.status}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}

fun buildEqFilter(column: String, value: String): String = "$column=eq.$value"
