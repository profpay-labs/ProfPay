package com.profpay.data.config.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.profpay.domain.config.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : ThemeRepository {

    private val _themeValue = MutableStateFlow(
        sharedPreferences.getInt(KEY_THEME, DEFAULT_THEME)
    )

    override val themeValue: StateFlow<Int> = _themeValue.asStateFlow()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME) {
                _themeValue.value = sharedPreferences.getInt(KEY_THEME, DEFAULT_THEME)
            }
        }
    }

    override fun setTheme(value: Int) {
        sharedPreferences.edit { putInt(KEY_THEME, value) }
    }

    private companion object {
        const val KEY_THEME = "valueTheme"
        const val DEFAULT_THEME = 2 // Системная тема по умолчанию
    }
}
