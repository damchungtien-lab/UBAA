package cn.edu.ubaa.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cn.edu.ubaa.model.dto.CaptchaInfo
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun LoginScreen(
    loginFormState: LoginFormState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onRememberPasswordChange: (Boolean) -> Unit,
    onAutoLoginChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    isLoading: Boolean,
    isRefreshingCaptcha: Boolean,
    captchaRequired: Boolean,
    captchaInfo: CaptchaInfo?,
    error: String?,
    modifier: Modifier = Modifier,
) {
  val uriHandler = LocalUriHandler.current
  BoxWithConstraints(
      modifier =
          modifier.fillMaxSize().background(
              brush =
                  Brush.linearGradient(
                      colors =
                          listOf(
                              MaterialTheme.colorScheme.background,
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                              MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.44f),
                          )
                  )
          )
  ) {
    val compactLayout = maxWidth < 920.dp

    if (compactLayout) {
      Column(
          modifier =
              Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
          verticalArrangement = Arrangement.Center,
      ) {
        LoginShell(
            compactLayout = true,
            loginFormState = loginFormState,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onCaptchaChange = onCaptchaChange,
            onRememberPasswordChange = onRememberPasswordChange,
            onAutoLoginChange = onAutoLoginChange,
            onLoginClick = onLoginClick,
            onRefreshCaptcha = onRefreshCaptcha,
            isLoading = isLoading,
            isRefreshingCaptcha = isRefreshingCaptcha,
            captchaRequired = captchaRequired,
            captchaInfo = captchaInfo,
            error = error,
            onOpenSource = { uriHandler.openUri("https://github.com/BUAASubnet/UBAA") },
        )
      }
    } else {
      Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        LoginShell(
            compactLayout = false,
            loginFormState = loginFormState,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onCaptchaChange = onCaptchaChange,
            onRememberPasswordChange = onRememberPasswordChange,
            onAutoLoginChange = onAutoLoginChange,
            onLoginClick = onLoginClick,
            onRefreshCaptcha = onRefreshCaptcha,
            isLoading = isLoading,
            isRefreshingCaptcha = isRefreshingCaptcha,
            captchaRequired = captchaRequired,
            captchaInfo = captchaInfo,
            error = error,
            onOpenSource = { uriHandler.openUri("https://github.com/BUAASubnet/UBAA") },
        )
      }
    }
  }
}

@Composable
private fun LoginShell(
    compactLayout: Boolean,
    loginFormState: LoginFormState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onRememberPasswordChange: (Boolean) -> Unit,
    onAutoLoginChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    isLoading: Boolean,
    isRefreshingCaptcha: Boolean,
    captchaRequired: Boolean,
    captchaInfo: CaptchaInfo?,
    error: String?,
    onOpenSource: () -> Unit,
) {
  Surface(
      modifier = Modifier.fillMaxWidth().widthIn(max = 1100.dp),
      shape = RoundedCornerShape(32.dp),
      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
      shadowElevation = 24.dp,
  ) {
    if (compactLayout) {
      Column(modifier = Modifier.fillMaxWidth()) {
        LoginHeroPanel(compactLayout = true, onOpenSource = onOpenSource)
        LoginFormPanel(
            loginFormState = loginFormState,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onCaptchaChange = onCaptchaChange,
            onRememberPasswordChange = onRememberPasswordChange,
            onAutoLoginChange = onAutoLoginChange,
            onLoginClick = onLoginClick,
            onRefreshCaptcha = onRefreshCaptcha,
            isLoading = isLoading,
            isRefreshingCaptcha = isRefreshingCaptcha,
            captchaRequired = captchaRequired,
            captchaInfo = captchaInfo,
            error = error,
            compactLayout = true,
            onOpenSource = onOpenSource,
        )
      }
    } else {
      Row(modifier = Modifier.fillMaxWidth()) {
        LoginHeroPanel(
            compactLayout = false,
            modifier = Modifier.weight(1.04f).fillMaxWidth(),
            onOpenSource = onOpenSource,
        )
        LoginFormPanel(
            loginFormState = loginFormState,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onCaptchaChange = onCaptchaChange,
            onRememberPasswordChange = onRememberPasswordChange,
            onAutoLoginChange = onAutoLoginChange,
            onLoginClick = onLoginClick,
            onRefreshCaptcha = onRefreshCaptcha,
            isLoading = isLoading,
            isRefreshingCaptcha = isRefreshingCaptcha,
            captchaRequired = captchaRequired,
            captchaInfo = captchaInfo,
            error = error,
            compactLayout = false,
            onOpenSource = onOpenSource,
            modifier = Modifier.weight(0.96f).fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun LoginHeroPanel(
    compactLayout: Boolean,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Box(
      modifier =
          modifier
              .background(
                  brush =
                      Brush.linearGradient(
                          colors =
                              listOf(
                                  MaterialTheme.colorScheme.secondary,
                                  MaterialTheme.colorScheme.primary,
                                  MaterialTheme.colorScheme.primaryContainer,
                              )
                      )
              )
              .padding(if (compactLayout) 24.dp else 34.dp)
  ) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = if (compactLayout) 280.dp else 620.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
      Surface(
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.14f),
      ) {
        Text(
            text = "BUAA Campus Remake",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "UBAA",
            style = if (compactLayout) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displaySmall,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "把智慧北航做成更统一、更顺手、也更现代的校园操作台。",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.92f),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "统一登录、课程与待办视图、博雅自动抢课、加密保险库和跨端体验，都在同一个工作流里完成。",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.82f),
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LoginHighlightRow(
            icon = Icons.Default.School,
            title = "统一校园入口",
            description = "课表、考试、博雅、签到与更多校园服务集中在同一个界面里。",
        )
        LoginHighlightRow(
            icon = Icons.Default.Bolt,
            title = "自动博雅抢课",
            description = "到点后自动触发选课尝试，并把结果写入通知中心。",
        )
        LoginHighlightRow(
            icon = Icons.Default.Security,
            title = "本地主密码解锁",
            description = "账号密码统一保管，服务端只保存加密后的保险库内容。",
        )
      }

      Spacer(modifier = Modifier.weight(1f, fill = compactLayout.not()))

      Surface(
          shape = RoundedCornerShape(24.dp),
          color = Color.White.copy(alpha = 0.12f),
      ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
              text = "开源重构项目",
              style = MaterialTheme.typography.labelLarge,
              color = Color.White.copy(alpha = 0.76f),
          )
          Text(
              text = "保留原有功能路径，同时让跨端 Web 版本更适合长期使用。",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White,
          )
          Text(
              text = "查看 GitHub 仓库",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable(onClick = onOpenSource),
          )
        }
      }
    }
  }
}

