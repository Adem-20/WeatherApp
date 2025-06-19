package com.example.havadurumu.utils

import android.app.Application
import com.example.havadurumu.dependency_injection.networkModule
import com.example.havadurumu.dependency_injection.repositoryModule
import com.example.havadurumu.dependency_injection.serializerModule
import com.example.havadurumu.dependency_injection.storageModule
import com.example.havadurumu.dependency_injection.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
//Uygulama çalıştığında ilk çalışan sınıf
class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        //Koin bağımlılık ınjection kütüphanesini başlatıyoruz
        startKoin {
            androidContext(this@AppConfig)
            //kullanacağımız modüller burada ve bu sayede viewmodel ve repositry sınıfları koin tarafından yönetilir
            modules(
                listOf(
                    repositoryModule,
                    viewModelModule,
                    serializerModule,
                    storageModule,
                    networkModule
                )
            )
        }
    }
}