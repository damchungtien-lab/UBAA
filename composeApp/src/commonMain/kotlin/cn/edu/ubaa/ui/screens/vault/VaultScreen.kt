package cn.edu.ubaa.ui.screens.vault

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cn.edu.ubaa.model.dto.VaultPlainEntryDto

@Composable
fun VaultScreen(
    uiState: VaultUiState,
    onUnlock: (String) -> Unit,
    onInitialize: (String) -> Unit,
    onUpsertEntry: (String?, String, String, String, String?, String?) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onSave: () -> Unit,
    onLock: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
  LazyColumn(
      modifier = modifier.fillMaxSize().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      VaultHeader(
          isLoading = uiState.isLoading,
          unlocked = uiState.unlocked,
          hasVault = uiState.record != null,
          onSave = onSave,
          onLock = onLock,
      )
    }
    uiState.error?.let { error ->
      item { Text(text = error, color = MaterialTheme.colorScheme.error) }
    }
    uiState.message?.let { message ->
      item { Text(text = message, color = MaterialTheme.colorScheme.primary) }
    }

    if (!uiState.unlocked) {
      item {
        VaultUnlockCard(
            hasVault = uiState.record != null,
            isLoading = uiState.isLoading,
            onUnlock = onUnlock,
            onInitialize = onInitialize,
            onReset = onReset,
        )
      }
    } else {
      item { VaultEntryEditor(onSave = onUpsertEntry) }
      if (uiState.entries.isEmpty()) {
        item {
          Text(
              text = "还没有保存任何账号密码",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      } else {
        items(uiState.entries) { entry ->
          VaultEntryCard(entry = entry, onDelete = { onDeleteEntry(entry.id) })
        }
      }
    }
  }
}

@Composable
private fun VaultHeader(
    isLoading: Boolean,
    unlocked: Boolean,
    hasVault: Boolean,
    onSave: () -> Unit,
    onLock: () -> Unit,
) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
  ) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(Icons.Default.Security, contentDescription = null)
      Column(modifier = Modifier.weight(1f)) {
        Text(
            text = "密码保险库",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = if (hasVault) "服务端只保存加密后的保险库" else "创建一个只由主密码解锁的保险库",
            style = MaterialTheme.typography.bodySmall,
        )
      }
      if (isLoading) {
        CircularProgressIndicator()
      } else if (unlocked) {
        IconButton(onClick = onSave) { Icon(Icons.Default.Save, contentDescription = "保存") }
        IconButton(onClick = onLock) { Icon(Icons.Default.Lock, contentDescription = "锁定") }
      }
    }
  }
}

@Composable
private fun VaultUnlockCard(
    hasVault: Boolean,
    isLoading: Boolean,
    onUnlock: (String) -> Unit,
    onInitialize: (String) -> Unit,
    onReset: () -> Unit,
) {
  var password by remember { mutableStateOf("") }
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Text(
          text = if (hasVault) "输入主密码解锁" else "设置保险库主密码",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          modifier = Modifier.fillMaxWidth(),
          label = { Text("主密码") },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = { if (hasVault) onUnlock(password) else onInitialize(password) },
            enabled = password.isNotBlank() && !isLoading,
        ) {
          Text(if (hasVault) "解锁" else "创建")
        }
        if (hasVault) {
          OutlinedButton(onClick = onReset, enabled = !isLoading) { Text("重置") }
        }
      }
    }
  }
}

@Composable
private fun VaultEntryEditor(
    onSave: (String?, String, String, String, String?, String?) -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var username by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var url by remember { mutableStateOf("") }
  var note by remember { mutableStateOf("") }

  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(
          text = "新增记录",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
      )
      OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("名称") },
          modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("用户名") },
          modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("密码") },
          modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text("网址/系统") },
          modifier = Modifier.fillMaxWidth(),
      )
      OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("备注") },
          modifier = Modifier.fillMaxWidth(),
      )
      Button(
          onClick = {
            onSave(null, title, username, password, url, note)
            title = ""
            username = ""
            password = ""
            url = ""
            note = ""
          },
          enabled = title.isNotBlank() && password.isNotBlank(),
      ) {
        Text("添加到保险库")
      }
    }
  }
}

@Composable
private fun VaultEntryCard(entry: VaultPlainEntryDto, onDelete: () -> Unit) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
              text = entry.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
          )
          Text(text = entry.username, style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "删除") }
      }
      Text(text = entry.password, style = MaterialTheme.typography.bodyMedium)
      entry.url?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
      entry.note?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
          text = "更新于 ${entry.updatedAt}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
