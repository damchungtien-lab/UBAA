package cn.edu.ubaa.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cn.edu.ubaa.model.dto.AppNotificationDto

@Composable
fun NotificationScreen(
    uiState: NotificationUiState,
    onRefresh: () -> Unit,
    onNotificationClick: (AppNotificationDto) -> Unit,
    modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    when {
      uiState.isLoading && uiState.notifications.isEmpty() ->
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      uiState.error != null && uiState.notifications.isEmpty() ->
          Text(
              text = uiState.error,
              color = MaterialTheme.colorScheme.error,
              modifier = Modifier.align(Alignment.Center).padding(24.dp),
          )
      uiState.notifications.isEmpty() ->
          Text(
              text = "暂无通知",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.align(Alignment.Center),
          )
      else ->
          LazyColumn(
              modifier = Modifier.fillMaxSize().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            items(uiState.notifications) { notification ->
              NotificationCard(notification = notification) { onNotificationClick(notification) }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
          }
    }
  }
}

@Composable
private fun NotificationCard(notification: AppNotificationDto, onClick: () -> Unit) {
  val unread = notification.readAt == null
  Card(
      modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
      colors =
          CardDefaults.cardColors(
              containerColor =
                  if (unread) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant
          ),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
      Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = null,
          tint =
              if (unread) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.primary,
      )
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = notification.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = notification.body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = notification.createdAt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
