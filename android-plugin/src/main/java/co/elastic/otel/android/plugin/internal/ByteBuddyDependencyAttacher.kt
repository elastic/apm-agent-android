package co.elastic.otel.android.plugin.internal

import org.gradle.api.Project

class ByteBuddyDependencyAttacher(
    private val project: Project,
    private val dependencyUri: String
) : BuildVariantListener {

    override fun onBuildVariant(name: String) {
        project.configurations.maybeCreate("${name}ByteBuddy").dependencies.add(
            project.dependencies.create(dependencyUri)
        )
    }
}