package com.waitasec.app.ui.settings

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.waitasec.app.data.AppInfo
import com.waitasec.app.data.SUGGESTED_PACKAGE_NAMES
import com.waitasec.app.data.UserSettings
import com.waitasec.app.ui.theme.Cream
import com.waitasec.app.ui.theme.Mist
import com.waitasec.app.ui.theme.SageDeep
import com.waitasec.app.ui.theme.SageMid
import com.waitasec.app.ui.theme.SagePale
import com.waitasec.app.ui.theme.SageSoft
import com.waitasec.app.ui.theme.SoftCoral
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAccessibilityStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Mist, Cream)))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                text = "Wait a sec…",
                style = MaterialTheme.typography.displayMedium,
                color = SageDeep,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Choose which apps need a breath first.",
                style = MaterialTheme.typography.bodyLarge,
                color = SageMid,
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                AccessibilityCard(
                    enabled = viewModel.accessibilityEnabled,
                    onOpenSettings = viewModel::openAccessibilitySettings,
                )
            }

            item {
                ProtectionCard(
                    protectionEnabled = settings.protectionEnabled,
                    serviceEnabled = viewModel.accessibilityEnabled,
                    onToggle = viewModel::setProtectionEnabled,
                )
            }

            item {
                DelayCard(
                    delaySeconds = settings.delaySeconds,
                    onDelayChange = viewModel::setDelaySeconds,
                )
            }

            item {
                Text(
                    text = "Restricted apps",
                    style = MaterialTheme.typography.titleLarge,
                    color = SageDeep,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = "${settings.restrictedPackages.size} selected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SageMid,
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search apps") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = SageMid)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SageMid,
                        unfocusedBorderColor = SageSoft,
                        focusedContainerColor = Cream,
                        unfocusedContainerColor = Cream,
                    ),
                )
            }

            if (viewModel.appsLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SageMid)
                    }
                }
            } else {
                items(viewModel.filteredApps(), key = { it.packageName }) { app ->
                    AppRow(
                        app = app,
                        selected = app.packageName in settings.restrictedPackages,
                        suggested = app.packageName in SUGGESTED_PACKAGE_NAMES,
                        onToggle = { selected ->
                            viewModel.togglePackage(app.packageName, selected)
                        },
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun AccessibilityCard(enabled: Boolean, onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (enabled) SagePale.copy(alpha = 0.55f) else SoftCoral.copy(alpha = 0.18f))
            .padding(18.dp),
    ) {
        Text(
            text = if (enabled) "Protection service is on" else "Protection service is off",
            style = MaterialTheme.typography.titleMedium,
            color = SageDeep,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (enabled) {
                "Watched apps will show a breathing pause before you can use them."
            } else {
                "Turn on Wait a sec… in Accessibility settings to intercept watched apps."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = SageMid,
        )
        if (!enabled) {
            TextButton(onClick = onOpenSettings, contentPadding = PaddingValues(0.dp)) {
                Text("Open Accessibility settings", color = SoftCoral)
            }
        }
    }
}

@Composable
private fun ProtectionCard(
    protectionEnabled: Boolean,
    serviceEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Cream)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Pause watched apps", style = MaterialTheme.typography.titleMedium, color = SageDeep)
            Text(
                text = if (serviceEnabled) {
                    "When off, launches go through without a wait."
                } else {
                    "Enable Accessibility first for this to take effect."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SageMid,
            )
        }
        Switch(
            checked = protectionEnabled && serviceEnabled,
            onCheckedChange = onToggle,
            enabled = serviceEnabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SageMid,
                checkedThumbColor = Cream,
            ),
        )
    }
}

@Composable
private fun DelayCard(delaySeconds: Int, onDelayChange: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Cream)
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Breath duration", style = MaterialTheme.typography.titleMedium, color = SageDeep)
            Text(
                text = "${delaySeconds}s",
                style = MaterialTheme.typography.titleLarge,
                color = SageMid,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = delaySeconds.toFloat(),
            onValueChange = { onDelayChange(it.roundToInt()) },
            valueRange = UserSettings.MIN_DELAY_SECONDS.toFloat()..UserSettings.MAX_DELAY_SECONDS.toFloat(),
            steps = UserSettings.MAX_DELAY_SECONDS - UserSettings.MIN_DELAY_SECONDS - 1,
            colors = SliderDefaults.colors(
                thumbColor = SageDeep,
                activeTrackColor = SageMid,
                inactiveTrackColor = SagePale,
            ),
        )
        Text(
            text = "A few slow seconds to notice the urge before you open the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = SageMid,
        )
    }
}

@Composable
private fun AppRow(
    app: AppInfo,
    selected: Boolean,
    suggested: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Cream)
            .clickable { onToggle(!selected) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(drawable = app.icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.titleMedium,
                color = SageDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (suggested) {
                Text(
                    text = "Often distracting",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SoftCoral.copy(alpha = 0.9f),
                )
            }
        }
        Checkbox(
            checked = selected,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = SageMid,
                uncheckedColor = SageSoft,
            ),
        )
    }
}

@Composable
private fun AppIcon(drawable: Drawable?) {
    if (drawable == null) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(SagePale, CircleShape),
        )
        return
    }
    val bitmap = drawable.toBitmap(width = 96, height = 96)
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp)),
    )
}
