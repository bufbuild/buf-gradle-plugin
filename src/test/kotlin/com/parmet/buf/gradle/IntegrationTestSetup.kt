/*
 * Copyright (c) 2021 Andrew Parmet
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

package com.parmet.buf.gradle

fun buildGradle(additionalConfig: String = "") =
    """
        plugins { 
          id 'java'
          id 'com.parmet.buf'
          id 'com.google.protobuf' version '0.8.14'
        }
        
        repositories { mavenCentral() }
        
        protobuf {
          protoc {
            artifact = 'com.google.protobuf:protoc:3.14.0'
          }
        }
        
        compileJava.enabled = false
        
        $additionalConfig
    """.trimIndent()

fun bufYaml() =
    """
        version: v1beta1
        build:
          roots:
            - src/main/proto
            - build/extracted-include-protos/main
        lint:
          ignore:
            - google
          use:
            - DEFAULT
    """.trimIndent()

fun basicProtoFile(messageName: String = "BasicMessage") =
    """
        syntax = "proto3";

        package parmet.buf.test.v1;

        message $messageName {}
    """.trimIndent()

val localRepo =
    """
        repositories {
          maven {
            url 'build/repos/test'
            name = 'test'
          }
        }
    """.trimIndent()

val imageArtifact =
    """
        buf {
          imageArtifact {
            groupId = 'foo'
            artifactId = 'bar'
            version = '2319'
          }
        }
    """

val publication = publication("maven")
val publication2 = publication("maven2")

private fun publication(name: String) =
    """
        publications {
          $name(MavenPublication) {
            groupId = 'foo'
            artifactId = 'bar'
            version = '2319'
            from components.java
          }
        }
    """.trimIndent()

val publishSchema =
    """
        apply plugin: 'maven-publish'

        buf {
          publishSchema = true
        }
    """.trimIndent()
