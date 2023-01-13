# Keep everything from open telemetry
-keep class io.opentelemetry.** { *; }

# Keep generated okhttp eventlistener
-keep class co.elastic.apm.android.common.okhttp.eventlistener.Generated_CompositeEventListener {
    public <methods>;
}
