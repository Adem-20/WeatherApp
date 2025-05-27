package com.example.havadurumu.fragments.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.databinding.ItemCurrentLocationBinding

class WeatherDataAdapter (
    //Konuma tıklandıgında callback çalışacak
    private val onLocationClicked : () -> Unit
) : RecyclerView.Adapter<WeatherDataAdapter.CurrentLocationViewHolder>()  {
    private val weatherData = mutableListOf<WeatherData>()
    // Yeni veri gelidğinde listeyi yeniler
    @SuppressLint("NotifyDataSetChanged")
    fun setData(data : List<WeatherData>){
        weatherData.clear()
        weatherData.addAll(data)
        notifyDataSetChanged()
    }
    //layout dosyasını inflate
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentLocationViewHolder {
        return CurrentLocationViewHolder(
            ItemCurrentLocationBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }

    override fun getItemCount(): Int {
        return weatherData.size
    }
    //Her satırı viewHolder a bağlar
    override fun onBindViewHolder(holder: CurrentLocationViewHolder, position: Int) {
        holder.bind(weatherData[position] as WeatherData.CurrentLocation)
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

}