# How-to

## How to get my Android application instance

Your Android application is needed to initialize the agent. There are a couple of ways you can get yours:

### From within your custom Application implementation (recommended)
Ideally, the agent should get initialized as soon as your application is launched, to make sure that it can start collecting telemetry from the very beginning.

Because of the above, an ideal place to do so is from within your own, custom [Application#onCreate](https://developer.android.com/reference/android/app/Application#onCreate()) method implementation, as shown below:

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

### From within an Activity instance
You can get your application from within any of your activities by calling the [Activity#getApplication()](https://developer.android.com/reference/android/app/Activity#getApplication()) method from it.

### From within a Fragment instance
From within a [Fragment](https://developer.android.com/reference/androidx/fragment/app/Fragment.html) instance you can get the [Activity](https://developer.android.com/reference/android/app/Activity) that it is associated to, by calling the [Fragment#requireActivity()](https://developer.android.com/reference/androidx/fragment/app/Fragment.html#requireActivity()) method. Once you get the Activity object, you can get your application from it as explained above.