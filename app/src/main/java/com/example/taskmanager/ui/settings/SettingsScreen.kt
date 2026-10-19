package com.example.taskmanager.ui.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.data.repository.AppSettings
import com.example.taskmanager.data.repository.ThemeMode
import com.example.taskmanager.reminder.ReminderNotifications
import com.example.taskmanager.ui.theme.ColorSchemeGenerator
import com.example.taskmanager.ui.theme.md_theme_light_primary
import com.example.taskmanager.ui.settings.SettingsViewModel

// curated so every option has readable contrast in both light and dark themes
private val accentColors = listOf(
    md_theme_light_primary,
    Color(0xFF3F51B5), // indigo
    Color(0xFF1E88E5), // blue
    Color(0xFF00838F), // cyan
    Color(0xFF00897B), // teal
    Color(0xFF2E7D32), // green
    Color(0xFF7B1FA2), // purple
    Color(0xFF5E35B1), // deep purple
    Color(0xFFD81B60), // pink
    Color(0xFFC62828), // red
    Color(0xFFE64A19), // deep orange
    Color(0xFF6D4C41), // brown
    Color(0xFF546E7A), // blue grey
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel) {
    val settings = viewModel.settings.collectAsStateWithLifecycle().value ?: AppSettings()
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull().orEmpty()
    }
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    // re-checked on resume so returning from system settings updates the card
    var notificationsAllowed by remember { mutableStateOf(ReminderNotifications.canPost(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { notificationsAllowed = ReminderNotifications.canPost(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(stringResource(R.string.appearance)) {
                Text(stringResource(R.string.theme), style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                            modifier = Modifier.testTag("ThemeMode_${mode.name}")
                        ) { Text(mode.displayName) }
                    }
                }

                if (supportsDynamic) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setDynamicColor(!settings.dynamicColor) }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.dynamic_color), style = MaterialTheme.typography.bodyLarge)
                            Text(
                                stringResource(R.string.dynamic_color_summary),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = settings.dynamicColor, onCheckedChange = viewModel::setDynamicColor)
                    }
                }
            }

            SettingsCard(stringResource(R.string.accent_color)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    // tapping a swatch turns wallpaper colours off, so it stays interactive, just dimmed
                    modifier = Modifier.alpha(if (settings.dynamicColor) 0.5f else 1f)
                ) {
                    accentColors.forEachIndexed { index, color ->
                        val selected = !settings.dynamicColor && color == settings.seedColor
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (selected) 3.dp else 0.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { viewModel.setSeedColor(color) }
                                .testTag("AccentColor_$index")
                        ) {
                            if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ColorSchemeGenerator.getContrastColor(color))
                            }
                        }
                    }
                }
            }

            SettingsCard(stringResource(R.string.notifications)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (notificationsAllowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(if (notificationsAllowed) R.string.notifications_on else R.string.notifications_off),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        )
                    }) { Text(stringResource(R.string.notifications_manage)) }
                }
            }

            SettingsCard(stringResource(R.string.about)) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.version, versionName),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
