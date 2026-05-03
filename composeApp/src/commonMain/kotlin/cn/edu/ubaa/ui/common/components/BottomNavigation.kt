package cn.edu.ubaa.ui.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class BottomNavTab {
  HOME,
  REGULAR,
  ADVANCED,
}

@Composable
fun BottomNavigation(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
  Surface(
      modifier = modifier.fillMaxWidth(),
      shadowElevation = 8.dp,
      color = MaterialTheme.colorScheme.surface,
  ) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
    ) {
      NavigationBarItem(
          icon = {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "首页",
            )
          },
          label = { Text("首页") },
          selected = currentTab == BottomNavTab.HOME,
          onClick = { onTabSelected(BottomNavTab.HOME) },
          colors =
              NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary,
                  indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              ),
      )

      NavigationBarItem(
          icon = {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "普通功能",
            )
          },
          label = { Text("普通功能") },
          selected = currentTab == BottomNavTab.REGULAR,
          onClick = { onTabSelected(BottomNavTab.REGULAR) },
          colors =
              NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary,
                  indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              ),
      )

      NavigationBarItem(
          icon = {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "高级功能",
            )
          },
          label = { Text("高级功能") },
          selected = currentTab == BottomNavTab.ADVANCED,
          onClick = { onTabSelected(BottomNavTab.ADVANCED) },
          colors =
              NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary,
                  indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              ),
      )
    }
  }
}
