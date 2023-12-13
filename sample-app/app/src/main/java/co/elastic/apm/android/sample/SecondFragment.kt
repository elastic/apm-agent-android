package co.elastic.apm.android.sample

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.elastic.apm.android.sample.databinding.FragmentSecondBinding
import co.elastic.apm.android.sample.network.WeatherRestManager
import co.elastic.apm.android.sample.network.data.ForecastResponse
import kotlinx.coroutines.launch

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            try {
                val city = arguments?.getString("city") ?: "Berlin"
                binding.temperatureTitle.text = getString(
                    R.string.temperature_title,
                    city
                )
                updateTemperature(WeatherRestManager.getCurrentCityWeather(city))
                showApiNotice()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), R.string.unknown_error_message, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.buttonSecond.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    private fun showApiNotice() {
        binding.txtApiNotice.movementMethod = LinkMovementMethod.getInstance()
        binding.txtApiNotice.text = Html.fromHtml(getString(R.string.weather_api_notice_message))
    }

    private fun updateTemperature(response: ForecastResponse) {
        binding.txtDegreesCelsius.text = getString(
            R.string.temperature_in_celsius,
            response.currentWeather.temperature
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}