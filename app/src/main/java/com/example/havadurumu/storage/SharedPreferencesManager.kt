package com.example.havadurumu.storage

import android.content.Context
import androidx.core.content.edit
import com.example.havadurumu.data.WeatherData
import com.google.gson.Gson
//uygulamanın sp işlemlerine yardımcı sınıf
//gson ile Json objelerini çevirip saklar
class SharedPreferencesManager(context : Context, private val gson: Gson) {

    private companion object{
        const val PREF_NAME = "WeatherAppPref"
        const val KEY_CURRENT_LOCATİON = "currentLocation"
    }

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE)
    //Mevcut konum bilgisi jsona dönüp spye kaydedilir
    fun saveCurrentLocation(currentLocation: WeatherData.CurrentLocation){
        val currentLocationJson = gson.toJson(currentLocation)
        sharedPreferences.edit(){
            putString(KEY_CURRENT_LOCATİON,currentLocationJson)
        }
    }
    //kaydedlilen konum bilgisi sp den alır ve tekrar nesneye dönüştürür
    fun getCurrentLocation() : WeatherData.CurrentLocation?{
        return sharedPreferences.getString(
            KEY_CURRENT_LOCATİON,
            null
        )?.let { currentLocationJson ->
            gson.fromJson(currentLocationJson,WeatherData.CurrentLocation::class.java)
        }
    }
}