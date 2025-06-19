package com.example.havadurumu.dependency_injection

import com.example.havadurumu.fragments.home.HomeViewModel
import com.example.havadurumu.fragments.location.LocationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
//viewModeller için koin modulü tanımlıyoruz
val viewModelModule = module {
    //HomeViewModelin nasıl oluşturulacağını koine söylüyoruz
    // HomeViewModel onstructorında WeatherDataRepository istiyor
    // get() fonksiyonu eatherDataRepository bağımlılığını otomatik enjekte ediyor
    viewModel { HomeViewModel(weatherDataRepository = get()) }
    viewModel { LocationViewModel(weatherDataRepository = get()) }
}