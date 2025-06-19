package com.example.havadurumu.network.api

import com.example.havadurumu.data.RemoteLocation
import com.example.havadurumu.data.RemoteWeatherData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    companion object {
        const val BASE_URL = "https://api.weatherapi.com/v1/"
        const val API_KEY = "22a4613d360446779e5203821252905"
    }
    //Get isteği ile "current.json endpointine istek atıyoruz"
    @GET("search.json")
    suspend fun searchLocation(
        //API anahtarı query parametresi olarak gönderilir
        @Query("key") key : String = API_KEY,
        //Kullanıcının aradığı location bilgisi
        @Query("q") query : String
    ) : Response<List<RemoteLocation>>//API dan dönen yanıt bir tane olabilir ve liste olabilir

    @GET("forecast.json") //Endpointine get isteği gönderiyoruz
    suspend fun getWeatherData(
        @Query("key") key: String = API_KEY,
        @Query("q") query: String
    ) : Response<RemoteWeatherData>//Doönen yanıt tipi

}