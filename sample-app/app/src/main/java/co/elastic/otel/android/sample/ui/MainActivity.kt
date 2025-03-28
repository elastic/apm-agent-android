package co.elastic.otel.android.sample.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import co.elastic.otel.android.extensions.log
import co.elastic.otel.android.extensions.span
import co.elastic.otel.android.sample.MyApp.Companion.agent
import co.elastic.otel.android.sample.R
import co.elastic.otel.android.sample.databinding.ActivityMainBinding
import io.opentelemetry.api.common.Attributes

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        agent.span("Main Activity creation") {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)

            val navController = findNavController(R.id.nav_host_fragment_content_main)
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            val counter = agent.getOpenTelemetry().getMeter("metricscope").counterBuilder("button click count").build()
            binding.fab.setOnClickListener { view ->
                counter.add(1)
                agent.log(
                    "Button click",
                    attributes = Attributes.builder().put("activity.name", "MainActivity").build()
                )
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}