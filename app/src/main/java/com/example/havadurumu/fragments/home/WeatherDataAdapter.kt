package com.example.havadurumu.fragments.home

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import coil.load
import com.example.havadurumu.data.CurrentWeather
import com.example.havadurumu.data.ForeCast
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.databinding.ItemContainerCurrentWeatherBinding
import com.example.havadurumu.databinding.ItemContainerForecastBinding
import com.example.havadurumu.databinding.ItemCurrentLocationBinding
import okhttp3.internal.notify
import java.text.Bidi

class WeatherDataAdapter (
    //Konuma tıklandıgında callback çalışacak
    private val onLocationClicked : () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    //view type indexlerini sabit olarak tnaımlıyoruz
    private companion object{
        const val INDEX_CURRENT_LOCATION = 0
        const val INDEX_CURRENT_WEATHER = 1
        const val INDEX_FORECAST = 2
    }

    private val weatherData = mutableListOf<WeatherData>()

    //anlık konum verisini adaptera ekliyor ve güncelliyoruz
    fun setCurrentLocation(currentLocation: WeatherData.CurrentLocation){
        if(weatherData.isEmpty()){
            weatherData.add(INDEX_CURRENT_LOCATION,currentLocation)
            notifyItemInserted(INDEX_CURRENT_LOCATION)
        }else{
            weatherData[INDEX_CURRENT_LOCATION] = currentLocation
            notifyItemChanged(INDEX_CURRENT_LOCATION)
        }
    }
    //Guncel hava durumu verisini adaptera ekliyoruz ve var oolanı güncelliyoruz
    fun setCurrentWeather(currentWeather: CurrentWeather){
        if (weatherData.getOrNull(INDEX_CURRENT_WEATHER) != null){
            weatherData[INDEX_CURRENT_WEATHER] = currentWeather
            notifyItemChanged(INDEX_CURRENT_WEATHER)
        }else{
            weatherData.add(INDEX_CURRENT_WEATHER, currentWeather)
            notifyItemInserted(INDEX_CURRENT_WEATHER)
        }
    }
    //Mevcut verileri yenisiyle günceller ve adaptera aralık bildirimleri yapar
    fun setForecastData(forecast: List<ForeCast>) {
     weatherData.removeAll{it is ForeCast }
        notifyItemRangeRemoved(INDEX_FORECAST,weatherData.size)
        weatherData.addAll(INDEX_FORECAST,forecast)
        notifyItemRangeChanged(INDEX_FORECAST,weatherData.size)
    }
    //Wiew holderları viewtype değerine göre oluştururuz farklı layoutları inflate ederiz duruma göre
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("WeatherDataTest", "onCreateViewHolder: viewType $viewType")
        return when(viewType){
            INDEX_CURRENT_LOCATION -> CurrentLocationViewHolder(
                ItemCurrentLocationBinding.inflate(
                    LayoutInflater.from(parent.context),parent,false
                )
            )

            INDEX_FORECAST -> ForecastViewHolder(
                ItemContainerForecastBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> CurrentWeatherViewHolder(
                ItemContainerCurrentWeatherBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        Log.d("Adapter","VeriAdedi: ${weatherData.size}")
        return weatherData.size
    }
    //verinin türüne göre görünüm türü bilgisini döndürüyoruz
    override fun getItemViewType(position: Int): Int {
        val type =  when (weatherData[position]) {
            is WeatherData.CurrentLocation -> INDEX_CURRENT_LOCATION
            is CurrentWeather -> INDEX_CURRENT_WEATHER
            is ForeCast -> INDEX_FORECAST
        }
        Log.d("WeatherDataTest", "getItemViewType: pozisyon $position -> tip $type")
        return type
    }
    //Her satırı viewHolder a bağlar
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.d("WeatherDataTest", "onBindViewHolder: pozisyon $position")
        when(holder){
            //view holder currentlocaiton tipindeyse konum verisini
            is CurrentLocationViewHolder -> holder.bind(weatherData[position] as WeatherData.CurrentLocation)
            //view holder currentweather tipindeyse hava durumu verisini bağla
            is CurrentWeatherViewHolder -> holder.bind(weatherData[position] as CurrentWeather)
            is ForecastViewHolder -> holder.bind(weatherData[position] as ForeCast)
        }
    }
    //Kartlarda gösterilecek öğeleri bağlıyoruz
    inner class CurrentLocationViewHolder (
        private val binding: ItemCurrentLocationBinding
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(currentLocation : WeatherData.CurrentLocation){
            with(binding){
                textCurrentDate.text = currentLocation.date
                textCurrentLocation.text = currentLocation.location
                imageCurrentLocation.setOnClickListener { onLocationClicked()}
                textCurrentLocation.setOnClickListener { onLocationClicked() }
            }
        }
    }

    inner class CurrentWeatherViewHolder(
        private val binding: ItemContainerCurrentWeatherBinding
    ): RecyclerView.ViewHolder(binding.root){
        //Current Weather ile UIları doldur
        fun bind(currentWeather: CurrentWeather){
            Log.d("Test", "bind() çağrıldı, sıcaklık: ${currentWeather.temperature}")
            Log.d("WeatherTest", "Gelen icon URL: https:${currentWeather.icon}")
            binding.root.setBackgroundColor(Color.RED)
            with(binding){
                imageIcon.load("https:${currentWeather.icon}") {crossfade(true)}
                textTemperature.text = String.format("%s\u00b0C",currentWeather.temperature)
                textWind.text = String.format("%s km/h",currentWeather.wind)
                textHumidity.text = String.format("%s%%", currentWeather.humidity)
                textChangeOfRain.text = String.format("%s%%", currentWeather.chanceOfRain)
            }
        }
    }

    inner class ForecastViewHolder(
        private val binding: ItemContainerForecastBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(forecast: ForeCast){
            with(binding){
                textTime.text = forecast.time
                textTemperatures.text = String.format("%s\u00B0C",forecast.temperatures)
                textFeelsLikeTemperature.text = String.format("%s\u00B0C",forecast.feelsLikeTemperature)
                imageIcon.load("https:${forecast.icon}"){ crossfade(true)}
            }
        }
    }
}