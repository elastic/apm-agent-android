-keepclassmembers enum io.opentelemetry.** {
    public static **[] values();
}
-keepclassmembers enum co.elastic.otel.android.** {
    public static **[] values();
}
-keep class io.opentelemetry.api.incubator.** { *; }
-dontwarn com.fasterxml.jackson.**
-dontwarn com.google.auto.service.AutoService
-dontwarn com.google.auto.value.**
-dontwarn com.google.common.io.ByteStreams
-dontwarn com.google.errorprone.annotations.**
-dontwarn io.grpc.**
-dontwarn java.awt.**
-dontwarn javax.json.bind.spi.JsonbProvider
