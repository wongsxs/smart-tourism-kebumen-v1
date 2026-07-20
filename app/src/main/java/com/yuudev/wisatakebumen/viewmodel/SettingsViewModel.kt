package com.yuudev.wisatakebumen.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {

    private val _themeState = MutableStateFlow(ThemeOption.SYSTEM)
    val themeState: StateFlow<ThemeOption> = _themeState.asStateFlow()

    private val _locationEnabled = MutableStateFlow(false)
    val locationEnabled: StateFlow<Boolean> = _locationEnabled.asStateFlow()

    private val _tripNotificationEnabled = MutableStateFlow(true)
    val tripNotificationEnabled: StateFlow<Boolean> = _tripNotificationEnabled.asStateFlow()

    private val _weatherNotificationEnabled = MutableStateFlow(true)
    val weatherNotificationEnabled: StateFlow<Boolean> = _weatherNotificationEnabled.asStateFlow()

    fun setTheme(themeOption: ThemeOption) {
        _themeState.value = themeOption
    }

    fun setLocationEnabled(enabled: Boolean) {
        _locationEnabled.value = enabled
    }

    fun setTripNotificationEnabled(enabled: Boolean) {
        _tripNotificationEnabled.value = enabled
    }

    fun setWeatherNotificationEnabled(enabled: Boolean) {
        _weatherNotificationEnabled.value = enabled
    }
}

enum class ThemeOption(val title: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("Ikuti Sistem")
}
