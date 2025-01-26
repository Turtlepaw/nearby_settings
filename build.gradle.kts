import org.gradle.api.JavaVersion
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

allprojects {
    version = "1.4"
    group = "io.jitpack.gradle-modular"
}

subprojects {
    apply(plugin = "maven-publish")
    extensions.configure<org.gradle.api.publish.PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()
                //from(components["java"])
            }
        }
    }
}
