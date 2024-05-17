# Keep everything from open telemetry
-keep class io.opentelemetry.** { *; }

# Keep generated okhttp eventlistener
-keep class co.elastic.apm.android.common.okhttp.eventlistener.Generated_CompositeEventListener {
    public <methods>;
}

-dontwarn com.fasterxml.jackson.**
-dontwarn com.google.auto.value.**
-dontwarn com.google.common.io.ByteStreams
-dontwarn com.google.errorprone.annotations.**
-dontwarn io.grpc.**
-dontwarn java.awt.**
-dontwarn javax.json.bind.spi.JsonbProvider
-dontwarn org.osgi.annotation.bundle.Export