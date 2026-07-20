package com.yuudev.wisatakebumen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yuudev.wisatakebumen.model.weather.*

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val latLng: String,
    val latitude: Double,
    val longitude: Double,
    val currentTemperature: Double,
    val currentWeatherCode: Int,
    val currentWindSpeed: Double,
    val currentRelativeHumidity: Int,
    val hourlyTime: String,
    val hourlyTemperature2m: String,
    val hourlyWeatherCode: String,
    val hourlyPrecipitationProbability: String,
    val dailyTime: String,
    val dailySunrise: String,
    val dailySunset: String
) {
    fun toWeatherResponse(): WeatherResponse {
        return WeatherResponse(
            latitude = latitude,
            longitude = longitude,
            current = CurrentWeather(currentTemperature, currentWeatherCode, currentWindSpeed, currentRelativeHumidity),
            hourly = HourlyWeather(
                hourlyTime.split(","),
                hourlyTemperature2m.split(",").mapNotNull { it.toDoubleOrNull() },
                hourlyWeatherCode.split(",").mapNotNull { it.toIntOrNull() },
                hourlyPrecipitationProbability.split(",").mapNotNull { it.toIntOrNull() }
            ),
            daily = DailyWeather(
                dailyTime.split(","),
                dailySunrise.split(","),
                dailySunset.split(",")
            )
        )
    }
}
