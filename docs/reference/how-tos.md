# How-tos

## How to get my Android application instance [get-application]

Your [Application](https://developer.android.com/reference/android/app/Application) instance is needed to initialize the agent. There are a couple of ways you can get yours:

### From within your custom Application implementation (recommended)

Ideally, the agent should get initialized as soon as your application is launched, to make sure that it can start collecting telemetry from the very beginning.

Based on the above, an ideal place to do so is from within your own, custom [Application.onCreate](https://developer.android.com/reference/android/app/Application#onCreate()) method implementation, as shown below:

```kotlin
package my.app

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val agent = ElasticApmAgent.builder(this) // <1>
            //...
            .build()
    }
}
```
1. `this` is your application.

:::{important}
For it to work, you **must** register your custom application in your `AndroidManifest.xml` file, like so:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name="my.app.MyApp"
        ...
    </application>
</manifest>
```
:::

### From an Activity

You can get your application from an [Activity](https://developer.android.com/reference/android/app/Activity) by calling its [getApplication()](https://developer.android.com/reference/android/app/Activity#getApplication()) method.

### From a Fragment
From a [Fragment](https://developer.android.com/reference/androidx/fragment/app/Fragment.html) instance, you can get the [Activity](https://developer.android.com/reference/android/app/Activity) that it is associated to by calling its [requireActivity()](https://developer.android.com/reference/androidx/fragment/app/Fragment.html#requireActivity()) method. Once you get the Activity object, you can get your application from it as explained above.

## How to get my {{stack}} export endpoint [get-export-endpoint]

The export endpoint is where your app's telemetry is sent, so it's a requirement to initialize the agent. To find it in your {{stack}}, you'll need to **open {{kib}} and find "Add data" in the main menu**, alternatively, you can use the [global search field](https://www.elastic.co/guide/en/kibana/current/introduction.html#kibana-navigation-search) and search for "Observability Onboarding".

Then **select "Application"**, as shown below:

:::{image} ../images/find-export-endpoint/1.png
:screenshot:
:::

Followed by **choosing "OpenTelemetry"**:

:::{image} ../images/find-export-endpoint/2.png
:screenshot:
:::

Lastly, on the next page, **scroll down to the "APM Agents" pane, and select the "OpenTelemetry" tab**:

:::{image} ../images/find-export-endpoint/3.png
:screenshot:
:::

You'll find your export endpoint URL as **the value for the `OTEL_EXPORTER_OTLP_ENDPOINT` configuration setting**:

:::{image} ../images/find-export-endpoint/4.png
:screenshot:
:::

## How to create an API Key [create-api-key]

API Keys are the recommended way of authenticating the agent with your {{stack}}. There's a couple of ways you can create one:

### Via {{kib}}'s Applications UI

This is the most straightforward approach, you'll need to follow [this quick guide](https://www.elastic.co/guide/en/observability/current/apm-api-key.html#apm-create-an-api-key) on it and leave all the settings with their default values.

### Via REST APIs

[This guide](https://www.elastic.co/guide/en/observability/current/apm-agent-key-api.html#apm-create-agent-key) will help you create an API Key with a set of privileges that are scoped for the APM Agent use case only.