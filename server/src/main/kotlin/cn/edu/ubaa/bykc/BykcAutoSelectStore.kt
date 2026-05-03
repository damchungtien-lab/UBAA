package cn.edu.ubaa.bykc

import cn.edu.ubaa.auth.GlobalRedisRuntime
import cn.edu.ubaa.model.dto.BykcAutoSelectJobDto
import cn.edu.ubaa.model.dto.BykcAutoSelectJobStatus
import cn.edu.ubaa.model.dto.BykcAutoSelectJobsResponse
import cn.edu.ubaa.utils.sha256Hex
import io.lettuce.core.Limit
import io.lettuce.core.Range
import io.lettuce.core.api.async.RedisAsyncCommands
import kotlin.time.Clock
import kotlinx.coroutines.future.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface BykcAutoSelectStore {
  suspend fun listJobs(username: String): BykcAutoSelectJobsResponse

  suspend fun upsertJob(
      username: String,
      courseId: Long,
      courseName: String,
      scheduledAt: String,
      dueAtEpochMillis: Long,
  ): BykcAutoSelectJobDto

  suspend fun cancelJob(username: String, courseId: Long): BykcAutoSelectJobDto?

  suspend fun claimDueJobs(nowEpochMillis: Long, limit: Int = 20): List<BykcAutoSelectJobRecord>

  suspend fun markSucceeded(job: BykcAutoSelectJobRecord, message: String): BykcAutoSelectJobDto

  suspend fun markFailed(job: BykcAutoSelectJobRecord, message: String): BykcAutoSelectJobDto

  suspend fun rescheduleRetry(
      job: BykcAutoSelectJobRecord,
      message: String,
      dueAtEpochMillis: Long,
  ): BykcAutoSelectJobDto
}

@Serializable
data class BykcAutoSelectJobRecord(
    val id: String,
    val username: String,
    val courseId: Long,
    val courseName: String,
    val scheduledAt: String,
    val dueAtEpochMillis: Long,
    val status: BykcAutoSelectJobStatus,
    val createdAt: String,
    val updatedAt: String,
    val attempts: Int = 0,
    val message: String? = null,
) {
  fun toDto(): BykcAutoSelectJobDto =
      BykcAutoSelectJobDto(
          id = id,
          courseId = courseId,
          courseName = courseName,
          scheduledAt = scheduledAt,
          status = status,
          createdAt = createdAt,
          updatedAt = updatedAt,
          attempts = attempts,
          message = message,
      )
}

