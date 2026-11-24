package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.WellbeingApplication
import com.example.data.TodayWidgetSnapshot
import com.example.data.formatScreenTimeCompact
import com.example.data.formatScreenTimeLong
import com.example.ui.theme.chartColorForIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ScreenTimeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as WellbeingApplication
                val snapshot = app.repository.getTodayWidgetSnapshot()
                for (widgetId in appWidgetIds) {
                    val views = buildRemoteViews(context, snapshot)
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun buildRemoteViews(
        context: Context,
        snapshot: TodayWidgetSnapshot,
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_screen_time)

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        views.setTextViewText(R.id.widget_day_pill, snapshot.dayLabel)
        views.setTextViewText(R.id.widget_total_time, formatScreenTimeLong(snapshot.totalMinutes))

        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val trackColor = ContextCompat.getColor(context, R.color.widget_bar_track)

        val rowIds = listOf(
            RowViewIds(
                R.id.widget_app_row_1,
                R.id.widget_app_dot_1,
                R.id.widget_app_name_1,
                R.id.widget_app_time_1,
                R.id.widget_app_bar_1,
            ),
            RowViewIds(
                R.id.widget_app_row_2,
                R.id.widget_app_dot_2,
                R.id.widget_app_name_2,
                R.id.widget_app_time_2,
                R.id.widget_app_bar_2,
            ),
            RowViewIds(
                R.id.widget_app_row_3,
                R.id.widget_app_dot_3,
                R.id.widget_app_name_3,
                R.id.widget_app_time_3,
                R.id.widget_app_bar_3,
            ),
        )

        if (snapshot.topApps.isEmpty()) {
            views.setViewVisibility(R.id.widget_empty_state, View.VISIBLE)
            rowIds.forEach { views.setViewVisibility(it.rowId, View.GONE) }
        } else {
            views.setViewVisibility(R.id.widget_empty_state, View.GONE)
            val maxMinutes = snapshot.topApps.maxOf { it.usageMinutes }.coerceAtLeast(1)

            rowIds.forEachIndexed { index, ids ->
                val app = snapshot.topApps.getOrNull(index)
                if (app == null) {
                    views.setViewVisibility(ids.rowId, View.GONE)
                } else {
                    views.setViewVisibility(ids.rowId, View.VISIBLE)
                    val accentColor = chartColorForIndex(app.colorIndex, isNight).toArgb()
                    views.setTextViewText(ids.nameId, app.appName)
                    views.setTextViewText(ids.timeId, formatScreenTimeCompact(app.usageMinutes))
                    views.setImageViewBitmap(ids.dotId, createCircleBitmap(accentColor, 24))
                    val fraction = (app.usageMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.08f, 1f)
                    views.setImageViewBitmap(
                        ids.barId,
                        createProgressBarBitmap(
                            fillColor = accentColor,
                            trackColor = trackColor,
                            fraction = fraction,
                        ),
                    )
                }
            }
        }

        return views
    }

    private data class RowViewIds(
        val rowId: Int,
        val dotId: Int,
        val nameId: Int,
        val timeId: Int,
        val barId: Int,
    )

    private fun createCircleBitmap(color: Int, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val radius = sizePx / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        return bitmap
    }

    private fun createProgressBarBitmap(
        fillColor: Int,
        trackColor: Int,
        fraction: Float,
    ): Bitmap {
        val width = 360
        val height = 14
        val radius = height / 2f
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = trackColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, trackPaint)

        val fillWidth = (width * fraction).coerceAtLeast(height.toFloat())
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(0f, 0f, fillWidth, height.toFloat()), radius, radius, fillPaint)

        return bitmap
    }

    companion object {
        fun refreshAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, ScreenTimeWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (widgetIds.isNotEmpty()) {
                val updateIntent = Intent(context, ScreenTimeWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                context.sendBroadcast(updateIntent)
            }
        }

        fun isPinSupported(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.isRequestPinAppWidgetSupported
        }

        fun requestPinWidget(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (!appWidgetManager.isRequestPinAppWidgetSupported) return false
            val provider = ComponentName(context, ScreenTimeWidgetProvider::class.java)
            return appWidgetManager.requestPinAppWidget(provider, null, null)
        }
    }
}
