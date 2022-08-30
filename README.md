# apm-agent-android

Elastic APM Android Agent

## Local testing

In order to use a local version of this agent you'll need to publish it locally into your machine's
maven local repo. In order to do that, simply open up a terminal in this project's root dir and
run: `./gradlew publishToMavenLocal`. After that, you can apply this agent into an Android
application project by following the "Set up" process defined below.

## Set up

```groovy
// Android app's build.gradle file
plugins {
    id "com.android.application"
    // Apply the Elastic android Gradle plugin to your App:
    id "co.elastic.apm.android" version "[use latest version]"
    // The latest version is defined in the `gradle.properties` file on the root of this project.
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

### General configuration

```java

class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Connectivity connectivity = ...;
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                .setServiceName("my-custom-name") // Defaults to your app's package name.
                .setServiceVersion("1.0.0") // Defaults to the version set in `android.defaultConfig.versionName` in your app's build.gradle file.
                .build();
        ElasticApmAgent.initialize(this, connectivity, configuration);
    }
}
```

### Configuring HTTP tracing

```java

class MyApp extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Connectivity connectivity = ...;
        // Create your own HttpTranceConfiguration:
        HttpTraceConfiguration httpTraceConfiguration = HttpTraceConfiguration.builder()
                // Make your changes to it before calling `build()` (more details below).
                .build();
        ElasticApmConfiguration configuration = ElasticApmConfiguration.builder()
                // Pass it to the agent's config builder:
                .setHttpTraceConfiguration(httpTraceConfiguration)
                .build();
        ElasticApmAgent.initialize(this, connectivity, configuration);
    }
}
```

#### Filtering HTTP requests from getting traced

By default, all of your app's HTTP requests will get traced. You can avoid some requests from
getting traced by creating your own `HttpExclusionRule`. For example this is an exclusion rule that
prevents all requests with the host `127.0.0.1` from getting traced:

```java
class MyHttpExclusionRule extends HttpExclusionRule {

    @Override
    public boolean exclude(HttpRequest request) {
        return request.url.getHost().equals("127.0.0.1");
    }
}
```

Then you'd need to add it to Elastic's Agent config through its `HttpTraceConfiguration` like so:

```java
HttpTraceConfiguration.builder()
        .addExclusionRule(new MyHttpExclusionRule())
        .build();
```

#### Adding extra attributes to your HTTP requests' spans

If the HTTP span attributes provided by default aren't enough, you can attach your own
`HttpAttributesVisitor` to add extra params to each HTTP request being traced. For example:

```java
class MyHttpAttributesVisitor implements HttpAttributesVisitor {

    public void visit(AttributesBuilder attrsBuilder, HttpRequest request) {
        attrsBuilder.put("my_custom_attr_key", "my_custom_attr_value");
    }
}
```

Then you'd need to add it to Elastic's Agent config through its `HttpTraceConfiguration` like so:

```java
HttpTraceConfiguration.builder()
        .addHttpAttributesVisitor(new MyHttpAttributesVisitor())
        .build();
```

## Attributes

There are common attributes that the Elastic Agent gather for every Span, and those are
listed [here](https://github.com/elastic/apm/tree/main/specs/agents/mobile).

However, due to the nature of Android's OS when it comes to collect some device-related data, some
of the above mentioned resources require the Host app (your app) to have some runtime permissions
granted. If their corresponding permissions aren't granted, then they won't be collected and nothing
will be sent for those attributes. Those are the following:

|Attribute|Used in|Requires permission|
|---|---|---|
|`net.host.connection.subtype`|HTTP Spans|[READ_PHONE_STATE](https://developer.android.com/reference/android/Manifest.permission#READ_PHONE_STATE)|