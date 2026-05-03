package cn.edu.ubaa.notifications

import org.slf4j.LoggerFactory

interface PushNotificationDispatcher {
  suspend fun notifyUser(username: String, title: String, body: String, actionUrl: String? = null)
}

class StoredSubscriptionPushNotificationDispatcher(
    private val store: NotificationStore = GlobalNotificationStore.instance,
) : PushNotificationDispatcher {
  private val log =
      LoggerFactory.getLogger(StoredSubscriptionPushNotificationDispatcher::class.java)

  override suspend fun notifyUser(
      username: String,
      title: String,
      body: String,
      actionUrl: String?,
  ) {
    val subscriptionCount = store.pushSubscriptions(username).size
    if (subscriptionCount == 0) return
    log.info(
        "Queued browser notification metadata for user {} subscriptions={} title={}",
        username,
        subscriptionCount,
        title,
    )
  }
}

object GlobalPushNotificationDispatcher {
  val instance: PushNotificationDispatcher by lazy {
    StoredSubscriptionPushNotificationDispatcher()
  }
}
