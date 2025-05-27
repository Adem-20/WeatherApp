package com.example.havadurumu.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Mühürlü Sınıf
sealed class WeatherData {

    data class CurrentLocation(
        val date : String = getCurrentDate(),
        val location : String = "Konumunuzu seçin",
        val latitude : Double? = null,
        val longitude : Double? = null
    ) :WeatherData()
}
    //Cihazın anlık tarih ve saati alınıyor
    private fun getCurrentDate() : String{
        val currentDate = Date()
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        return "Today, ${formatter.format(currentDate)}"
    }
