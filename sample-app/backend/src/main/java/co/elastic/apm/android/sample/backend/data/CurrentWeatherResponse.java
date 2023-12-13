package co.elastic.apm.android.sample.backend.data;

import java.util.Objects;

public class CurrentWeatherResponse {
    double temperature;

    public double getTemperature() {
        return temperature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrentWeatherResponse that = (CurrentWeatherResponse) o;
        return Double.compare(that.temperature, temperature) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature);
    }
}
