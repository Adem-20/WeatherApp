package com.example.havadurumu.fragments.location

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.havadurumu.data.RemoteLocation
import com.example.havadurumu.databinding.ItemContainerLocationBinding

class LocationsAdapter(
    private val onLocationClicked:(RemoteLocation) -> Unit
) : RecyclerView.Adapter<LocationsAdapter.LocationViewHolder>(){

    private val locatios = mutableListOf<RemoteLocation>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData (data: List<RemoteLocation>) {
        locatios.clear()
        locatios.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        return LocationViewHolder(
            ItemContainerLocationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,false
            )
        )
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(remoteLocation = locatios[position])
    }

    override fun getItemCount(): Int {
        return locatios.size
    }

    inner class LocationViewHolder(
        private val binding : ItemContainerLocationBinding
    ) :RecyclerView.ViewHolder(binding.root){
        fun bind(remoteLocation: RemoteLocation){
            with(remoteLocation){
                val location = "$name , $region, $country"
                binding.textRemoteLocation.text = location
                binding.root.setOnClickListener { onLocationClicked (remoteLocation) }
            }
        }
    }
}