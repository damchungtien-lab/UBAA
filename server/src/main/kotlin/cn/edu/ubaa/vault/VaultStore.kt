package cn.edu.ubaa.vault

import cn.edu.ubaa.auth.GlobalDistributedLockManager
import cn.edu.ubaa.auth.GlobalRedisRuntime
import cn.edu.ubaa.auth.RedisDistributedLockManager
import cn.edu.ubaa.model.dto.VaultRecordDto
import cn.edu.ubaa.model.dto.VaultSaveRequest
import cn.edu.ubaa.model.dto.VaultSaveResponse
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlin.time.Clock
import kotlinx.coroutines.future.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class VaultRevisionConflictException : Exception("Vault revision conflict")

interface VaultStore {
  suspend fun get(username: String): VaultRecordDto?

  suspend fun save(username: String, request: VaultSaveRequest): VaultSaveResponse

  suspend fun delete(username: String)
}

class RedisVaultStore(
    private val redis: RedisAsyncCommands<String, String> = GlobalRedisRuntime.instance.asyncCommands,
    private val lockManager: RedisDistributedLockManager = GlobalDistributedLockManager.instance,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : VaultStore {
  override suspend fun get(username: String): VaultRecordDto? =
      redis.get(key(username)).await()?.let { raw ->
        runCatching { json.decodeFromString<VaultRecordDto>(raw) }.getOrNull()
      }

  override suspend fun save(username: String, request: VaultSaveRequest): VaultSaveResponse =
      lockManager.withLock("vault", username) {
        val current = get(username)
        val expectedBase = request.baseRevision
        if (current == null) {
          if (expectedBase != null) throw VaultRevisionConflictException()
        } else if (expectedBase != current.revision) {
          throw VaultRevisionConflictException()
        }

        val record =
            VaultRecordDto(
                revision = (current?.revision ?: 0L) + 1L,
                updatedAt = Clock.System.now().toString(),
                cipherText = request.cipherText,
                params = request.params,
            )
        redis.set(key(username), json.encodeToString(record)).await()
        VaultSaveResponse(record)
      }

  override suspend fun delete(username: String) {
    redis.del(key(username)).await()
  }

  private fun key(username: String): String = "vault:user:$username"
}

object GlobalVaultStore {
  val instance: VaultStore by lazy { RedisVaultStore() }
}