class RedisBykcAutoSelectStore(
    private val redis: RedisAsyncCommands<String, String> = GlobalRedisRuntime.instance.asyncCommands,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : BykcAutoSelectStore {
  override suspend fun listJobs(username: String): BykcAutoSelectJobsResponse {
    val ids = redis.smembers(userIndexKey(username)).await().orEmpty()
    val jobs =
        ids.mapNotNull { readJob(it) }
            .filter { it.username == username }
            .sortedWith(compareBy<BykcAutoSelectJobRecord> { it.dueAtEpochMillis }.thenBy { it.createdAt })
            .map { it.toDto() }
    return BykcAutoSelectJobsResponse(jobs)
  }

  override suspend fun upsertJob(
      username: String,
      courseId: Long,
      courseName: String,
      scheduledAt: String,
      dueAtEpochMillis: Long,
  ): BykcAutoSelectJobDto {
    val id = jobId(username, courseId)
    val now = Clock.System.now().toString()
    val existing = readJob(id)
    val record =
        BykcAutoSelectJobRecord(
            id = id,
            username = username,
            courseId = courseId,
            courseName = courseName,
            scheduledAt = scheduledAt,
            dueAtEpochMillis = dueAtEpochMillis,
            status = BykcAutoSelectJobStatus.PENDING,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            attempts = 0,
            message = null,
        )
    writeJob(record)
    redis.sadd(userIndexKey(username), id).await()
    redis.set(courseIndexKey(username, courseId), id).await()
    return record.toDto()
  }

  override suspend fun cancelJob(username: String, courseId: Long): BykcAutoSelectJobDto? {
    val id = redis.get(courseIndexKey(username, courseId)).await() ?: return null
    val existing = readJob(id) ?: return null
    if (existing.username != username) return null
    val updated =
        existing.copy(
            status = BykcAutoSelectJobStatus.CANCELLED,
            updatedAt = Clock.System.now().toString(),
            message = "用户已取消自动抢课",
        )
    writeJob(updated)
    redis.del(courseIndexKey(username, courseId)).await()
    return updated.toDto()
  }

  override suspend fun claimDueJobs(
      nowEpochMillis: Long,
      limit: Int,
  ): List<BykcAutoSelectJobRecord> {
    val ids =
        redis
            .zrangebyscore(
                dueKey,
                Range.create(0.0, nowEpochMillis.toDouble()),
                Limit.create(0L, limit.toLong()),
            )
            .await()
            .orEmpty()
    val claimed = mutableListOf<BykcAutoSelectJobRecord>()
    ids.forEach { id ->
      redis.zrem(dueKey, id).await()
      val existing = readJob(id) ?: return@forEach
      if (
          existing.status != BykcAutoSelectJobStatus.PENDING ||
              existing.dueAtEpochMillis > nowEpochMillis
      ) {
        return@forEach
      }
      val running =
          existing.copy(
              status = BykcAutoSelectJobStatus.RUNNING,
              attempts = existing.attempts + 1,
              updatedAt = Clock.System.now().toString(),
          )
      writeJob(running)
      claimed += running
    }
    return claimed
  }

  override suspend fun markSucceeded(
      job: BykcAutoSelectJobRecord,
      message: String,
  ): BykcAutoSelectJobDto {
    val updated =
        job.copy(
            status = BykcAutoSelectJobStatus.SUCCEEDED,
            updatedAt = Clock.System.now().toString(),
            message = message,
        )
    writeJob(updated)
    redis.del(courseIndexKey(job.username, job.courseId)).await()
    return updated.toDto()
  }

  override suspend fun markFailed(
      job: BykcAutoSelectJobRecord,
      message: String,
  ): BykcAutoSelectJobDto {
    val updated =
        job.copy(
            status = BykcAutoSelectJobStatus.FAILED,
            updatedAt = Clock.System.now().toString(),
            message = message,
        )
    writeJob(updated)
    redis.del(courseIndexKey(job.username, job.courseId)).await()
    return updated.toDto()
  }

  override suspend fun rescheduleRetry(
      job: BykcAutoSelectJobRecord,
      message: String,
      dueAtEpochMillis: Long,
  ): BykcAutoSelectJobDto {
    val updated =
        job.copy(
            status = BykcAutoSelectJobStatus.PENDING,
            dueAtEpochMillis = dueAtEpochMillis,
            updatedAt = Clock.System.now().toString(),
            message = message,
        )
    writeJob(updated)
    return updated.toDto()
  }

  private suspend fun writeJob(record: BykcAutoSelectJobRecord) {
    redis.set(jobKey(record.id), json.encodeToString(record)).await()
    if (record.status == BykcAutoSelectJobStatus.PENDING) {
      redis.zadd(dueKey, record.dueAtEpochMillis.toDouble(), record.id).await()
    } else {
      redis.zrem(dueKey, record.id).await()
    }
  }

  private suspend fun readJob(id: String): BykcAutoSelectJobRecord? =
      redis.get(jobKey(id)).await()?.let { raw ->
        runCatching { json.decodeFromString<BykcAutoSelectJobRecord>(raw) }.getOrNull()
      }

  private fun jobId(username: String, courseId: Long): String = sha256Hex("$username:$courseId")

  private fun jobKey(id: String): String = "bykc:auto-select:job:$id"

  private fun userIndexKey(username: String): String = "bykc:auto-select:user:$username"

  private fun courseIndexKey(username: String, courseId: Long): String =
      "bykc:auto-select:course:$username:$courseId"

  private companion object {
    private const val dueKey = "bykc:auto-select:due"
  }
}

object GlobalBykcAutoSelectStore {
  val instance: BykcAutoSelectStore by lazy { RedisBykcAutoSelectStore() }
}
