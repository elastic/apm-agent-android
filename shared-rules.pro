# Keep everything from open telemetry
-keep class io.opentelemetry.** { *; }

-dontwarn com.fasterxml.jackson.**
-dontwarn com.google.auto.value.**
-dontwarn com.google.common.io.ByteStreams
-dontwarn com.google.errorprone.annotations.**
-dontwarn io.grpc.**
-dontwarn java.awt.**
-dontwarn javax.json.bind.spi.JsonbProvider
-dontwarn org.osgi.annotation.bundle.Export
-dontwarn com.google.auto.service.AutoService
