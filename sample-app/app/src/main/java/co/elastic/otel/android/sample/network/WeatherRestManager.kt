package co.elastic.otel.android.sample.network

import co.elastic.otel.android.sample.network.data.ForecastResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRestManager {

    private val service: CityWeatherService by lazy {
        val retrofit = Retrofit.Builder()
            // For Android Emulators, the "10.0.2.2" address is the one of its host machine.
            // Using it here allows accessing services that are running on the host machine from an
            // Android application that runs in the emulator.
            .baseUrl("http://10.0.2.2:8080/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CityWeatherService::class.java)
    }

    suspend fun getCurrentCityWeather(city: String): ForecastResponse {
        return service.getCurrentWeather(city)
    }
}