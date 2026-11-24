package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppUsageEntity
import com.example.data.DailySummaryEntity
import com.example.data.PresetAppCatalog
import com.example.data.formatScreenTimeCompact
import com.example.data.formatScreenTimeLong
import com.example.ui.DaySummaryUi
import com.example.ui.theme.WellbeingChartPalette
import com.example.ui.theme.chartColorForIndex

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditAppUsageSheet(
    app: AppUsageEntity,
    focusTimer: Boolean,
    onDismiss: () -> Unit,
    onSave: (AppUsageEntity) -> Unit,
    onDelete: (AppUsageEntity) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var appName by remember(app.id) { mutableStateOf(app.appName) }
    var category by remember(app.id) { mutableStateOf(app.category) }
    var iconKey by remember(app.id) { mutableStateOf(app.iconKey) }
    var colorIndex by remember(app.id) { mutableIntStateOf(app.colorIndex) }
    var totalMinutes by remember(app.id) { mutableIntStateOf(app.usageMinutes) }
    var timerMinutes by remember(app.id) { mutableIntStateOf(app.timerLimitMinutes) }

    var hoursText by remember(app.id) { mutableStateOf((app.usageMinutes / 60).toString()) }
    var minsText by remember(app.id) { mutableStateOf((app.usageMinutes % 60).toString()) }

    fun syncFromTotal(newTotal: Int) {
        val clamped = newTotal.coerceIn(0, 24 * 60)
        totalMinutes = clamped
        hoursText = (clamped / 60).toString()
        minsText = (clamped % 60).toString()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppIconBadge(iconKey = iconKey, colorIndex = colorIndex, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (focusTimer) "App timer & duration" else "Edit app usage",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = formatScreenTimeLong(totalMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                IconButton(
                    onClick = { onDelete(app) },
                    modifier = Modifier.testTag("delete_app_button"),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Remove app",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }

            OutlinedTextField(
                value = appName,
                onValueChange = { appName = it },
                label = { Text("App name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_app_name_input"),
            )

            // Compact Hours / Minutes Input + Quick Steppers
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(2)
                        hoursText = digits
                        val h = digits.toIntOrNull() ?: 0
                        val m = minsText.toIntOrNull() ?: 0
                        totalMinutes = (h * 60 + m).coerceIn(0, 24 * 60)
                    },
                    label = { Text("Hours") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_app_hours_input"),
                )
                OutlinedTextField(
                    value = minsText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(2)
                        minsText = digits
                        val h = hoursText.toIntOrNull() ?: 0
                        val m = (digits.toIntOrNull() ?: 0).coerceIn(0, 59)
                        totalMinutes = (h * 60 + m).coerceIn(0, 24 * 60)
                    },
                    label = { Text("Minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_app_minutes_input"),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                listOf(-15 to "-15m", -5 to "-5m", 5 to "+5m", 15 to "+15m", 60 to "+1h").forEach { (delta, label) ->
                    FilledTonalButton(
                        onClick = { syncFromTotal(totalMinutes + delta) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quick_time_${delta}"),
                    ) {
                        Text(label)
                    }
                }
            }

            HorizontalDivider()

            // Daily App Timer options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassBottom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily app timer",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(0 to "Off", 30 to "30m", 60 to "1h", 90 to "1h 30m", 120 to "2h").forEach { (mins, label) ->
                        FilterChip(
                            selected = timerMinutes == mins,
                            onClick = { timerMinutes = mins },
                            label = { Text(label) },
                        )
                    }
                }
            }

            // Compact Color & Icon picker
            ColorAndIconPicker(
                selectedIconKey = iconKey,
                onIconSelect = { iconKey = it },
                selectedColorIndex = colorIndex,
                onColorSelect = { colorIndex = it },
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(
                            app.copy(
                                appName = appName.trim().ifEmpty { app.appName },
                                category = category,
                                iconKey = iconKey,
                                colorIndex = colorIndex,
                                usageMinutes = totalMinutes,
                                timerLimitMinutes = timerMinutes,
                            )
                        )
                    },
                    enabled = appName.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_app_edit_button"),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAppSheet(
    onDismiss: () -> Unit,
    onAdd: (
        appName: String,
        category: String,
        iconKey: String,
        colorIndex: Int,
        usageMinutes: Int,
        timerLimitMinutes: Int,
    ) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var appName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Social") }
    var iconKey by remember { mutableStateOf("apps") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var hoursText by remember { mutableStateOf("0") }
    var minsText by remember { mutableStateOf("30") }

    val totalMinutes = ((hoursText.toIntOrNull() ?: 0) * 60 + (minsText.toIntOrNull() ?: 0))
        .coerceIn(0, 24 * 60)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Add app usage",
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = "Quick presets",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                PresetAppCatalog.templates.forEach { preset ->
                    FilterChip(
                        selected = appName.equals(preset.appName, ignoreCase = true),
                        onClick = {
                            appName = preset.appName
                            category = preset.category
                            iconKey = preset.iconKey
                            colorIndex = preset.colorIndex
                            hoursText = (preset.defaultMinutes / 60).toString()
                            minsText = (preset.defaultMinutes % 60).toString()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = iconForKey(preset.iconKey),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        label = { Text(preset.appName) },
                    )
                }
            }

            OutlinedTextField(
                value = appName,
                onValueChange = { appName = it },
                label = { Text("App name (preset or custom)") },
                placeholder = { Text("e.g. YouTube, Kindle, Slack") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("add_app_name_input"),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = { Text("Hours") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = minsText,
                    onValueChange = { minsText = it.filter { ch -> ch.isDigit() }.take(2) },
                    label = { Text("Minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PresetAppCatalog.categories.take(6).forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                    )
                }
            }

            ColorAndIconPicker(
                selectedIconKey = iconKey,
                onIconSelect = { iconKey = it },
                selectedColorIndex = colorIndex,
                onColorSelect = { colorIndex = it },
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onAdd(appName, category, iconKey, colorIndex, totalMinutes, 0)
                    },
                    enabled = appName.isNotBlank(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_add_app_button"),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDaySummarySheet(
    day: DaySummaryUi,
    onDismiss: () -> Unit,
    onSave: (DailySummaryEntity) -> Unit,
    onEditApp: (AppUsageEntity) -> Unit,
    onAddApp: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var dayLabel by remember(day.summary.dayIndex) { mutableStateOf(day.summary.dayLabel) }
    var unlocks by remember(day.summary.dayIndex) { mutableIntStateOf(day.summary.unlocksCount) }
    var notifications by remember(day.summary.dayIndex) { mutableIntStateOf(day.summary.notificationsCount) }
    var useOverride by remember(day.summary.dayIndex) { mutableStateOf(day.summary.useCustomTotalOverride) }

    val initialTotal = if (day.summary.useCustomTotalOverride) {
        day.summary.customTotalMinutes
    } else {
        day.autoSumMinutes
    }
    var overrideHours by remember(day.summary.dayIndex) { mutableStateOf((initialTotal / 60).toString()) }
    var overrideMins by remember(day.summary.dayIndex) { mutableStateOf((initialTotal % 60).toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Edit ${day.summary.dayLabel}",
                style = MaterialTheme.typography.titleLarge,
            )

            OutlinedTextField(
                value = dayLabel,
                onValueChange = { dayLabel = it },
                label = { Text("Day label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            CompactCounterRow(
                label = "Unlocks",
                value = unlocks,
                onDelta = { unlocks = (unlocks + it).coerceAtLeast(0) },
                stepBig = 5,
            )

            CompactCounterRow(
                label = "Notifications",
                value = notifications,
                onDelta = { notifications = (notifications + it).coerceAtLeast(0) },
                stepBig = 10,
            )

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom total screen time",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = if (useOverride) {
                            "Overrides app sum on chart & widget"
                        } else {
                            "Auto-summed from apps (${formatScreenTimeLong(day.autoSumMinutes)})"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = useOverride,
                    onCheckedChange = { useOverride = it },
                    modifier = Modifier.testTag("custom_total_switch"),
                )
            }

            if (useOverride) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = overrideHours,
                        onValueChange = { overrideHours = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Total hours") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = overrideMins,
                        onValueChange = { overrideMins = it.filter { ch -> ch.isDigit() }.take(2) },
                        label = { Text("Total minutes") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            HorizontalDivider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Apps (${day.apps.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onAddApp) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add app")
                }
            }

            day.apps.forEach { app ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onEditApp(app) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        AppIconBadge(iconKey = app.iconKey, colorIndex = app.colorIndex, size = 32.dp, iconSize = 18.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = formatScreenTimeCompact(app.usageMinutes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val customTotal = ((overrideHours.toIntOrNull() ?: 0) * 60 +
                            (overrideMins.toIntOrNull() ?: 0).coerceIn(0, 59)).coerceIn(0, 24 * 60)
                        onSave(
                            day.summary.copy(
                                dayLabel = dayLabel.trim().ifEmpty { day.summary.dayLabel },
                                unlocksCount = unlocks,
                                notificationsCount = notifications,
                                useCustomTotalOverride = useOverride,
                                customTotalMinutes = customTotal,
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_day_summary_button"),
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPreviewSheet(
    today: DaySummaryUi?,
    isPinSupported: Boolean,
    onPinWidget: () -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = isSystemInDarkTheme()
    val topApps = today?.apps?.filter { it.usageMinutes > 0 }?.take(3).orEmpty()
    val maxMins = topApps.maxOfOrNull { it.usageMinutes }?.coerceAtLeast(1) ?: 1

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Home Screen Widget",
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Text(
                text = "Live preview of your classic Screen Time widget. Every edit you make updates this widget automatically.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Live 1:1 preview card mimicking widget_screen_time.xml
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .testTag("widget_live_preview_card"),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Screen time",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text(
                                text = today?.summary?.dayLabel ?: "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatScreenTimeLong(today?.computedTotalMinutes ?: 0),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (topApps.isEmpty()) {
                        Text(
                            text = "No app usage logged today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            topApps.forEach { app ->
                                val accent = chartColorForIndex(app.colorIndex, isDark)
                                val fraction = (app.usageMinutes.toFloat() / maxMins.toFloat()).coerceIn(0.08f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(accent),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = app.appName,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = formatScreenTimeCompact(app.usageMinutes),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(accent),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onPinWidget,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_widget_button"),
            ) {
                Icon(Icons.Rounded.Widgets, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPinSupported) "Add widget to home screen" else "How to add widget")
            }

            Text(
                text = "Tip: You can also long-press your Android home screen, tap Widgets, and choose Screen Wellbeing -> Screen time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            TextButton(
                onClick = onResetDefaults,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag("reset_defaults_button"),
            ) {
                Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore default sample data")
            }
        }
    }
}

@Composable
private fun CompactCounterRow(
    label: String,
    value: Int,
    onDelta: (Int) -> Unit,
    stepBig: Int,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
            }
            TextButton(onClick = { onDelta(-stepBig) }) { Text("-$stepBig") }
            IconButton(onClick = { onDelta(-1) }) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease $label")
            }
            IconButton(onClick = { onDelta(1) }) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase $label")
            }
            TextButton(onClick = { onDelta(stepBig) }) { Text("+$stepBig") }
        }
    }
}

@Composable
private fun ColorAndIconPicker(
    selectedIconKey: String,
    onIconSelect: (String) -> Unit,
    selectedColorIndex: Int,
    onColorSelect: (Int) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Accent color",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            WellbeingChartPalette.indices.forEach { idx ->
                val color = chartColorForIndex(idx, isDark)
                val selected = idx == selectedColorIndex
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selected) 3.dp else 0.dp,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clickable { onColorSelect(idx) },
                    contentAlignment = Alignment.Center,
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Selected color",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        Text(
            text = "Icon",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            PresetAppCatalog.iconKeys.forEach { (key, _) ->
                val selected = key == selectedIconKey
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onIconSelect(key) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconForKey(key),
                            contentDescription = key,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}
