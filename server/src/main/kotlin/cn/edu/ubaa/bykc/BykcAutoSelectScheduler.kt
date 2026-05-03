package cn.edu.ubaa.bykc

import cn.edu.ubaa.auth.GlobalDistributedLockManager
import cn.edu.ubaa.auth.RedisDistributedLockManager
import cn.edu.ubaa.notifications.GlobalNotificationStore
import cn.edu.ubaa.notifications.GlobalPushNotificationDispatcher
import cn.edu.ubaa.notifications.NotificationStore
import cn.edu.ubaa.notifications.PushNotificationDispatcher
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class BykcAutoSelectScheduler(
    private val storeProvider: () -> BykcAutoSelectStore = { GlobalBykcAutoSelectStore.instance },
    private val bykcServiceProvider: () -> BykcService = { GlobalBykcService.instance },
    private val notificationStoreProvider: () -> NotificationStore = { GlobalNotificationStore.instance },
    private val pushDispatcherProvider: () -> PushNotificationDispatcher = { GlobalPushNotificationDispatcher.instance },
    private val lockManagerProvider: () -> RedisDistributedLockManager = { GlobalDistributedLockManager.instance },
    private val interval: Duration = 10.seconds,
    private val maxAttempts: Int = 3,
) {
  private val log = LoggerFactory.getLogger(BykcAutoSelectScheduler::class.java)
  private var loopJob: Job? = null
  private val store by lazy(storeProvider)
  private val bykcService by lazy(bykcServiceProvider)
  private val notificationStore by lazy(notificationStoreProvider)
  private val pushDispatcher by lazy(pushDispatcherProvider)
  private val lockManager by lazy(lockManagerProvider)

  fun start(scope: CoroutineScope) {
    if (loopJob != null) return
    loopJob =
        scope.launch {
          while (isActive) {
            runCatching { runOnce() }
                .onFailure { log.warn("BYKC auto-select scheduler tick failed", it) }
            delay(interval)
          }
        }
  }

  suspend fun runOnce(nowEpochMillis: Long = Clock.System.now().toEpochMilliseconds()) {
    val jobs =
        lockManager.withLock("bykc-auto-select", "due") {
          store.claimDueJobs(nowEpochMillis = nowEpochMillis)
        }
    jobs.forEach { job ->
      runCatching { executeJob(job) }
          .onFailure { error ->
            log.warn("BYKC auto-select job {} failed unexpectedly", job.id, error)
            completeFailure(job, error.message ?: "自动抢课执行失败")
          }
    }
  }

  private suspend fun executeJob(job: BykcAutoSelectJobRecord) {
    bykcService.selectCourse(job.username, job.courseId).fold(
        onSuccess = { message ->
          val finalMessage = message.ifBlank { "选课成功" }
          store.markSucceeded(job, finalMessage)
          notify(
              job = job,
              type = "bykc_auto_select_succeeded",
              title = "博雅抢课成功",
              body = "${job.courseName} 已完成自动选课。",
          )
        },
        onFailure = { error ->
          val message = error.message?.takeIf(String::isNotBlank) ?: "选课失败，请稍后重试"
          if (job.attempts < maxAttempts && !message.looksLikeSessionExpired()) {
            val retryAt = Clock.System.now().toEpochMilliseconds() + 30_000L
            store.rescheduleRetry(job, "第 ${job.attempts} 次尝试失败，稍后重试：$message", retryAt)
          } else {
            completeFailure(
                job,
                if (message.looksLikeSessionExpired()) "登录状态已失效，请重新登录后手动处理。"
                else message,
            )
          }
        },
    )
  }

  private suspend fun completeFailure(job: BykcAutoSelectJobRecord, message: String) {
    store.markFailed(job, message)
    notify(
        job = job,
        type = if (message.looksLikeSessionExpired()) "bykc_auto_select_session_expired"
        else "bykc_auto_select_failed",
        title = if (message.looksLikeSessionExpired()) "博雅抢课需要重新登录" else "博雅抢课失败",
        body = "${job.courseName} 自动选课未完成：$message",
    )
  }

  private suspend fun notify(
      job: BykcAutoSelectJobRecord,
      type: String,
      title: String,
      body: String,
  ) {
    notificationStore.append(
        username = job.username,
        type = type,
        title = title,
        body = body,
        actionUrl = "/",
        payload = mapOf("courseId" to job.courseId.toString(), "jobId" to job.id),
    )
    pushDispatcher.notifyUser(job.username, title, body, "/")
  }

  private fun String.looksLikeSessionExpired(): Boolean =
      contains("登录") || contains("会话") || contains("session", ignoreCase = true)
}

object GlobalBykcAutoSelectScheduler {
  val instance: BykcAutoSelectScheduler by lazy { BykcAutoSelectScheduler() }
}
