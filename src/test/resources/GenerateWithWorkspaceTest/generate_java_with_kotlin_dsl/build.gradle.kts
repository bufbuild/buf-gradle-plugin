import com.parmet.buf.gradle.BUF_BUILD_DIR
import com.parmet.buf.gradle.GENERATED_DIR

plugins {
    `java`
    id("com.parmet.buf")
}

repositories {
    mavenCentral()
}

tasks.named("compileJava").configure { dependsOn("bufGenerate") }

sourceSets["main"].java {
    srcDir("$buildDir/$BUF_BUILD_DIR/$GENERATED_DIR/java")
}

val protobufVersion: String by project

dependencies {
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
}
