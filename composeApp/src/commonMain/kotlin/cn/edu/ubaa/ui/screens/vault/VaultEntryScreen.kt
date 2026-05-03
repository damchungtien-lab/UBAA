package cn.edu.ubaa.ui.screens.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cn.edu.ubaa.api.VaultEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultEntryScreen(
    entry: VaultEntry?,
    viewModel: VaultViewModel,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()

  var title by remember(entry) { mutableStateOf(entry?.title ?: "") }
  var systemName by remember(entry) { mutableStateOf(entry?.systemName ?: "") }
  var username by remember(entry) { mutableStateOf(entry?.username ?: "") }
  var password by remember(entry) { mutableStateOf(entry?.password ?: "") }
  var url by remember(entry) { mutableStateOf(entry?.url ?: "") }
  var notes by remember(entry) { mutableStateOf(entry?.notes ?: "") }
  var showPassword by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.message) {
    if (uiState.message != null) {
      onSave()
      viewModel.clearMessage()
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("名称 *") },
          placeholder = { Text("例如: 校园网账号") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Label, null) },
      )

      OutlinedTextField(
          value = systemName,
          onValueChange = { systemName = it },
          label = { Text("系统/平台") },
          placeholder = { Text("例如: 智慧北航") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Computer, null) },
      )

      OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("用户名") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Person, null) },
      )

      OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("密码") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          visualTransformation =
              if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          leadingIcon = { Icon(Icons.Default.Key, null) },
          trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
              Icon(
                  if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  null,
              )
            }
          },
      )

      OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text("网址") },
          placeholder = { Text("https://...") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Link, null) },
      )

      OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("备注") },
          modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
          maxLines = 5,
          leadingIcon = { Icon(Icons.Default.Notes, null) },
      )

      uiState.error?.let { error ->
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Button(
          onClick = {
            viewModel.saveEntry(
                id = entry?.id,
                title = title.trim(),
                systemName = systemName.trim(),
                username = username.trim(),
                password = password,
                url = url.trim(),
                notes = notes.trim(),
            )
          },
          modifier = Modifier.fillMaxWidth().height(48.dp),
          enabled = title.isNotBlank(),
      ) {
        Icon(Icons.Default.Save, null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (entry != null) "保存修改" else "添加记录")
      }
    }
  }
}
