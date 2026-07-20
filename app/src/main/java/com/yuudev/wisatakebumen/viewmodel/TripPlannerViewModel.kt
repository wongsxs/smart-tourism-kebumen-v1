package com.yuudev.wisatakebumen.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuudev.wisatakebumen.model.TripPlanResponse
import com.yuudev.wisatakebumen.model.TripRequest
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.model.WisataWeather
import com.yuudev.wisatakebumen.network.RetrofitClient
import com.yuudev.wisatakebumen.viewmodel.WeatherViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class TripPlannerViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var tripPlanResult by mutableStateOf<TripPlanResponse?>(null)
        private set

    fun generateTrip(
        request: TripRequest,
        wisataList: List<Wisata>,
        weatherViewModel: WeatherViewModel
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val weatherDataList = wisataList.map { wisata ->
                    async {
                        val res = weatherViewModel.getOrFetchWeather(wisata.lat, wisata.lng)
                        if (res != null) {
                            val (weatherDesc, _) = WeatherViewModel.getWeatherInfo(res.current.weatherCode)
                            WisataWeather(
                                idWisata = wisata.id,
                                weather = weatherDesc,
                                temperature = res.current.temperature,
                                humidity = res.current.relativeHumidity,
                                windSpeed = res.current.windSpeed,
                                weatherCode = res.current.weatherCode,
                                rainProbability = res.hourly?.precipitationProbability?.firstOrNull() ?: 0
                            )
                        } else null
                    }
                }.awaitAll().filterNotNull()

                val finalRequest = request.copy(weather = weatherDataList)
                val response = RetrofitClient.api.getTripPlan(finalRequest)
                tripPlanResult = response
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Gagal memuat rencana perjalanan. Pastikan koneksi internet stabil atau hubungi developer."
            } finally {
                isLoading = false
            }
        }
    }
    
    fun resetPlan() {
        tripPlanResult = null
        errorMessage = null
    }
}
