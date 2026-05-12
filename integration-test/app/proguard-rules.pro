# Maximize R8 optimizations to surface potential issues with the agent's
# consumer ProGuard rules. These settings mirror what a heavily optimized
# production app might use.
-repackageclasses ''
-overloadaggressively
-allowaccessmodification

# Keep the Crasher class so it appears in crash stacktraces with an obfuscated
# name.  Without this, R8 inlines everything into CrashActivity.onCreate.
-keep,allowobfuscation,allowoptimization class co.elastic.otel.android.integration.Crasher { *; }
