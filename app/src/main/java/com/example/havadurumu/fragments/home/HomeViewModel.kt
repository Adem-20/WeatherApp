package com.example.havadurumu.fragments.home

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.havadurumu.data.CurrentWeather
import com.example.havadurumu.data.CurrentWeatherRemote
import com.example.havadurumu.data.ForeCast
import com.example.havadurumu.data.LiveDataEvent
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.network.repository.WeatherDataRepository
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import java.lang.Error
import java.text.SimpleDateFormat
import java.util.Locale

class HomeViewModel (private val weatherDataRepository: WeatherDataRepository) : ViewModel() {

    private val _weatherData = MutableLiveData<LiveDataEvent<WeatherDataState>>()
    val weatherData: LiveData<LiveDataEvent<WeatherDataState>> get() = _weatherData

    //Vew modelden verileri alıtyoruz
    fun getWeatherData(latitude: Double, longitude: Double){
        viewModelScope.launch {
            emitWeatherDataUiState(isLoading = true)
            weatherDataRepository.getWeatherData(latitude, longitude)?.let { weatherData ->
                emitWeatherDataUiState(
                    currentWeather = CurrentWeather(
                        icon = weatherData.current.condition.icon,
                        temperature = weatherData.current.temperature,
                        wind = weatherData.current.wind,
                        humidity = weatherData.current.humidity,
                        chanceOfRain = weatherData.forecast.forecastDay.first().day.chanceOfRain
                    ),
                    //forecate dönüştür
                    forecast = weatherData.forecast.forecastDay.first().hour.map {
                        ForeCast(
                            time = getForecastTime(it.time),
                            temperatures = it.temperature,
                            feelsLikeTemperature = it.feelsLikeTemperature,
                            icon = it.condition.icon
                        )
                    }
                )
            } ?: emitWeatherDataUiState(error = "Hava durumu verileri alınamadı.")
        }
    }
    //uıya göndereceğimiz durum bilgisi
    private fun emitWeatherDataUiState(
        isLoading: Boolean = false,
        currentWeather: CurrentWeather? = null,
        forecast: List<ForeCast>? = null,
        error: String? = null
    ){
        val weatherDataState = WeatherDataState(isLoading,currentWeather,forecast, error)
        _weatherData.value = LiveDataEvent(weatherDataState)
    }
    //UI katmanı için durum sınıfı (yükleniyor,başarlı,hata)
    data class CurrentLocationDataState(
        val isLoading: Boolean,
        val currentLocation: WeatherData.CurrentLocation?,
        val error: String?
    )
    //Konumun UI da görüntülenebilmesi için LiveData
    private val _currentLocation = MutableLiveData<LiveDataEvent<CurrentLocationDataState>>()
    val currentLocation:LiveData<LiveDataEvent<CurrentLocationDataState>> get() = _currentLocation
    //Kullanıcıdan adres bilgisi alınır ve adrese çevrilir
    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,
        geocoder: Geocoder
    ){
        //Coroutine başlatılıyor
        viewModelScope.launch {
            emitCurrentLocationUiState(isLoading = true)//Yükleniyor durumunu gönder
            weatherDataRepository.getCurrentLocation(
                fusedLocationProviderClient = fusedLocationProviderClient,
                onSuccess = {currentLocation ->
                    //Konum başarılı bir şekilde alındıysa adres bilgisi ile güncelle
                    uptadeAddressText(currentLocation,geocoder)
                },
                onFailure = {
                    //Konum alınamazsa hata mesajı göster
                    emitCurrentLocationUiState(error = "Konum izni alınamadı")
                }
            )
        }
    }
    //Konumdan adres bilgisini alıp UI a aktarır
    private fun uptadeAddressText(currentLocation: WeatherData.CurrentLocation,geocoder: Geocoder){
        viewModelScope.launch {
            runCatching {
                weatherDataRepository.uptadeAddressText(currentLocation,geocoder)
            }.onSuccess {location ->
                emitCurrentLocationUiState(currentLocation = location)
            }.onFailure {
                emitCurrentLocationUiState(
                    currentLocation = currentLocation.copy(
                        location = "Bilinmiyor"
                    )
                )
            }
        }
    }
    //Uı a gönderilecek durum LiveData üzerinden(Otomatik)
    private fun emitCurrentLocationUiState(
        isLoading : Boolean = false,
        currentLocation: WeatherData.CurrentLocation? = null,
        error: String? = null
    ){
        val currentLocationDataState = CurrentLocationDataState(isLoading,currentLocation,error)
        _currentLocation.value = LiveDataEvent(currentLocationDataState)
    }
    //uı ın izleyeceği veri sınıfı
    data class WeatherDataState(
        val isLoading: Boolean,
        val currentWeather: CurrentWeather?,
        val forecast: List<ForeCast>?,
        val error: String?
    )
    //dateTimeı alıp saat.dakika formatına çeviriyoruz
    private fun getForecastTime(dateTime: String) : String{
        val pattern = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = pattern.parse(dateTime) ?: return dateTime
        return SimpleDateFormat("HH:mm",Locale.getDefault()).format(date)
    }

}