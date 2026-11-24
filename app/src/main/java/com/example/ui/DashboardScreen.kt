package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AppUsageEntity
import com.example.data.formatScreenTimeCompact
import com.example.data.formatScreenTimeLong
import com.example.ui.components.AddAppSheet
import com.example.ui.components.AppIconBadge
import com.example.ui.components.EditAppUsageSheet
import com.example.ui.components.EditDaySummarySheet
import com.example.ui.components.ScreenTimeDonutChart
import com.example.ui.components.WeeklyBarChart
import com.example.ui.components.WidgetPreviewSheet
import com.example.ui.theme.chartColorForIndex
import com.example.widget.ScreenTimeWidgetProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WellbeingViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        val message = uiState.snackbarMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbar()
        }
    }

    val selectedDay = uiState.selectedDay

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.openWidgetPreviewSheet() },
                        modifier = Modifier.testTag("top_widget_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = stringResource(R.string.cd_widget_preview),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    if (selectedDay != null) {
                        IconButton(
                            onClick = { viewModel.openEditDaySheet(selectedDay) },
                            modifier = Modifier.testTag("top_edit_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.cd_edit_dashboard),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val isWide = maxWidth >= 760.dp

            if (selectedDay == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Loading screen time...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (isWide) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 1040.dp)
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            HeroSummaryCard(
                                day = selectedDay,
                                chronologicalDays = uiState.chronologicalDays,
                                selectedDayIndex = uiState.selectedDayIndex,
                                onSelectDay = viewModel::selectDay,
                                onEditDay = { viewModel.openEditDaySheet(selectedDay) },
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        item {
                            AppUsageSectionHeader(onAddApp = viewModel::openAddAppSheet)
                        }
                        if (selectedDay.apps.isEmpty()) {
                            item {
                                EmptyAppUsageCard(onAddApp = viewModel::openAddAppSheet)
                            }
                        } else {
                            val maxAppMins = selectedDay.apps.maxOf { it.usageMinutes }.coerceAtLeast(1)
                            items(selectedDay.apps, key = { it.id }) { app ->
                                AppUsageRowCard(
                                    app = app,
                                    maxMinutes = maxAppMins,
                                    onClick = { viewModel.openEditAppSheet(app, focusTimer = false) },
                                    onTimerClick = { viewModel.openEditAppSheet(app, focusTimer = true) },
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        HeroSummaryCard(
                            day = selectedDay,
                            chronologicalDays = uiState.chronologicalDays,
                            selectedDayIndex = uiState.selectedDayIndex,
                            onSelectDay = viewModel::selectDay,
                            onEditDay = { viewModel.openEditDaySheet(selectedDay) },
                        )
                    }

                    item {
                        AppUsageSectionHeader(onAddApp = viewModel::openAddAppSheet)
                    }

                    if (selectedDay.apps.isEmpty()) {
                        item {
                            EmptyAppUsageCard(onAddApp = viewModel::openAddAppSheet)
                        }
                    } else {
                        val maxAppMins = selectedDay.apps.maxOf { it.usageMinutes }.coerceAtLeast(1)
                        items(selectedDay.apps, key = { it.id }) { app ->
                            AppUsageRowCard(
                                app = app,
                                maxMinutes = maxAppMins,
                                onClick = { viewModel.openEditAppSheet(app, focusTimer = false) },
                                onTimerClick = { viewModel.openEditAppSheet(app, focusTimer = true) },
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Active compact editor modal sheets
    when (val sheet = uiState.activeSheet) {
        is ActiveEditorSheet.EditApp -> {
            EditAppUsageSheet(
                app = sheet.app,
                focusTimer = sheet.focusTimer,
                onDismiss = viewModel::dismissSheet,
                onSave = viewModel::saveEditedApp,
                onDelete = viewModel::deleteApp,
            )
        }
        ActiveEditorSheet.AddApp -> {
            AddAppSheet(
                onDismiss = viewModel::dismissSheet,
                onAdd = viewModel::addNewApp,
            )
        }
        is ActiveEditorSheet.EditDaySummary -> {
            EditDaySummarySheet(
                day = sheet.day,
                onDismiss = viewModel::dismissSheet,
                onSave = viewModel::saveDaySummary,
                onEditApp = { app -> viewModel.openEditAppSheet(app, focusTimer = false) },
                onAddApp = viewModel::openAddAppSheet,
            )
        }
        ActiveEditorSheet.WidgetPreview -> {
            val pinSupported = remember { ScreenTimeWidgetProvider.isPinSupported(context) }
            WidgetPreviewSheet(
                today = uiState.todaySummary,
                isPinSupported = pinSupported,
                onPinWidget = {
                    val launched = ScreenTimeWidgetProvider.requestPinWidget(context)
                    if (launched) {
                        viewModel.dismissSheet()
                        viewModel.notifyMessage("Confirm widget placement on your home screen")
                    } else {
                        viewModel.notifyMessage("Long-press home screen -> Widgets -> Screen time")
                    }
                },
                onResetDefaults = viewModel::resetDefaults,
                onDismiss = viewModel::dismissSheet,
            )
        }
        null -> Unit
    }
}

@Composable
private fun HeroSummaryCard(
    day: DaySummaryUi,
    chronologicalDays: List<DaySummaryUi>,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    onEditDay: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            ScreenTimeDonutChart(
                dayLabel = day.summary.dayLabel,
                totalMinutes = day.computedTotalMinutes,
                apps = day.apps,
                isCustomOverride = day.summary.useCustomTotalOverride,
                onCenterClick = onEditDay,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                StatCounterCard(
                    icon = Icons.Outlined.LockOpen,
                    count = day.summary.unlocksCount,
                    label = "Unlocks",
                    onClick = onEditDay,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("unlocks_stat_card"),
                )
                StatCounterCard(
                    icon = Icons.Outlined.Notifications,
                    count = day.summary.notificationsCount,
                    label = "Notifications",
                    onClick = onEditDay,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("notifications_stat_card"),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            WeeklyBarChart(
                chronologicalDays = chronologicalDays,
                selectedDayIndex = selectedDayIndex,
                onSelectDay = onSelectDay,
            )
        }
    }
}

@Composable
private fun StatCounterCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AppUsageSectionHeader(onAddApp: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text(
            text = "App activity",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onAddApp,
            modifier = Modifier.testTag("add_app_button"),
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.cd_add_app),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AppUsageRowCard(
    app: AppUsageEntity,
    maxMinutes: Int,
    onClick: () -> Unit,
    onTimerClick: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val accent = chartColorForIndex(app.colorIndex, isDark)
    val fraction = (app.usageMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.04f, 1f)
    val overLimit = app.timerLimitMinutes > 0 && app.usageMinutes > app.timerLimitMinutes

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("app_usage_row_${app.id}"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        ) {
            AppIconBadge(iconKey = app.iconKey, colorIndex = app.colorIndex)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatScreenTimeLong(app.usageMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (overLimit) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

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

                if (app.timerLimitMinutes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (overLimit) {
                            "Over ${formatScreenTimeCompact(app.timerLimitMinutes)} timer"
                        } else {
                            "Timer: ${formatScreenTimeCompact(app.timerLimitMinutes)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overLimit) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(
                onClick = onTimerClick,
                modifier = Modifier.testTag("app_timer_button_${app.id}"),
            ) {
                Icon(
                    imageVector = if (app.timerLimitMinutes > 0) Icons.Rounded.HourglassTop
                    else Icons.Outlined.HourglassBottom,
                    contentDescription = stringResource(R.string.cd_app_timer),
                    tint = if (app.timerLimitMinutes > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun EmptyAppUsageCard(onAddApp: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.HourglassBottom,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No apps logged for this day",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add preset or custom apps to build your screen time breakdown and widget.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(14.dp))
            FilledTonalButton(onClick = onAddApp) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add first app")
            }
        }
    }
}
