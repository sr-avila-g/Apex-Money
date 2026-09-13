package com.sravila.apexmoney.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sravila.apexmoney.core.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "apex_money_user_prefs")


data class UserPreferences(
    val themeMode: ThemeMode,
    val currencySymbol: String,
    val currencyCode: String,
    val isDiscreetMode: Boolean,
    val isHapticEnabled: Boolean,
    val isBiometricEnabled: Boolean,
    val isRoundUpEnabled: Boolean,
    val isStrictBudget: Boolean,
    val isAutoReversalEnabled: Boolean,
    val lastSyncTimestamp: String,
    val isCloudSyncEnabled: Boolean,
    val customSupabaseUrl: String,
    val customSupabaseKey: String,
    // Nuevas preferencias v2.0
    val showAccountsCarousel: Boolean,
    val isCreditCardSupportEnabled: Boolean,
    val showNetBalanceWarning: Boolean,
    val accountColorBadges: Boolean
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val CURRENCY_CODE = stringPreferencesKey("currency_code")
        val DISCREET_MODE = booleanPreferencesKey("discreet_mode")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val ROUND_UP_ENABLED = booleanPreferencesKey("round_up_enabled")
        val STRICT_BUDGET = booleanPreferencesKey("strict_budget")
        val AUTO_REVERSAL = booleanPreferencesKey("auto_reversal")
        val LAST_SYNC = stringPreferencesKey("last_sync")
        val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
        val CUSTOM_SUPABASE_URL = stringPreferencesKey("custom_supabase_url")
        val CUSTOM_SUPABASE_KEY = stringPreferencesKey("custom_supabase_key")
        // v2.0
        val SHOW_ACCOUNTS_CAROUSEL = booleanPreferencesKey("show_accounts_carousel")
        val CREDIT_CARD_SUPPORT = booleanPreferencesKey("credit_card_support")
        val SHOW_NET_BALANCE_WARNING = booleanPreferencesKey("show_net_balance_warning")
        val ACCOUNT_COLOR_BADGES = booleanPreferencesKey("account_color_badges")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.AMOLED.name
            val themeMode = try {
                ThemeMode.valueOf(themeStr)
            } catch (e: Exception) {
                ThemeMode.AMOLED
            }
            UserPreferences(
                themeMode = themeMode,
                currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: "$",
                currencyCode = preferences[PreferencesKeys.CURRENCY_CODE] ?: "USD",
                isDiscreetMode = preferences[PreferencesKeys.DISCREET_MODE] ?: false,
                isHapticEnabled = preferences[PreferencesKeys.HAPTIC_ENABLED] ?: true,
                isBiometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
                isRoundUpEnabled = preferences[PreferencesKeys.ROUND_UP_ENABLED] ?: false,
                isStrictBudget = preferences[PreferencesKeys.STRICT_BUDGET] ?: false,
                isAutoReversalEnabled = preferences[PreferencesKeys.AUTO_REVERSAL] ?: true,
                lastSyncTimestamp = preferences[PreferencesKeys.LAST_SYNC] ?: "1970-01-01T00:00:00Z",
                isCloudSyncEnabled = preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] ?: false,
                customSupabaseUrl = preferences[PreferencesKeys.CUSTOM_SUPABASE_URL] ?: "",
                customSupabaseKey = preferences[PreferencesKeys.CUSTOM_SUPABASE_KEY] ?: "",
                showAccountsCarousel = preferences[PreferencesKeys.SHOW_ACCOUNTS_CAROUSEL] ?: true,
                isCreditCardSupportEnabled = preferences[PreferencesKeys.CREDIT_CARD_SUPPORT] ?: true,
                showNetBalanceWarning = preferences[PreferencesKeys.SHOW_NET_BALANCE_WARNING] ?: true,
                accountColorBadges = preferences[PreferencesKeys.ACCOUNT_COLOR_BADGES] ?: true
            )
        }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun updateCurrency(symbol: String, code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = symbol
            preferences[PreferencesKeys.CURRENCY_CODE] = code
        }
    }
    
    suspend fun updateBooleanPreference(key: String, value: Boolean) {
        context.dataStore.edit { preferences ->
            when(key) {
                "discreet" -> preferences[PreferencesKeys.DISCREET_MODE] = value
                "haptic" -> preferences[PreferencesKeys.HAPTIC_ENABLED] = value
                "biometric" -> preferences[PreferencesKeys.BIOMETRIC_ENABLED] = value
                "round_up" -> preferences[PreferencesKeys.ROUND_UP_ENABLED] = value
                "strict_budget" -> preferences[PreferencesKeys.STRICT_BUDGET] = value
                "auto_reversal" -> preferences[PreferencesKeys.AUTO_REVERSAL] = value
                "cloud_sync" -> preferences[PreferencesKeys.CLOUD_SYNC_ENABLED] = value
                "show_accounts_carousel" -> preferences[PreferencesKeys.SHOW_ACCOUNTS_CAROUSEL] = value
                "credit_card_support" -> preferences[PreferencesKeys.CREDIT_CARD_SUPPORT] = value
                "show_net_balance_warning" -> preferences[PreferencesKeys.SHOW_NET_BALANCE_WARNING] = value
                "account_color_badges" -> preferences[PreferencesKeys.ACCOUNT_COLOR_BADGES] = value
            }
        }
    }

    suspend fun updateLastSync(timestamp: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC] = timestamp
        }
    }

    suspend fun updateSupabaseCredentials(url: String, key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_SUPABASE_URL] = url
            preferences[PreferencesKeys.CUSTOM_SUPABASE_KEY] = key
        }
    }
}
