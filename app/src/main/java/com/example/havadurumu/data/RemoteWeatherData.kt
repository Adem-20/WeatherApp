package com.example.havadurumu.data

import com.google.gson.annotations.SerializedName

data class RemoteWeatherData(
    val current: CurrentWeatherRemote,
    val forecast: ForeCastRemote
)
//Apı den gelen verileri temsil eder
data class CurrentWeatherRemote(
    @SerializedName("temp_c") val temperature: Float,
    val condition: WeatherConditionRemote,
    @SerializedName("wind_kph") val wind: Float,
    val humidity: Int
)
//Anlık veriler
data class ForeCastRemote(
    @SerializedName("forecastday") val forecastDay: List<ForeCastDayRemote>
)
//tahmin verileri
data class ForeCastDayRemote(
    val day: DayRemote,
    val hour: List<ForeCastHourRemote>
)
//tahmin gün verileri
data class DayRemote(
    @SerializedName("daily_chance_of_rain") val chanceOfRain: Int
)
//saatlik veriler
data class ForeCastHourRemote(
    val time: String,
    @SerializedName("temp_c") val temperature: Float,
    @SerializedName("feelslike_c") val feelsLikeTemperature: Float,
    val condition: WeatherConditionRemote
)
//görsel
data class WeatherConditionRemote(
    val icon: String
)

