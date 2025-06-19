package com.example.havadurumu.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Mühürlü Sınıf
sealed class WeatherData {
    data class CurrentLocation(
        val date: String = getCurrentDate(),
        val location: String = "Konumunuzu seçin",
        val latitude: Double? = null,
        val longitude: Double? = null
    ) : WeatherData()
}

//Gösterilecek veriler
data class CurrentWeather(
    val icon: String,
    val temperature: Float,
    val wind: Float,
    val humidity: Int,
    val chanceOfRain: Int
) : WeatherData()

//Zamana göre hava durumu verisi
data class ForeCast(
    val time: String,
    val temperatures: Float,
    val feelsLikeTemperature: Float,
    val icon: String
) : WeatherData()

//Cihazın anlık tarih ve saati alınıyor
private fun getCurrentDate(): String {
    val currentDate = Date()
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return "Today, ${formatter.format(currentDate)}"
}
