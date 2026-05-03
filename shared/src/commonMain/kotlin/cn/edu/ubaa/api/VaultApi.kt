package cn.edu.ubaa.api

import cn.edu.ubaa.model.dto.VaultRecordDto
import cn.edu.ubaa.model.dto.VaultSaveRequest
import cn.edu.ubaa.model.dto.VaultSaveResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

class VaultApi(private val apiClient: ApiClient = ApiClientProvider.shared) {
  suspend fun getVault(): Result<VaultRecordDto?> {
    return try {
      val response = apiClient.getClient().get("api/v1/vault")
      when (response.status) {
        HttpStatusCode.OK -> Result.success(response.body())
        HttpStatusCode.NotFound -> Result.success(null)
        else -> Result.failure(response.toApiCallException())
      }
    } catch (e: Exception) {
      Result.failure(e.toUserFacingApiException("Vault load failed"))
    }
  }

  suspend fun saveVault(request: VaultSaveRequest): Result<VaultSaveResponse> =
      safeApiCall {
        apiClient.getClient().put("api/v1/vault") {
          contentType(ContentType.Application.Json)
          setBody(request)
        }
      }

  suspend fun deleteVault(): Result<Map<String, String>> =
      safeApiCall { apiClient.getClient().delete("api/v1/vault") }
}
