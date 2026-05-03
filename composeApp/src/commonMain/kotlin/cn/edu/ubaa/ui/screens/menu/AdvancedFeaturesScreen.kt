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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
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

private data class AdvancedFeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accent: Color,
)

@Composable
fun AdvancedFeaturesScreen(
    onSigninClick: () -> Unit,
    onCgyyClick: () -> Unit,
    onEvaluationClick: () -> Unit,
    onYgdkClick: () -> Unit,
    onVaultClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val features =
      listOf(
          AdvancedFeatureItem(
              id = "signin",
              title = "课程签到",
              description = "聚焦课堂签到与快捷处理，减少重复跳转。",
              icon = Icons.Default.HowToReg,
              accent = Color(0xFF0D5B63),
          ),
          AdvancedFeatureItem(
              id = "cgyy",
              title = "研讨室预约",
              description = "查询空闲时段、填写表单并回看预约订单。",
              icon = Icons.Default.DateRange,
              accent = Color(0xFF3E6A87),
          ),
          AdvancedFeatureItem(
              id = "ygdk",
              title = "阳光打卡",
              description = "记录体育活动并快速回看历史提交。",
              icon = Icons.Default.WbSunny,
              accent = Color(0xFFB36A1C),
          ),
          AdvancedFeatureItem(
              id = "vault",
              title = "密码保险库",
              description = "用统一主密码保管账号，服务端只存密文。",
              icon = Icons.Default.Security,
              accent = Color(0xFF4C6B45),
          ),
          AdvancedFeatureItem(
              id = "evaluation",
              title = "自动评教",
              description = "把学期末的重复动作折叠成一条完成路径。",
              icon = Icons.Default.AssignmentTurnedIn,
              accent = Color(0xFF6E4C87),
          ),
          AdvancedFeatureItem(
              id = "more",
              title = "更多能力",
              description = "更多模块会继续沿着这个统一体验补进来。",
              icon = Icons.Default.MoreHoriz,
              accent = Color(0xFF475260),
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
                              MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.26f),
                          )
                  )
              )
              .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    FeatureHubHeader(
        eyebrow = "Automation",
        title = "高级功能",
        subtitle = "把更重度、更高频、也更容易忘记的校园动作收拢到自动化入口里。",
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 220.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      items(features) { feature ->
        Card(
            modifier =
                Modifier.fillMaxWidth().heightIn(min = 188.dp).clickable {
                  when (feature.id) {
                    "signin" -> onSigninClick()
                    "cgyy" -> onCgyyClick()
                    "ygdk" -> onYgdkClick()
                    "evaluation" -> onEvaluationClick()
                    "vault" -> onVaultClick()
                  }
                },
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
                  text = if (feature.id == "more") "持续扩展中" else "打开工具",
                  style = MaterialTheme.typography.labelLarge,
                  color = feature.accent,
                  fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}
