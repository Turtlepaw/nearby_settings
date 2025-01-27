import org.gradle.api.JavaVersion
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("maven-publish")
}

allprojects {
    version = "1.0"
    group = "com.turtlepaw.nearby_settings"
}

subprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    groupId = project.group.toString()
                    artifactId = project.name
                    version = project.version.toString()

                    artifact("${projectDir}/tv_core/build/outputs/aar/tv_core-release.aar")

                    // Add this for Android libraries
                    from(components.findByName("release"))
                }
            }
        }
    }
}
