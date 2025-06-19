package com.example.havadurumu.fragments.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.clearFragmentResultListener
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.example.havadurumu.R
import com.example.havadurumu.data.WeatherData
import com.example.havadurumu.databinding.FragmentHomeBinding
import com.example.havadurumu.storage.SharedPreferencesManager
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {
    //Location fragmentdan dönen verileri almak için anahtarlar tanımlıyoruz
    companion object{
        const val REQUEST_KEY_MANUAL_LOCATION_SEARCH = "manualLocationSearh"
        const val KEY_LOCATION_TEXT = "locationText"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
    }

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
    //uygulama başlarken bir kez kontrol eden bayrak değişkeni
    private var isInitialLocationSet: Boolean = false

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
        setCurrentLocation(currentLocation = sharedPreferencesManager.getCurrentLocation())
        setObservers()
        setListeners()
        if (!isInitialLocationSet){
            setCurrentLocation(currentLocation = sharedPreferencesManager.getCurrentLocation())
            isInitialLocationSet = true // konum rastgele bir yere tıklandıpında ayarlanmamısı için bayrak true yapılıyor
        }
    }

    private fun setListeners(){
        binding.swipeRefleshLayout.setOnRefreshListener {
            setCurrentLocation(sharedPreferencesManager.getCurrentLocation() )
        }
    }
    //ViewModelden gelen verileri gözlemlemek için observe kullanılır
    private fun setObservers(){
        with(homeViewModel){
            //CurrentLocation liveDatası gözlemleniyor
            currentLocation.observe(viewLifecycleOwner) {
                //live data null gelirse işlem yapılmaz
                val currentLocationDataState = it.getContentIfNotHandled() ?: return@observe
                if (currentLocationDataState.isLoading){
                    showLoading()
                }
                currentLocationDataState.currentLocation?.let { currentLocation ->
                    hideLoading()
                    sharedPreferencesManager.saveCurrentLocation(currentLocation)
                    setCurrentLocation(currentLocation)//gelen veriler adaptera eklenir
                }
                //eğer error null değilse hata olmuştur ve kullanıcıya mesaj gösterilir
                currentLocationDataState.error?.let { error ->
                    hideLoading()
                    Toast.makeText(requireContext(),error,Toast.LENGTH_SHORT).show()
                }
            }
            //live data dözlemlenir
            weatherData.observe(viewLifecycleOwner){
                //veri varsa tekrar işlememk için dikkatli oluyoruz
                Log.d("WeatherDataTest", "LiveData observer tetiklendi")
                val weatherDataState = it.getContentIfNotHandled() ?: return@observe
                Log.d("WeatherDataTest", "peekContent ile veri: $weatherDataState")
                binding.swipeRefleshLayout.isRefreshing = weatherDataState.isLoading
                weatherDataState.currentWeather?.let { currentWeather ->
                    Log.d("WeatherDataTest", "Fragment aldı: $currentWeather")
                  weatherDataAdapter.setCurrentWeather(currentWeather)
                }
                weatherDataState.forecast?.let { foreCasts ->
                    weatherDataAdapter.setForecastData(foreCasts)
                }
                weatherDataState.error?.let { error ->
                    Toast.makeText(requireContext(),error,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Adaptera hava durumu verisi atanıyor
    private fun setWeatherDataAdapter(){
        binding.rvWeatherData.itemAnimator = null
        binding.rvWeatherData.adapter = weatherDataAdapter
    }
    //geçerli konumu adaptera alıyoruz
    private fun setCurrentLocation(currentLocation : WeatherData.CurrentLocation? = null){
        weatherDataAdapter.setCurrentLocation(currentLocation ?: WeatherData.CurrentLocation())
        currentLocation?.let { getWeatherData(currentLocation = it) }
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
                    1-> startManualLocationSearch()
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
            swipeRefleshLayout.isEnabled = false
            swipeRefleshLayout.isRefreshing = true
        }
    }
    //Yükleniyor animasyonu başlatılır ve rv görünür yapar
    private fun hideLoading(){
        with(binding){
            //veri göründüğü için rv görünür
            rvWeatherData.visibility = View.VISIBLE
            //Yükleme animasyonu kapatılır
            swipeRefleshLayout.isEnabled = true
            swipeRefleshLayout.isRefreshing = false
        }
    }
    //manual arama başlar ve sonuçlar dinlenir
    private fun startManualLocationSearch(){
        startListeningManualLocationSelection()
        findNavController().navigate(R.id.action_home_fragment_to_location_fragment)
    }
    //locationfragmentdan dönen sonuçlar dinlenir
    private fun startListeningManualLocationSelection(){
        setFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH) { _, bundle ->
            stopListeningManualLocationSelection()
            //Bundledan konum bilgilerini alıyoruz
            val currentLocation = WeatherData.CurrentLocation(
                location = bundle.getString(KEY_LOCATION_TEXT) ?: "Bilinmiyor",
                latitude = bundle.getDouble(KEY_LATITUDE),
                longitude = bundle.getDouble(KEY_LONGITUDE)
            )
            sharedPreferencesManager.saveCurrentLocation(currentLocation)
            setCurrentLocation(currentLocation)
        }
    }
    //gereksiz tetiklenmelere karşı  dinleyiciyi kapatıyoruz
    private fun stopListeningManualLocationSelection(){
        clearFragmentResultListener(REQUEST_KEY_MANUAL_LOCATION_SEARCH)
    }
    //View model aracılıgı ile APIden verileri alıyoruz
    private fun getWeatherData(currentLocation: WeatherData.CurrentLocation){
        if (currentLocation.latitude != null && currentLocation.longitude != null){
            homeViewModel.getWeatherData(
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude
            )
        }
    }
}