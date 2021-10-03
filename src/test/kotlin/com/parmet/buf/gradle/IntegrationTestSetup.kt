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
          id 'com.google.protobuf' version '0.8.17'
        }
        
        repositories { mavenCentral() }
        
        protobuf {
          protoc {
            artifact = 'com.google.protobuf:protoc:3.17.3'
          }
        }
        
        compileJava.enabled = false
        
        $additionalConfig
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

val publishSchema =
    """
        apply plugin: 'maven-publish'

        buf {
          publishSchema = true
        }
    """.trimIndent()
