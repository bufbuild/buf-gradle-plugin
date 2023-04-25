# buf-gradle-plugin

[![Maven Central](https://img.shields.io/badge/dynamic/xml?color=orange&label=maven-central&prefix=v&query=%2F%2Fmetadata%2Fversioning%2Flatest&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fbuild%2Fbuf%2Fbuf-gradle-plugin%2Fmaven-metadata.xml)](https://search.maven.org/artifact/build.buf/buf-gradle-plugin)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/build/buf/buf-gradle-plugin/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/build.buf)

Integration for [Buf](https://github.com/bufbuild/buf) with Gradle. Supports integration purely between Buf and Gradle or additionally with the [protobuf-gradle-plugin](https://github.com/google/protobuf-gradle-plugin).

This plugin supports straightforward usage of `buf lint`, `buf format`, and `buf generate`, and a self-contained integration between `buf build` and `buf breaking`.

## Table of Contents

<!--ts-->
* [buf-gradle-plugin](#buf-gradle-plugin)
   * [Table of Contents](#table-of-contents)
   * [Usage](#usage)
      * [Examples](#examples)
   * [Configuration](#configuration)
      * [Dependencies](#dependencies)
      * [bufLint](#buflint)
      * [bufBreaking](#bufbreaking)
         * [Checking against the latest published version](#checking-against-the-latest-published-version)
         * [Checking against a static version](#checking-against-a-static-version)
         * [Artifact details](#artifact-details)
      * [bufGenerate](#bufgenerate)
         * [Generating Dependencies](#generating-dependencies)
         * [Further Configuration](#further-configuration)
   * [Additional Configuration](#additional-configuration)

<!-- Created by https://github.com/ekalinin/github-markdown-toc -->
<!-- Added by: andrewparmet, at: Sun Oct  2 15:32:56 EDT 2022 -->

<!--te-->
<!-- Regenerate with `./gh-md-toc --insert README.md` -->

## Usage

By default this plugin assumes that Buf is configured for the project root (with or without a workspace `buf.work.yaml`). It will scan all top-level directories for protobuf sources.

If the project includes the `protobuf-gradle-plugin`, then this plugin will use an implicit Buf workspace that includes all specified protobuf source set directories, the `include` dependencies that the protobuf-gradle-plugin extracts into `"$buildDir/extracted-include-protos"`, and the dependencies that the protobuf-gradle-plugin has been told to generate that are extracted into `"$buildDir/extracted-protos"`.

This plugin does not support usage of both a Buf workspace and the `protobuf-gradle-plugin`; determining ownership of dependency resolution in that case would be complicated and error-prone.

Apply the plugin:

``` kotlin
plugins {
    id("build.buf") version "<version>"
}
```

or

``` kotlin
buildscript {
    dependencies {
        classpath("build.buf:buf-gradle-plugin:<version>")
    }
}

apply(plugin = "build.buf")
```

When applied the plugin creates tasks:
- `bufFormatApply` applies Buf's formatter to protobuf code
- `bufFormatCheck` validates protobuf code using Buf's formatter
- `bufLint` lints protobuf code
- `bufBreaking` checks protobuf against a previous version for backwards-incompatible changes
- `bufGenerate` generates protobuf code

### Examples

Each [integration test](src/test/resources) in this project is an example usage.

## Configuration

For a basic Buf project or one that uses the `protobuf-gradle-plugin` you can create a Buf configuration file in the project directory:

``` yaml
# buf.yaml

version: v1
lint:
  ignore:
    - path/to/dir/to/ignore
  use:
    - DEFAULT
```

As an alternative to a `buf.yaml` file in the project directory you can specify the location of `buf.yaml` by configuring the extension: 

``` kotlin
buf {
    configFileLocation = rootProject.file("buf.yaml")
}
```

Or you can share a Buf configuration across projects and specify it via the dedicated `buf` configuration:

``` kotlin
dependencies {
    buf("build.buf:shared-buf-configuration:0.1.0")
}
```

As an example to create this artifact, set up a project `shared-buf-configuration`:

```
shared-buf-configuration % tree
.
├── build.gradle.kts
└── buf.yaml
``` 

``` kotlin
// build.gradle.kts

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("bufconfig") {
            groupId = "build.buf"
            artifactId = "shared-buf-configuration"
            version = "0.1.0"
            artifact(file("buf.yaml"))
        }
    }
}
```

Projects with Buf workspaces must configure their workspaces as described in the Buf documentation; no configuration for linting will be overrideable. A `buf.yaml` in the project root or specified in the extension will be used for breakage checks only.

### Dependencies

If your `buf.yaml` declares any dependencies using the `deps` key, you must run `buf mod update` to create a `buf.lock` file manually. The `buf-gradle-plugin` does not (yet) support creating the dependency lock file.

### `bufFormatApply` and `bufFormatCheck`

`bufFormatApply` is run manually and has no configuration.

`bufFormatCheck` is run automatically during the `check` task if `enforceFormat` is enabled. It has no other configuration.

```kotlin
buf {
    enforceFormat = true // true by default
}
```

### `bufLint`

`bufLint` is configured by creating `buf.yaml` in basic projects or projects using the `protobuf-gradle-plugin`. It is run automatically during the `check` task. Specification of `buf.yaml` is not supported for projects using a workspace.

### `bufBreaking`

`bufBreaking` is more complicated since it requires a previous version of the protobuf schema to validate the current version. Buf's built-in Git integration isn't quite enough since it requires a buildable protobuf source set and the `protobuf-gradle-plugin`'s extraction step typically targets the project build directory, which is ephemeral and not committed.

This plugin uses `buf build` to create an image from the current protobuf schema and publishes it as a Maven publication. In subsequent builds of the project the plugin will resolve the previously published schema image and run `buf breaking` against the current schema with the image as its reference.

#### Checking against the latest published version

Enable `checkSchemaAgainstLatestRelease` and the plugin will resolve the previously published Maven artifact as its input for validation.

For example, first publish the project with `publishSchema` enabled:

``` kotlin
buf {
    publishSchema = true
}
```

Then configure the plugin to check the schema:

``` kotlin
buf {
    // continue to publish schema
    publishSchema = true

    checkSchemaAgainstLatestRelease = true
}
```

The plugin will run Buf to check the project's current schema:

```
> Task :bufBreaking FAILED
src/main/proto/buf/service/test/test.proto:9:1:Previously present field "1" with name "test_content" on message "TestMessage" was deleted.
```

#### Checking against a static version

If for some reason you do not want to dynamically check against the latest published version of your schema, you can specify a constant version with `previousVersion`:

``` kotlin
buf {
    // continue to publish schema
    publishSchema = true
    
    // will always check against version 0.1.0
    previousVersion = "0.1.0" 
}
```

#### Artifact details

By default the published image artifact will infer its details from an existing Maven publication if one exists. If one doesn't exist, you have more than one, or you'd like to specify the details yourself, you can configure them:

``` kotlin
buf {
    publishSchema = true
    
    imageArtifact {
        groupId = rootProject.group.toString()
        artifactId = "custom-artifact-id"
        version = rootProject.version.toString()
    }
}
```

### `bufGenerate`

`bufGenerate` is configured as described in the Buf docs. Create a `buf.gen.yaml` in the project root and `bufGenerate` will generate code in the project's build directory at `"$buildDir/bufbuild/generated/<out path from buf.gen.yaml>"`.

An example for Java code generation using the remote plugin:

``` yaml
version: v1
plugins:
  - remote: buf.build/protocolbuffers/plugins/java:<version>
    out: java
```

If you want to use generated code in your build you must add the generated code as a source directory and configure a task dependency to ensure code is generated before compilation:

``` kotlin
// build.gradle.kts

import build.buf.gradle.BUF_GENERATED_DIR

plugins {
    `java`
    id("build.buf") version "<version>"
}

// Add a task dependency for compilation
tasks.named("compileJava").configure { dependsOn("bufGenerate") }

// Add the generated code to the main source set
sourceSets["main"].java { srcDir("$buildDir/$BUF_GENERATED_DIR/java") }

// Configure dependencies for protobuf-java:
repositories { mavenCentral() }

dependencies {
    implementation("com.google.protobuf:protobuf-java:<protobuf version>")
}
```

#### Generating Dependencies

If you'd like to generate code for your dependencies, configure the `bufGenerate` task:

``` kotlin
// build.gradle.kts

buf {
    generate {
        includeImports = true
    }
}
```

Ensure you have an up-to-date `buf.lock` file generated by `buf mod update` or this generation will fail.


#### Further Configuration

By default `bufGenerate` will read the `buf.gen.yaml` template file from the project root directory. You can override the location of the template file:

``` kotlin
// build.gradle.kts

buf {
    generate {
        templateFileLocation = rootProject.file("subdir/buf.gen.yaml")
    }
}
```

## Additional Configuration

The version of Buf used can be configured using the `toolVersion` property on the extension:

``` kotlin
buf {
    toolVersion = <version>
}
```
