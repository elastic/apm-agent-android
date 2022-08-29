# apm-agent-android

Elastic APM Android Agent

## Set up

```groovy
// Android app's build.gradle file
plugins {
    id "com.android.application"
    id "co.elastic.apm.android" // Apply the Elastic android Gradle plugin to your App.
}
```

```java
// Application class

class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Connectivity connectivity = Connectivity.create("http[s]://your.endpoint"); // .withAuthToken("your-auth-token");
        ElasticApmAgent.initialize(this, connectivity); // Initialize the Elastic APM agent once.
        //ElasticApmAgent.initialize(this, connectivity, configuration); optional with custom config.
    }
}
```

## Configuration

You can customize Elastic's APM agent by providing your own configuration when initializing it as
shown below:

```java

class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Connectivity connectivity = Connectivity.create("http[s]://your.endpoint");
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setServiceName("my-custom-name") // Defaults to your app's package name.
                .setServiceVersion("1.0.0") // Defaults to the version set in `android.defaultConfig.versionName` in the build.gradle file.
                .build();
        ElasticApmAgent.initialize(this, connectivity, configuration);
    }
}
```