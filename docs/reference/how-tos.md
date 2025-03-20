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
From a [Fragment](https://developer.android.com/reference/androidx/fragment/app/Fragment.html) instance, you can get the [Activity](https://developer.android.com/reference/android/app/Activity) that it is associated to by calling its [requireActivity()](https://developer.android.com/reference/androidx/fragment/app/Fragment.html#requireActivity()) method. Once you get the Activity object, you can get your application from it as [explained above](#from-an-activity).

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

## How to configure SSL/TLS? [faq-ssl]

Please note that the Elastic Agent does not handle SSL/TLS configs internally, therefore, the recommended way to manage these types of configurations is by doing so as part of your app’s network security configurations, as explained in Android’s official [security guidelines](https://developer.android.com/privacy-and-security/security-ssl). Below we show a set of common use-cases and quick tips on what could be done on each one, however, each case might be different, so please refer to Android’s [official docs](https://developer.android.com/privacy-and-security/security-config) on this topic in case you need more details.

### Connecting to Elastic Cloud [faq-ssl-elastic-cloud]

If your {{stack}} is hosted in {{ecloud}}, you shouldn’t need to add any SSL/TLS config changes in your app, it should work out of the box.

### Connecting to an on-prem server [faq-ssl-on-prem]

If your {{stack}} is hosted on-prem, then it depends on the type of CA your host uses to sign its certificates, if it’s a commonly trusted CA, then you shouldn’t have to worry about changing your app’s SSL/TLS configuration as it all should work well out of the box, however, if your CAs are unknown/private or your server uses a self-signed certificate, then you would need to configure your app to trust custom CAs by following [Android’s guide on it](https://developer.android.com/privacy-and-security/security-config).

### Debugging purposes [faq-ssl-debug]

If you’re running a local server and need to connect to it without using https in order to run a quick test, then you could temporarily [enable cleartext traffic](https://developer.android.com/guide/topics/manifest/application-element#usesCleartextTraffic) within your `AndroidManifest.xml` file, inside the `<application>` tag. As shown below:

```xml
<application
    ...
    android:usesCleartextTraffic="true">
    ...
</application>
```

::::{note}
You should only enable cleartext traffic for debugging purposes and not for production code.
::::

If enabling cleartext traffic isn’t a valid option for your debugging use-case, you should refer to Android’s guide on [configuring CAs for debugging](https://developer.android.com/privacy-and-security/security-config#TrustingDebugCa).

For more information on how Android handles network security, please refer to the official [Android docs on it](https://developer.android.com/privacy-and-security/security-ssl).
