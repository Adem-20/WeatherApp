package com.example.havadurumu.network.repository

import android.annotation.SuppressLint
import android.location.Geocoder
import com.example.havadurumu.data.RemoteLocation
import com.example.havadurumu.data.RemoteWeatherData
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.network.api.WeatherApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class WeatherDataRepository(private val weatherApi: WeatherApi){
    //Konum izni kontrolü yapılmadığı için Android studio iznini bastırıyoeuz
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        fusedLocationProviderClient: FusedLocationProviderClient,//Google konum servisi
        onSuccess : (currentLocation: WeatherData.CurrentLocation)-> Unit,//Başarılı olursa çalışacak
        onFailure : () ->Unit//hata olursa çalışiacak(lambda)
    ){
        //Mevcut konumu al
        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,//KOnumu dosdoğru al
            CancellationTokenSource().token//işlem sırasında gerekirse iptal edilebilir
        ).addOnSuccessListener {location ->
            //konum null gelirse hata fonksiyonnunu çağırıyoruz
            location ?: onFailure()
            onSuccess(
                //konum bilgilerini veriye dönüştür ve sonucu gönder
                WeatherData.CurrentLocation(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            )
        }.addOnFailureListener{
            //konum alma işlemi başarısız olursa hata fonksiyonunu çağırıyoruz
        onFailure()}
    }
    //Geocoder kullandıgımız için depraction uyarısını bastırıyoruz
    @Suppress("DEPRACTİON")
    fun uptadeAddressText(currentLocation: WeatherData.CurrentLocation,
                          geocoder : Geocoder//Kordinatların adres metnini oluşturuypruz
    ):WeatherData.CurrentLocation {
        val latitude = currentLocation.latitude ?: return currentLocation//Enlem boylam boşssa geri dön
        val longitude = currentLocation.longitude ?: return currentLocation
        //Koordinatların adres bilgisini alıyoruz
        return geocoder.getFromLocation(latitude,longitude,1)?.let {addresses ->
            val address = addresses[0]//ilk sonuç
            //Adresi bölüm bölüm oluşturuyoruz
            val addressText = StringBuilder()
            addressText.append(address.locality).append(", ")
            addressText.append(address.adminArea).append(", ")
            addressText.append(address.countryName)
            //Mevcut konumun location alanını güncelleyip yenisi dönüyor
            currentLocation.copy(
                location = addressText.toString()
            )
            //adresi bulamazsa
        } ?: currentLocation
    }

    // Kullanıcı izmir girdi izmire query ile istek ataız

    suspend fun searchLocation(query:String) : List<RemoteLocation>? {
        //Retrofit ile api a "query" parametrei ile istek atar
        val response = weatherApi.searchLocation(query = query)
        //Yanıt doğru dönerse veri gösterilir yoksa null döner
        return if (response.isSuccessful) response.body() else null
    }
    //konuma göre veriler çekilir
    suspend fun getWeatherData(latitude: Double, longitude: Double): RemoteWeatherData?{
        val response = weatherApi.getWeatherData(query = "$latitude, $longitude")
        return if (response.isSuccessful) response.body() else null
    }
}