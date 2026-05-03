package cn.edu.ubaa.ui.screens.bykc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.edu.ubaa.model.dto.AutoBookingTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoBookingScreen(
    viewModel: AutoBookingViewModel,
    onNavigateToCourse: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets =
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            if (uiState.activeTasks.isEmpty() && uiState.completedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无自动预约任务",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "在博雅课程详情页面点击\"自动预约\"按钮添加任务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                ) {
                    if (uiState.activeTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = "进行中 (${uiState.activeTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        items(uiState.activeTasks, key = { it.id }) { task ->
                            AutoBookingTaskCard(
                                task = task,
                                onDelete = { confirmDeleteId = task.id },
                                onNavigate = { onNavigateToCourse(task.courseId) },
                            )
                        }
                    }

                    if (uiState.completedTasks.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "已完成 (${uiState.completedTasks.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        items(uiState.completedTasks, key = { it.id }) { task ->
                            AutoBookingTaskCard(
                                task = task,
                                onDelete = { confirmDeleteId = task.id },
                                onNavigate = { onNavigateToCourse(task.courseId) },
                            )
                        }
                    }
                }
            }

            if (uiState.isPolling && uiState.activeTasks.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在监控预约开放时间...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    confirmDeleteId?.let { taskId ->
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            title = { Text("删除任务") },
            text = { Text("确定要删除此自动预约任务吗？删除后将不会自动选课。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeTask(taskId)
                        confirmDeleteId = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteId = null }) { Text("取消") } },
        )
    }
}

@Composable
private fun AutoBookingTaskCard(
    task: AutoBookingTask,
    onDelete: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), onClick = onNavigate) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = task.courseName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (!task.isCompleted) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "监控中",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                } else if (task.isFailed) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "失败",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                    )
                } else {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                "成功",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50),
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50),
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            task.courseSelectStartDate?.let { start ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "开放时间: ${formatDateTimeDisplay(start)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            task.resultMessage
                ?.takeIf { it.isNotBlank() }
                ?.let { msg ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (task.isCompleted && !task.isFailed) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.error,
                    )
                }

            if (!task.isCompleted) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDelete,
                    colors =
                        ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("取消自动预约")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除记录")
                }
            }
        }
    }
}
