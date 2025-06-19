package com.example.havadurumu.dependency_injection

import com.example.havadurumu.storage.SharedPreferencesManager
import org.koin.dsl.module

val storageModule = module {
    single { SharedPreferencesManager(context = get(), gson = get()) }
}