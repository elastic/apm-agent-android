# Keep everything from open telemetry testing
-keep class io.opentelemetry.sdk.testing.** { *; }

-dontwarn org.assertj.core.**.*
-dontwarn org.junit.jupiter.**.*
-dontwarn javax.annotation.*
-dontwarn javax.annotation.**.*
