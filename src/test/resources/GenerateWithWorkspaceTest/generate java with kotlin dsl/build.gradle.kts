/*
 * Copyright (c) 2022 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
