import com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY
import com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET

plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
    id("org.gradle.kotlin.kotlin-dsl.base") version "1.3.6"
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

allprojects {
    configureLinting()
}

group = "com.parmet"
configurePublishing()
configureStagingRepoTasks()
configureKotlin()

gradlePlugin {
    isAutomatedPublishing = true

    plugins {
        create("buf") {
            id = "com.parmet.buf"
            implementationClass = "com.parmet.buf.gradle.BufPlugin"
            displayName = ProjectInfo.name
            description = ProjectInfo.description
        }
    }
}

pluginBundle {
    mavenCoordinates {
        group = project.group.toString()
    }
    website = ProjectInfo.url
    vcsUrl = ProjectInfo.url
    description = ProjectInfo.description
    tags = listOf("protobuf", "kotlin", "buf")
}

ext[GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

tasks.named("publishPlugins") {
    enabled = isRelease()
}
