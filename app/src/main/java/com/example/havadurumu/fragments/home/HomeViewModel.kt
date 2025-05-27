package com.example.havadurumu.fragments.home

import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.network.repository.WeatherDataRepository
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.launch
import java.lang.Error

class HomeViewModel (private val weatherDataRepository: WeatherDataRepository) : ViewModel() {
    //Konumun UI da görüntülenebilmesi için LiveData
    private val _currentLocation = MutableLiveData<CurrentLocationDataState>()
    val currentLocation:LiveData<CurrentLocationDataState> get() = _currentLocation
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
            val location = weatherDataRepository.uptadeAddressText(currentLocation,geocoder)
            //UI a başarılı sonucu gönderiyoruz
            emitCurrentLocationUiState(currentLocation = location)
        }
    }
    //Uı a gönderilecek durum LiveData üzerinden(Otomatik)
    private fun emitCurrentLocationUiState(
        isLoading : Boolean = false,
        currentLocation: WeatherData.CurrentLocation? = null,
        error: String? = null
    ){
        val currentLocationDataState = CurrentLocationDataState(isLoading,currentLocation,error)
        _currentLocation.value = currentLocationDataState
    }
    //UI katmanı için durum sınıfı (yükleniyor,başarlı,hata)
    data class CurrentLocationDataState(
        val isLoading: Boolean,
        val currentLocation: WeatherData.CurrentLocation?,
        val error: String?
    )
}