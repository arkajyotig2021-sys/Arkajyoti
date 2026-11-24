package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppUsageEntity
import com.example.data.DailySummaryEntity
import com.example.data.WellbeingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DaySummaryUi(
    val summary: DailySummaryEntity,
    val apps: List<AppUsageEntity>,
    val computedTotalMinutes: Int,
) {
    val autoSumMinutes: Int get() = apps.sumOf { it.usageMinutes }
}

sealed interface ActiveEditorSheet {
    data class EditApp(val app: AppUsageEntity, val focusTimer: Boolean = false) : ActiveEditorSheet
    data object AddApp : ActiveEditorSheet
    data class EditDaySummary(val day: DaySummaryUi) : ActiveEditorSheet
    data object WidgetPreview : ActiveEditorSheet
}

data class WellbeingUiState(
    val isLoading: Boolean = true,
    val selectedDayIndex: Int = 0,
    val days: List<DaySummaryUi> = emptyList(),
    val activeSheet: ActiveEditorSheet? = null,
    val snackbarMessage: String? = null,
) {
    val selectedDay: DaySummaryUi?
        get() = days.firstOrNull { it.summary.dayIndex == selectedDayIndex } ?: days.firstOrNull()

    val todaySummary: DaySummaryUi?
        get() = days.firstOrNull { it.summary.dayIndex == 0 } ?: days.firstOrNull()

    /**
     * Chronological order for the 7-day bar chart (oldest on left, Today on right).
     */
    val chronologicalDays: List<DaySummaryUi>
        get() = days.sortedByDescending { it.summary.dayIndex }
}

class WellbeingViewModel(
    private val repository: WellbeingRepository,
) : ViewModel() {

    private val selectedDayIndex = MutableStateFlow(0)
    private val activeSheet = MutableStateFlow<ActiveEditorSheet?>(null)
    private val snackbarMessage = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    val uiState: StateFlow<WellbeingUiState> = combine(
        repository.dailySummaries,
        repository.allAppUsages,
        selectedDayIndex,
        activeSheet,
        snackbarMessage,
    ) { summaries, apps, selectedIdx, sheet, snack ->
        val appsByDay = apps.groupBy { it.dayIndex }
        val dayItems = summaries.map { summary ->
            val dayApps = (appsByDay[summary.dayIndex] ?: emptyList())
                .sortedWith(compareByDescending<AppUsageEntity> { it.usageMinutes }.thenBy { it.id })
            val total = if (summary.useCustomTotalOverride) {
                summary.customTotalMinutes
            } else {
                dayApps.sumOf { it.usageMinutes }
            }
            DaySummaryUi(
                summary = summary,
                apps = dayApps,
                computedTotalMinutes = total,
            )
        }
        WellbeingUiState(
            isLoading = summaries.isEmpty(),
            selectedDayIndex = selectedIdx,
            days = dayItems,
            activeSheet = sheet,
            snackbarMessage = snack,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WellbeingUiState(),
    )

    fun selectDay(dayIndex: Int) {
        selectedDayIndex.value = dayIndex
    }

    fun openEditAppSheet(app: AppUsageEntity, focusTimer: Boolean = false) {
        activeSheet.value = ActiveEditorSheet.EditApp(app, focusTimer)
    }

    fun openAddAppSheet() {
        activeSheet.value = ActiveEditorSheet.AddApp
    }

    fun openEditDaySheet(day: DaySummaryUi) {
        activeSheet.value = ActiveEditorSheet.EditDaySummary(day)
    }

    fun openWidgetPreviewSheet() {
        activeSheet.value = ActiveEditorSheet.WidgetPreview
    }

    fun dismissSheet() {
        activeSheet.value = null
    }

    fun clearSnackbar() {
        snackbarMessage.value = null
    }

    fun notifyMessage(message: String) {
        snackbarMessage.value = message
    }

    fun saveEditedApp(updatedApp: AppUsageEntity) {
        viewModelScope.launch {
            repository.updateAppUsage(updatedApp)
            activeSheet.value = null
            snackbarMessage.value = "Updated ${updatedApp.appName} • Widget synced"
        }
    }

    fun deleteApp(app: AppUsageEntity) {
        viewModelScope.launch {
            repository.deleteAppUsage(app.id)
            activeSheet.value = null
            snackbarMessage.value = "Removed ${app.appName} • Widget synced"
        }
    }

    fun addNewApp(
        appName: String,
        category: String,
        iconKey: String,
        colorIndex: Int,
        usageMinutes: Int,
        timerLimitMinutes: Int,
    ) {
        val cleanName = appName.trim()
        if (cleanName.isEmpty()) return
        val targetDay = selectedDayIndex.value
        viewModelScope.launch {
            repository.addAppUsage(
                AppUsageEntity(
                    dayIndex = targetDay,
                    appName = cleanName,
                    category = category.ifBlank { "Utilities" },
                    iconKey = iconKey,
                    colorIndex = colorIndex,
                    usageMinutes = usageMinutes.coerceAtLeast(0),
                    timerLimitMinutes = timerLimitMinutes.coerceAtLeast(0),
                )
            )
            activeSheet.value = null
            snackbarMessage.value = "Added $cleanName • Widget synced"
        }
    }

    fun saveDaySummary(updatedSummary: DailySummaryEntity) {
        viewModelScope.launch {
            repository.updateDailySummary(updatedSummary)
            activeSheet.value = null
            snackbarMessage.value = "Saved ${updatedSummary.dayLabel} summary • Widget synced"
        }
    }

    fun resetDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
            selectedDayIndex.value = 0
            activeSheet.value = null
            snackbarMessage.value = "Restored sample data • Widget synced"
        }
    }

    class Factory(private val repository: WellbeingRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WellbeingViewModel(repository) as T
        }
    }
}
