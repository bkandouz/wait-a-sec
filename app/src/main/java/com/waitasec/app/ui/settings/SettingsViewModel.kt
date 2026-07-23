package com.waitasec.app.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.waitasec.app.WaitASecApp
import com.waitasec.app.data.AppInfo
import com.waitasec.app.data.UserSettings
import com.waitasec.app.util.AccessibilityHelper
import com.waitasec.app.util.InstalledAppsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    application: Application,
    private val app: WaitASecApp = application as WaitASecApp,
) : AndroidViewModel(application) {

    val settings: StateFlow<UserSettings> = app.repository.settings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserSettings(),
    )

    var installedApps by mutableStateOf<List<AppInfo>>(emptyList())
        private set

    var appsLoading by mutableStateOf(true)
        private set

    var accessibilityEnabled by mutableStateOf(false)
        private set

    var searchQuery by mutableStateOf("")
        private set

    init {
        refreshAccessibilityStatus()
        loadApps()
    }

    fun refreshAccessibilityStatus() {
        accessibilityEnabled = AccessibilityHelper.isServiceEnabled(getApplication())
    }

    fun openAccessibilitySettings() {
        AccessibilityHelper.openAccessibilitySettings(getApplication())
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun filteredApps(): List<AppInfo> {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) return installedApps
        return installedApps.filter {
            it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            appsLoading = true
            installedApps = withContext(Dispatchers.IO) {
                InstalledAppsHelper.loadLauncherApps(getApplication())
            }
            appsLoading = false
        }
    }

    fun togglePackage(packageName: String, selected: Boolean) {
        viewModelScope.launch {
            app.repository.togglePackage(packageName, selected)
        }
    }

    fun setDelaySeconds(seconds: Int) {
        viewModelScope.launch {
            app.repository.setDelaySeconds(seconds)
        }
    }

    fun setProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            app.repository.setProtectionEnabled(enabled)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            app.repository.setOnboardingComplete(true)
        }
    }

    class Factory(private val app: WaitASecApp) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(app) as T
        }
    }
}
