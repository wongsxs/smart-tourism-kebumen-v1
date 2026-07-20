package com.yuudev.wisatakebumen.viewmodel

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yuudev.wisatakebumen.model.weather.WeatherResponse
import com.yuudev.wisatakebumen.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherState {
    object Idle : WeatherState()
    object Loading : WeatherState()
    data class Success(val data: WeatherResponse) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application)

    private val _weatherData = MutableStateFlow<Map<String, WeatherState>>(emptyMap())
    val weatherData: StateFlow<Map<String, WeatherState>> = _weatherData.asStateFlow()

    fun fetchWeather(lat: Double, lng: Double, force: Boolean = false) {
        val key = "${lat},${lng}"
        
        if (!force && _weatherData.value[key] is WeatherState.Success) return

        _weatherData.value = _weatherData.value.toMutableMap().apply {
            put(key, WeatherState.Loading)
        }

        viewModelScope.launch {
            try {
                val response = repository.getWeather(lat, lng, force)
                _weatherData.value = _weatherData.value.toMutableMap().apply {
                    put(key, WeatherState.Success(response))
                }
            } catch (e: Exception) {
                _weatherData.value = _weatherData.value.toMutableMap().apply {
                    put(key, WeatherState.Error(e.localizedMessage ?: "Gagal mengambil data cuaca"))
                }
            }
        }
    }

    suspend fun getOrFetchWeather(lat: Double, lng: Double): WeatherResponse? {
        val key = "${lat},${lng}"
        val currentState = _weatherData.value[key]
        if (currentState is WeatherState.Success) {
            return currentState.data
        }
        
        return try {
            val response = repository.getWeather(lat, lng)
            _weatherData.value = _weatherData.value.toMutableMap().apply {
                put(key, WeatherState.Success(response))
            }
            response
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        fun getWeatherInfo(weatherCode: Int): Pair<String, ImageVector> {
            return when (weatherCode) {
                0 -> "Cerah" to Icons.Rounded.WbSunny
                1, 2, 3 -> "Berawan" to Icons.Rounded.Cloud
                45, 48 -> "Berkabut" to Icons.Rounded.Cloud
                51, 53, 55, 56, 57 -> "Gerimis" to Icons.Rounded.Cloud
                61, 63, 65, 66, 67 -> "Hujan" to Icons.Rounded.WbCloudy
                71, 73, 75, 77 -> "Bersalju" to Icons.Rounded.Cloud
                80, 81, 82 -> "Hujan Lebat" to Icons.Rounded.WbCloudy
                85, 86 -> "Badai Salju" to Icons.Rounded.Thunderstorm
                95, 96, 99 -> "Badai Petir" to Icons.Rounded.Thunderstorm
                else -> "Tidak Diketahui" to Icons.Rounded.WbSunny
            }
        }
    }
}
