package com.example.havadurumu.fragments.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.databinding.FragmentHomeBinding
import com.example.havadurumu.storage.SharedPreferencesManager
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {
    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel : HomeViewModel by viewModel()
    //konum servisleribe erişim(tembel)
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }
    //Koordinatların verisini almak için geocoder nesnesi (tembel)
    private val geocoder by lazy {Geocoder(requireContext())}

    //Konuma tıklandıgında çalışacak lambda
    private val weatherDataAdapter = WeatherDataAdapter(
        onLocationClicked ={showLocationOptions()}
    )

    private val sharedPreferencesManager : SharedPreferencesManager by inject()

    //Konum izni istiyoruz
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        if(isGranted){
            //İzin verildiyse konumu alıyoruz
            getCurrentLocation()
        }else{
            //Konum izni reddedildiyse uyarı gösteriyoruz
            Toast.makeText(requireContext(),"Konum izni reddedildi",Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //rvye adapter bağlanıypr
        setWeatherDataAdapter()
        //Adaptera veri gönderiliyor
        setWeatherData(currentLocation = sharedPreferencesManager.getCurrentLocation())
        setObservers()
    }
    //ViewModelden gelen verileri gözlemlemek için observe kullanılır
    private fun setObservers(){
        with(homeViewModel){
            //CurrentLocation liveDatası gözlemleniyor
            currentLocation.observe(viewLifecycleOwner) {
                //live data null gelirse işlem yapılmaz
                val currentLocationDataState = it ?: return@observe
                if (currentLocationDataState.isLoading){
                    showLoading()
                }
                currentLocationDataState.currentLocation?.let { currentLocation ->
                    hideLoading()
                    sharedPreferencesManager.saveCurrentLocation(currentLocation)
                    setWeatherData(currentLocation)//gelen veriler adaptera eklenir
                }
                //eğer error null değilse hata olmuştur ve kullanıcıya mesaj gösterilir
                currentLocationDataState.error?.let { error ->
                    hideLoading()
                    Toast.makeText(requireContext(),error,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Adaptera hava durumu verisi atanıyor
    private fun setWeatherDataAdapter(){
        binding.rvWeatherData.adapter = weatherDataAdapter
    }

    private fun setWeatherData(currentLocation : WeatherData.CurrentLocation? = null){
        weatherDataAdapter.setData(data = listOf(currentLocation ?: WeatherData.CurrentLocation()))
    }

    //Mevcut konumu alıyoruz(view model aracılıgı ile)
    private fun getCurrentLocation(){
        homeViewModel.getCurrentLocation(fusedLocationProviderClient,geocoder)
    }

    //Kullanıcıdan konum izni alıp almadığımızı kontrol ediyoruz
    private fun isLocationPermissionGranted() : Boolean{
        return ContextCompat.checkSelfPermission(
            requireContext(),Manifest.permission.ACCESS_FINE_LOCATION
        )== PackageManager.PERMISSION_GRANTED
    }

    //Tekrar izin istiyoruz
    private fun requestLocationPermission(){
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    //İzin verildiyse konumu alıyoruz verilmediyse tekrar izin istiyoruz
    private fun proceedWithCurrentLocation(){
        if (isLocationPermissionGranted()){
            getCurrentLocation()
        }else{
            requestLocationPermission()
        }
    }

    //Kullanıcıya konumu nasıl seçeceğine dair seçenek sunuyoruz
    private fun showLocationOptions(){
        val options = arrayOf("Mevcut Konum","Manuel olarak ara")
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Konum yönetimini seç")
            setItems(options){ _, which->
                when(which){
                    0-> proceedWithCurrentLocation()
                }
            }
            show()
        }
    }
    //Yükleniyor animasyonu başlatılır ve rv gizler
    private fun showLoading(){
        with(binding){
            //veri görğnmediği için rv görünmez
            rvWeatherData.visibility = View.GONE
            //yükleme animasyonu görünür
            swipeRefleshLayout.isRefreshing = true
        }
    }
    //Yükleniyor animasyonu başlatılır ve rv görünür yapar
    private fun hideLoading(){
        with(binding){
            //veri göründüğü için rv görünür
            rvWeatherData.visibility = View.VISIBLE
            //Yükleme animasyonu kapatılır
            swipeRefleshLayout.isRefreshing = false
        }
    }
}