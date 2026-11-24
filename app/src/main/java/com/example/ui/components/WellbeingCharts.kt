package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AppUsageEntity
import com.example.data.formatScreenTimeCompact
import com.example.data.formatScreenTimeLong
import com.example.ui.DaySummaryUi
import com.example.ui.theme.chartColorForIndex

fun iconForKey(key: String): ImageVector {
    return when (key) {
        "video" -> Icons.Rounded.PlayCircle
        "photo_social" -> Icons.Rounded.PhotoCamera
        "browser" -> Icons.Rounded.Public
        "chat" -> Icons.Rounded.ChatBubble
        "music" -> Icons.Rounded.MusicNote
        "mail" -> Icons.Rounded.Mail
        "map" -> Icons.Rounded.Map
        "movie" -> Icons.Rounded.Movie
        "forum" -> Icons.Rounded.Forum
        "tag" -> Icons.Rounded.Tag
        "image" -> Icons.Rounded.PhotoLibrary
        "gamepad" -> Icons.Rounded.SportsEsports
        else -> Icons.Rounded.Apps
    }
}

@Composable
fun AppIconBadge(
    iconKey: String,
    colorIndex: Int,
    size: Dp = 42.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val accent = chartColorForIndex(colorIndex, isDark)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = if (isDark) 0.22f else 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconForKey(iconKey),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun ScreenTimeDonutChart(
    dayLabel: String,
    totalMinutes: Int,
    apps: List<AppUsageEntity>,
    isCustomOverride: Boolean,
    onCenterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val activeApps = apps.filter { it.usageMinutes > 0 }
    val appSum = activeApps.sumOf { it.usageMinutes }

    val sweepProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "donut_sweep",
    )

    Box(
        modifier = modifier
            .size(236.dp)
            .clip(CircleShape)
            .clickable(onClick = onCenterClick)
            .testTag("donut_chart_hero"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            val strokeWidth = 22.dp.toPx()
            val arcDiameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(arcDiameter, arcDiameter)
            val strokeStyle = Stroke(width = strokeWidth, cap = StrokeCap.Butt)

            // Base track ring
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = strokeStyle,
            )

            if (appSum > 0 && activeApps.isNotEmpty()) {
                val gapDegrees = if (activeApps.size > 1) 2.5f else 0f
                val totalGap = gapDegrees * activeApps.size
                val availableDegrees = (360f - totalGap).coerceAtLeast(180f)
                var currentAngle = -90f

                activeApps.forEach { app ->
                    val fraction = app.usageMinutes.toFloat() / appSum.toFloat()
                    val segmentSweep = (fraction * availableDegrees * sweepProgress).coerceAtLeast(2f)
                    drawArc(
                        color = chartColorForIndex(app.colorIndex, isDark),
                        startAngle = currentAngle,
                        sweepAngle = segmentSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = strokeStyle,
                    )
                    currentAngle += segmentSweep + gapDegrees
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 36.dp),
        ) {
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatScreenTimeLong(totalMinutes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun WeeklyBarChart(
    chronologicalDays: List<DaySummaryUi>,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxMinutes = chronologicalDays.maxOfOrNull { it.computedTotalMinutes }?.coerceAtLeast(60) ?: 60

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        chronologicalDays.forEach { day ->
            val isSelected = day.summary.dayIndex == selectedDayIndex
            val targetRatio = (day.computedTotalMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.06f, 1f)
            val animatedRatio by animateFloatAsState(
                targetValue = targetRatio,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "bar_height_${day.summary.dayIndex}",
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectDay(day.summary.dayIndex) }
                    .padding(vertical = 4.dp)
                    .testTag("week_day_bar_${day.summary.dayIndex}"),
            ) {
                Text(
                    text = if (isSelected) formatScreenTimeCompact(day.computedTotalMinutes) else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .fillMaxHeight(animatedRatio)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            ),
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = day.summary.shortLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