@Composable
private fun LoginHighlightRow(
    icon: ImageVector,
    title: String,
    description: String,
) {
  Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.Top,
  ) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.14f),
    ) {
      Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.padding(10.dp).size(20.dp),
          tint = Color.White,
      )
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          fontWeight = FontWeight.Bold,
      )
      Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.8f),
      )
    }
  }
}

@Composable
private fun LoginFormPanel(
    loginFormState: LoginFormState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCaptchaChange: (String) -> Unit,
    onRememberPasswordChange: (Boolean) -> Unit,
    onAutoLoginChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onRefreshCaptcha: () -> Unit,
    isLoading: Boolean,
    isRefreshingCaptcha: Boolean,
    captchaRequired: Boolean,
    captchaInfo: CaptchaInfo?,
    error: String?,
    compactLayout: Boolean,
    onOpenSource: () -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.fillMaxWidth().padding(if (compactLayout) 22.dp else 32.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().padding(if (compactLayout) 18.dp else 22.dp),
          verticalArrangement = Arrangement.spacedBy(18.dp),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Surface(
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
          ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                  tint = MaterialTheme.colorScheme.primary,
              )
              Text(
                  text = "校园账户登录",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold,
              )
            }
          }
          Text(
              text = "欢迎回来",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
          )
          Text(
              text = "使用智慧北航账号继续访问你的课程、通知与自动化工具。",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        OutlinedTextField(
            value = loginFormState.username,
            onValueChange = onUsernameChange,
            label = { Text("学号") },
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = loginFormState.password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        if (captchaRequired && captchaInfo != null) {
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedTextField(
                  value = loginFormState.captcha,
                  onValueChange = onCaptchaChange,
                  label = { Text("验证码") },
                  singleLine = true,
                  enabled = !isLoading,
                  modifier = Modifier.weight(1f).padding(end = 8.dp),
              )
              CaptchaImage(
                  captchaInfo = captchaInfo,
                  onClick = onRefreshCaptcha,
                  isRefreshing = isRefreshingCaptcha,
                  modifier = Modifier.height(56.dp).width(128.dp),
              )
            }
            Text(
                text = "点击图片可刷新验证码",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        if (compactLayout) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LoginOptionRow(
                checked = loginFormState.rememberPassword,
                label = "记住密码",
                enabled = !isLoading,
                onCheckedChange = onRememberPasswordChange,
            )
            LoginOptionRow(
                checked = loginFormState.autoLogin,
                label = "自动登录",
                enabled = !isLoading,
                onCheckedChange = onAutoLoginChange,
            )
          }
        } else {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            LoginOptionRow(
                checked = loginFormState.rememberPassword,
                label = "记住密码",
                enabled = !isLoading,
                onCheckedChange = onRememberPasswordChange,
                modifier = Modifier.weight(1f),
            )
            LoginOptionRow(
                checked = loginFormState.autoLogin,
                label = "自动登录",
                enabled = !isLoading,
                onCheckedChange = onAutoLoginChange,
                modifier = Modifier.weight(1f),
            )
          }
        }

        Button(
            onClick = onLoginClick,
            enabled =
                !isLoading &&
                    loginFormState.username.isNotBlank() &&
                    loginFormState.password.isNotBlank() &&
                    (!captchaRequired || loginFormState.captcha.isNotBlank()),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
          if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
          } else {
            Text("登录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
        }

        error?.let { msg ->
          Card(
              modifier = Modifier.fillMaxWidth(),
              colors =
                  CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.errorContainer
                  ),
          ) {
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }

    Text(
        text = "开源项目 · https://github.com/BUAASubnet/UBAA",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable(onClick = onOpenSource),
    )
  }
}

@Composable
private fun LoginOptionRow(
    checked: Boolean,
    label: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier = modifier.clickable(enabled = enabled) { onCheckedChange(!checked) },
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surface,
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        Text(label, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
}

@Composable
private fun CaptchaImage(
    captchaInfo: CaptchaInfo,
    onClick: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
  @OptIn(ExperimentalEncodingApi::class)
  val imageBytes =
      remember(captchaInfo.base64Image) {
        captchaInfo.base64Image
            ?.substringAfter("base64,", "")
            ?.takeIf { it.isNotBlank() }
            ?.let { Base64.decode(it) }
      }

  Card(
      modifier = modifier.clickable(enabled = !isRefreshing, onClick = onClick),
      shape = RoundedCornerShape(16.dp),
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
  ) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      if (isRefreshing) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
      } else if (imageBytes != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current).data(imageBytes).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
      } else {
        Text(
            text = "加载失败",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
      }
    }
  }
}
