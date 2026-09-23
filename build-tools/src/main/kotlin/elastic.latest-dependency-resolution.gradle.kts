val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val latestOkHttpVersion =
    libs.findLibrary("okhttp").get().get().versionConstraint.requiredVersion
val latestKotlinVersion = libs.findVersion("kotlin").get().requiredVersion

configurations.matching {
    it.name.endsWith("CompileClasspath") ||
        it.name.endsWith("RuntimeClasspath") ||
        it.name.endsWith("ByteBuddy")
}.configureEach {
    resolutionStrategy.eachDependency {
        when {
            requested.group == "com.squareup.okhttp3" && requested.name == "okhttp" -> {
                useVersion(latestOkHttpVersion)
                because("development builds test the latest catalog version")
            }

            requested.group == "org.jetbrains.kotlin" &&
                requested.name == "kotlin-stdlib" -> {
                useVersion(latestKotlinVersion)
                because("development builds test the latest catalog version")
            }
        }
    }
}
