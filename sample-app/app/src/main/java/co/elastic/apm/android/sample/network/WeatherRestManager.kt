package co.elastic.apm.android.sample.network

import co.elastic.apm.android.sample.network.data.ForecastResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRestManager {

    private val service: CityWeatherService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CityWeatherService::class.java)
    }

    suspend fun getCurrentCityWeather(city: String): ForecastResponse {
        return service.getCurrentWeather(city)
    }
}