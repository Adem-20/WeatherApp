package com.example.havadurumu.dependency_injection

import com.google.gson.Gson
import org.koin.dsl.module
//koin modülü : json ayrıştırma için gson nesnesini sağlar
val serializerModule = module {
    single{ Gson() }
}