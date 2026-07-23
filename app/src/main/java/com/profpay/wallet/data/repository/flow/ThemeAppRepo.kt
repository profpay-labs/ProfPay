package com.profpay.wallet.data.repository.flow

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeAppRepo @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) {
    // Используем StateFlow для гарантированного начального значения
    private val _isDarkTheme = MutableStateFlow(
        sharedPreferences.getInt(KEY_THEME, DEFAULT_THEME)
    )

    val isDarkTheme: StateFlow<Int> = _isDarkTheme.asStateFlow()

    init {
        // Слушаем изменения
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME) {
                _isDarkTheme.value = sharedPreferences.getInt(KEY_THEME, DEFAULT_THEME)
            }
        }
    }

    /**
     * Установить тему.
     * @param themeValue 0 = светлая, 1 = тёмная, 2 = системная
     */
    fun setTheme(themeValue: Int) {
        sharedPreferences.edit { putInt(KEY_THEME, themeValue) }
    }

    private companion object {
        const val KEY_THEME = "valueTheme"
        const val DEFAULT_THEME = 2 // Системная тема по умолчанию
    }
}
