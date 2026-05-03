package cn.edu.ubaa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultUnlockScreen(
    viewModel: VaultViewModel,
    onVaultReady: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConfigured = uiState.isConfigured

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isUnlocked) {
        if (uiState.isUnlocked) {
            onVaultReady()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null && isConfigured) {
            password = ""
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets =
            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = if (isConfigured) "解锁密码保管箱" else "设置保管箱密码",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text =
                    if (isConfigured) "请输入保管箱密码以解锁"
                    else "请设置一个主密码来保护您的密码记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (uiState.error != null) viewModel.clearError()
                },
                label = { Text(if (isConfigured) "主密码" else "设置主密码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation =
                    if (showPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                leadingIcon = { Icon(Icons.Default.Key, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            null,
                        )
                    }
                },
                isError = uiState.error != null,
            )

            if (!isConfigured) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        if (uiState.error != null) viewModel.clearError()
                    },
                    label = { Text("确认密码") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    isError = uiState.error != null,
                )
            }

            uiState.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isConfigured) {
                        viewModel.unlock(password)
                    } else {
                        if (password != confirmPassword) {
                            // Error will be set from ViewModel
                        }
                        viewModel.setMasterPassword(password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = password.isNotBlank() && (isConfigured || confirmPassword == password),
            ) {
                Icon(
                    if (isConfigured) Icons.Default.LockOpen else Icons.Default.VpnKey,
                    null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isConfigured) "解锁" else "创建保管箱")
            }

            if (isConfigured) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { viewModel.resetVault(); password = ""; confirmPassword = "" }) {
                    Text("重置保管箱 (数据将丢失)", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
