# Sample Android application for the Elastic APM Agent

> This is part of
> our [blog post](https://www.elastic.co/blog/monitoring-android-applications-elastic-apm) on
> Monitoring Android applications with Elastic APM. If you need more detailed information on the
> overall usage of the Elastic APM Agent, you should take a look at it.

To showcase an end-to-end scenario including distributed tracing we'll instrument this sample
weather application that comprises two Android UI fragments and a simple local backend
service based on Spring Boot.

The first Fragment will have a dropdown list with some city names and also a button that takes you
to the second one, where you’ll see the selected city’s current temperature. If you pick a
non-European city on the first screen, you’ll get an error from the (local) backend when you head to
the second screen. This is to demonstrate how network and backend errors are captured and correlated
in Elastic APM.

## How to run

### Launching the local backend service

As part of our sample app, we’re going to launch a simple local backend service that will handle our
app’s HTTP requests. The backend service is instrumented with
the [Elastic APM Java agent](https://www.elastic.co/guide/en/apm/agent/java/current/index.html) to
collect
and send its own APM data over to Elastic APM, allowing it to correlate the mobile interactions with
the processing of the backend requests.

In order to configure the local server, we need to set our Elastic APM endpoint and secret token (
the same used for our Android app in the previous step) into the
backend/src/main/resources/elasticapm.properties file:

```properties
service_name=weather-backend
application_packages=co.elastic.apm.android.sample
server_url=YOUR_ELASTIC_APM_URL
secret_token=YOUR_ELASTIC_APM_SECRET_TOKEN
```

After the backend configuration is done, we can proceed to start the server by running the following
command in a terminal located in the root directory of our sample project: `./gradlew bootRun` (or
`gradlew.bat bootRun` if you’re on Windows). Alternatively, you can start the backend service from
Android Studio.

### Using the app

Launch the sample app in an Android emulator (from Android Studio). Once everything is running, we
need to navigate around in the app to generate some load that we would like to observe in Elastic
APM. So, select a city, click Next and repeat it multiple times. Please, also make sure to select
New York at least once. You will see that the weather forecast won’t work for New York as the city.
Below, we will use Elastic APM to find out what’s going wrong when selecting New York.

### Analyzing the data

After launching the app and navigating through it, you should be able to start seeing telemetry data
coming into your configured Kibana instance. For a more detailed overview of what to see there, you
should take a look
at [this blog post](https://www.elastic.co/blog/monitoring-android-applications-elastic-apm) on
Monitoring Android applications with Elastic APM.