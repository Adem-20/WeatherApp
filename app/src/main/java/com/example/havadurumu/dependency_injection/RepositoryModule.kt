package com.example.havadurumu.dependency_injection

import com.example.havadurumu.network.repository.WeatherDataRepository
import org.koin.dsl.module
//WeatherDataRepositorynin uygulama genelinde kullanılmasını sağlar
val repositoryModule = module {
    //singleton ile uygulama boyunca tek bir instance oluşturur
    single { WeatherDataRepository(weatherApi = get()) }
}