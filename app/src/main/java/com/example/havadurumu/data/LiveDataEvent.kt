package com.example.havadurumu.data
//Live data üzerinden tek seferlik toast navigation vb için yadımcı veri sınfı
data class LiveDataEvent<out T >(private val content : T){
    //Daha wönce içeriğin işlenip işlenmediğini kontrol ediypruz
    private var hasBeenHandled = false

    fun getContentIfNotHandled() : T?{
        return if (hasBeenHandled){
            null
        }else{
            hasBeenHandled = true
            content
        }
    }
}
