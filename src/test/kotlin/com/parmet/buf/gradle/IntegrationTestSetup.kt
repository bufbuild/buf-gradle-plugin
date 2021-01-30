package com.parmet.buf.gradle

import java.io.File
import org.gradle.testkit.runner.GradleRunner

fun checkRunner(projectDir: File) =
    GradleRunner.create()
        .withProjectDir(projectDir)
        .withArguments("check")
        .withPluginClasspath()

fun buildGradle() =
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
