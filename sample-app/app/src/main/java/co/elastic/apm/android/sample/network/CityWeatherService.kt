package co.elastic.apm.android.sample.network

import co.elastic.apm.android.sample.network.data.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityWeatherService {
    @GET("forecast")
    suspend fun getCurrentWeather(
        @Query("city") city: String
    ): ForecastResponse
}