package cn.edu.ubaa.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
fun RegularFeaturesScreen(
    onScheduleClick: () -> Unit,
    onExamClick: () -> Unit,
    onBykcClick: () -> Unit,
    onClassroomClick: () -> Unit,
    onSpocClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val features =
      listOf(
          FeatureItem(
              id = "schedule",
              title = "课程表查询",
              description = "一眼看到今天课程、周视图和学期切换。",
              icon = Icons.Default.CalendarToday,
              accent = Color(0xFF0D5B63),
          ),
          FeatureItem(
              id = "exam",
              title = "考试查询",
              description = "把考试时间、地点和学期筛选整理到一处。",
              icon = Icons.AutoMirrored.Filled.Assignment,
              accent = Color(0xFF233E53),
          ),
          FeatureItem(
              id = "bykc",
              title = "博雅课程",
              description = "浏览课程、查看已选状态，并进入自动抢课流程。",
              icon = Icons.Default.School,
              accent = Color(0xFFB36A1C),
          ),
          FeatureItem(
              id = "classroom",
              title = "空教室查询",
              description = "快速过滤校区与教学楼，定位可用教室。",
              icon = Icons.Default.MeetingRoom,
              accent = Color(0xFF3E6A87),
          ),
          FeatureItem(
              id = "spoc",
              title = "SPOC 作业",
              description = "集中查看作业与提交状态，减少遗漏。",
              icon = Icons.Default.AssignmentTurnedIn,
              accent = Color(0xFF4C6B45),
          ),
      )

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .background(
                  Brush.verticalGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.background,
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                          )
                  )
              )
              .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FeatureHubHeader(
        eyebrow = "Workspace",
        title = "常用服务",
        subtitle = "把课表、考试、博雅与学习待办放进同一张校园工作台。",
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 220.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      items(features) { feature ->
        FeatureAccentCard(
            feature = feature,
            onClick = {
              when (feature.id) {
                "schedule" -> onScheduleClick()
                "exam" -> onExamClick()
                "bykc" -> onBykcClick()
                "classroom" -> onClassroomClick()
                "spoc" -> onSpocClick()
              }
            },
        )
      }
    }
  }
}

@Composable
internal fun FeatureHubHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
) {
  Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
      shadowElevation = 10.dp,
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
          text = eyebrow,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
      )
      Text(
          text = title,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
      )
      Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun FeatureAccentCard(
    feature: FeatureItem,
    onClick: () -> Unit,
) {
  Card(
      modifier = Modifier.fillMaxWidth().heightIn(min = 188.dp).clickable(onClick = onClick),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
  ) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors =
                            listOf(
                                feature.accent.copy(alpha = 0.18f),
                                MaterialTheme.colorScheme.surface,
                            )
                    )
                )
    ) {
      Column(
          modifier = Modifier.fillMaxSize().padding(18.dp),
          verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Surface(shape = CircleShape, color = feature.accent.copy(alpha = 0.12f)) {
          Icon(
              imageVector = feature.icon,
              contentDescription = null,
              modifier = Modifier.padding(12.dp).size(22.dp),
              tint = feature.accent,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
              text = feature.title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = feature.description,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Text(
            text = "进入模块",
            style = MaterialTheme.typography.labelLarge,
            color = feature.accent,
            fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}
