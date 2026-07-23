package com.waitasec.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wait_a_sec_settings")

class RestrictedAppsRepository(private val context: Context) {

    private object Keys {
        val restrictedPackages = stringSetPreferencesKey("restricted_packages")
        val delaySeconds = intPreferencesKey("delay_seconds")
        val protectionEnabled = booleanPreferencesKey("protection_enabled")
        val onboardingComplete = booleanPreferencesKey("onboarding_complete")
    }

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            restrictedPackages = prefs[Keys.restrictedPackages] ?: emptySet(),
            delaySeconds = (prefs[Keys.delaySeconds] ?: UserSettings.DEFAULT_DELAY_SECONDS)
                .coerceIn(UserSettings.MIN_DELAY_SECONDS, UserSettings.MAX_DELAY_SECONDS),
            protectionEnabled = prefs[Keys.protectionEnabled] ?: true,
            onboardingComplete = prefs[Keys.onboardingComplete] ?: false,
        )
    }

    /** Blocking snapshot for AccessibilityService on the main thread. */
    fun currentSettingsBlocking(): UserSettings = runBlocking { settings.first() }

    suspend fun setRestrictedPackages(packages: Set<String>) {
        context.dataStore.edit { it[Keys.restrictedPackages] = packages }
    }

    suspend fun togglePackage(packageName: String, selected: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.restrictedPackages]?.toMutableSet() ?: mutableSetOf()
            if (selected) current.add(packageName) else current.remove(packageName)
            prefs[Keys.restrictedPackages] = current
        }
    }

    suspend fun setDelaySeconds(seconds: Int) {
        context.dataStore.edit {
            it[Keys.delaySeconds] = seconds.coerceIn(
                UserSettings.MIN_DELAY_SECONDS,
                UserSettings.MAX_DELAY_SECONDS,
            )
        }
    }

    suspend fun setProtectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.protectionEnabled] = enabled }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[Keys.onboardingComplete] = complete }
    }
}
