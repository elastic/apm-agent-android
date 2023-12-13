package co.elastic.apm.android.sample.network.data

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("current_weather")
    val currentWeather: CurrentWeatherResponse
)