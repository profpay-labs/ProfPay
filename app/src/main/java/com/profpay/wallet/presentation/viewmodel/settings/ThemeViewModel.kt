package com.profpay.wallet.presentation.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.domain.config.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository,
) : ViewModel() {

    val state: StateFlow<ThemeState> = themeRepository.themeValue
        .map { themeValue -> ThemeState.Success(themeValue) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeState.Success(themeRepository.themeValue.value),
        )

    /**
     * Установить тему.
     * @param themeValue 0 = светлая, 1 = тёмная, 2 = системная
     */
    fun setTheme(themeValue: Int) {
        themeRepository.setTheme(themeValue)
    }

    /**
     * Определяет, использовать ли тёмную тему.
     *
     * @param sharedThemeInt 0 = светлая, 1 = тёмная, 2 = системная
     * @param systemDarkTheme текущая системная тема
     */
    fun isDarkTheme(
        sharedThemeInt: Int,
        systemDarkTheme: Boolean,
    ): Boolean = when (sharedThemeInt) {
        THEME_LIGHT -> false
        THEME_DARK -> true
        THEME_SYSTEM -> systemDarkTheme
        else -> systemDarkTheme
    }

    companion object {
        const val THEME_LIGHT = 0
        const val THEME_DARK = 1
        const val THEME_SYSTEM = 2
    }
}

sealed interface ThemeState {
    data object Loading : ThemeState
    data class Success(val themeStateResult: Int) : ThemeState
}
