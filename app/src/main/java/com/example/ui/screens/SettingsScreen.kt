package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val readReceipts by settingsViewModel.readReceiptsEnabled.collectAsState()

    var showStorageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Appearance & Themes Section
            SettingsSectionHeader("Appearance & Customization")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Enable dark mode canvas",
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.toggleDarkMode(it) },
                        testTag = "dark_mode_switch"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notifications & Sounds
            SettingsSectionHeader("Notifications & Privacy")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Push Notifications",
                        subtitle = "Receive alerts for new messages & calls",
                        checked = notificationsEnabled,
                        onCheckedChange = { settingsViewModel.toggleNotifications(it) }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsSwitchRow(
                        icon = Icons.Outlined.DoneAll,
                        title = "Read Receipts",
                        subtitle = "Allow contacts to see when you read messages",
                        checked = readReceipts,
                        onCheckedChange = { settingsViewModel.toggleReadReceipts(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data & Storage
            SettingsSectionHeader("Data & Storage")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsClickableRow(
                        icon = Icons.Outlined.CleaningServices,
                        title = "Clear Cache & Temporary Files",
                        subtitle = "14.2 MB cached images and voice notes",
                        onClick = { showStorageDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // About & Account
            SettingsSectionHeader("ConnectX Account")

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    SettingsClickableRow(
                        icon = Icons.Outlined.Info,
                        title = "About ConnectX",
                        subtitle = "Version 2.4.0 (Build 2026)",
                        onClick = { }
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsClickableRow(
                        icon = Icons.Outlined.ExitToApp,
                        title = "Log Out of ConnectX",
                        subtitle = "Sign out from this device",
                        onClick = onLogoutClick,
                        isDestructive = true
                    )
                }
            }
        }
    }

    if (showStorageDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDialog = false },
            title = { Text("Clear ConnectX Cache") },
            text = { Text("Are you sure you want to clean 14.2 MB of temporary media cache? Your chats and messages will remain intact.") },
            confirmButton = {
                Button(
                    onClick = { showStorageDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Clear Cache")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStorageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = PrimaryIndigo,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryIndigo)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
fun SettingsClickableRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else PrimaryIndigo
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
