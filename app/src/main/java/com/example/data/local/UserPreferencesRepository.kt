package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.AppLanguage
import com.example.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.surakshaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "suraksha_user_preferences"
)

data class UserDisplayPreferences(
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val hasCompletedLanguageOnboarding: Boolean = false,
    val followedCaseIds: Set<String> = DEFAULT_FOLLOWED_CASE_IDS,
    val notificationsEnabled: Boolean = true,
    val sensitiveBlurEnabled: Boolean = false
) {
    companion object {
        val DEFAULT_FOLLOWED_CASE_IDS = setOf(
            "case_ap_school_chainsaw_01",
            "case_wb_kolkata_medical_03",
            "case_mh_badlapur_school_02"
        )
    }
}

class UserPreferencesRepository(private val context: Context) {

    private val dataStore = context.applicationContext.surakshaDataStore

    companion object {
        private val KEY_LANGUAGE_CODE = stringPreferencesKey("app_language_code")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        private val KEY_COMPLETED_ONBOARDING = booleanPreferencesKey("completed_language_onboarding")
        private val KEY_FOLLOWED_CASE_IDS = stringSetPreferencesKey("followed_case_ids")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_SENSITIVE_BLUR_ENABLED = booleanPreferencesKey("sensitive_blur_enabled")

        const val MIN_FONT_SCALE = 0.85f
        const val MAX_FONT_SCALE = 1.6f
    }

    val preferencesFlow: Flow<UserDisplayPreferences> = dataStore.data.map { prefs ->
        val langCode = prefs[KEY_LANGUAGE_CODE]
        val themeStr = prefs[KEY_THEME_MODE]
        val rawScale = prefs[KEY_FONT_SCALE] ?: 1.0f
        val completedOnboarding = prefs[KEY_COMPLETED_ONBOARDING] ?: (langCode != null)
        val followedIds = prefs[KEY_FOLLOWED_CASE_IDS] ?: UserDisplayPreferences.DEFAULT_FOLLOWED_CASE_IDS
        val notificationsEnabled = prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
        val sensitiveBlurEnabled = prefs[KEY_SENSITIVE_BLUR_ENABLED] ?: false

        UserDisplayPreferences(
            appLanguage = AppLanguage.fromCode(langCode),
            themeMode = ThemeMode.entries.find { it.name == themeStr } ?: ThemeMode.SYSTEM,
            fontScale = rawScale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE),
            hasCompletedLanguageOnboarding = completedOnboarding,
            followedCaseIds = followedIds,
            notificationsEnabled = notificationsEnabled,
            sensitiveBlurEnabled = sensitiveBlurEnabled
        )
    }

    /**
     * Reads preferences synchronously before the first frame so there is no flash of wrong language/theme.
     */
    fun readInitialPreferencesBlocking(): UserDisplayPreferences {
        return try {
            runBlocking {
                preferencesFlow.first()
            }
        } catch (_: Exception) {
            UserDisplayPreferences()
        }
    }

    suspend fun setAppLanguage(language: AppLanguage, markOnboardingComplete: Boolean = true) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE_CODE] = language.code
            if (markOnboardingComplete) {
                prefs[KEY_COMPLETED_ONBOARDING] = true
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { prefs ->
            prefs[KEY_FONT_SCALE] = scale.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
        }
    }

    suspend fun setFollowedCaseIds(caseIds: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_FOLLOWED_CASE_IDS] = caseIds
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSensitiveBlurEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_SENSITIVE_BLUR_ENABLED] = enabled
        }
    }

    suspend fun completeLanguageOnboarding(language: AppLanguage? = null) {
        dataStore.edit { prefs ->
            if (language != null) {
                prefs[KEY_LANGUAGE_CODE] = language.code
            }
            prefs[KEY_COMPLETED_ONBOARDING] = true
        }
    }
}
