plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

repositories {
    gradlePluginPortal()
    jcenter()
}

dependencies {
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.1.2")
    implementation("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.22.0")
    implementation(kotlin("gradle-plugin"))
}
