package cl.duoc.fotomotion.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface HuertoApiService {
    @GET("api/health")
    suspend fun verificarConexion(): Map<String, Any>

    companion object {
        fun crear(): HuertoApiService {
            return Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // Cambiar por la URL/puerto de tu backend Spring Boot
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(HuertoApiService::class.java)
        }
    }
}
