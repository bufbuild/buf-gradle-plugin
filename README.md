# buf-gradle-plugin

[![Maven Central](https://img.shields.io/badge/dynamic/xml?color=orange&label=maven-central&prefix=v&query=%2F%2Fmetadata%2Fversioning%2Flatest&url=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcom%2Fparmet%2Fbuf-gradle-plugin%2Fmaven-metadata.xml)](https://search.maven.org/artifact/com.parmet/buf-gradle-plugin)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/parmet/buf-gradle-plugin/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/com.parmet.buf)

Linting and breakage-check integration for [Buf](https://github.com/bufbuild/buf) with the
[protobuf-gradle-plugin](https://github.com/google/protobuf-gradle-plugin).

Supports straightforward usage of `buf lint` and a self-contained integration between `buf build` and `buf breaking`. Does not integrate with `buf generate` as it assumes usage of the `protobuf-gradle-plugin` for dependency resolution and code generation.

## Usage

Make sure you have applied the `protobuf-gradle-plugin` to your project.

Create a Buf configuration file in the project directory:

``` yaml
# buf.yaml

version: v1
lint:
  ignore:
    - google
  use:
    - DEFAULT
```

This plugin assumes that all protobuf source is in the `src/main/proto` directory. It works with an implicit Buf workspace that includes `src/main/proto`, the `include` dependencies that the protobuf-gradle-plugin extracts into `"${project.buildDir}/extracted-include-protos"`, and the dependencies that the protobuf-gradle-plugin has been told to generate that are extracted into `"${project.buildDir}/extracted-protos"`. 

See [below](#configuration) for alternative methods of configuration.

Apply the plugin:

``` kotlin
plugins {
    id("com.parmet.buf") version "<version>"
}
```

or

``` kotlin
buildscript {
    dependencies {
        classpath("com.parmet:buf-gradle-plugin:<version>")
    }
}

apply(plugin = "com.parmet.buf")
```

When applied the plugin creates two useful tasks:
- `bufLint` lints protobuf code
- `bufBreaking` checks protobuf against a previous version for
backwards-incompatible changes.

## Configuration

As an alternative to a `buf.yaml` file in the project directory you can specify
the location of `buf.yaml` by configuring the extension: 

``` kotlin
buf {
    configFileLocation = rootProject.file("buf.yaml")
}
```

Or you can share a Buf configuration across projects and specify it via the
dedicated `buf` configuration:

``` kotlin
dependencies {
    buf("com.parmet:shared-buf-configuration:0.1.0")
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
            groupId = "com.parmet"
            artifactId = "shared-buf-configuration"
            version = "0.1.0"
            artifact(file("buf.yaml"))
        }
    }
}
```

### `bufLint`

`bufLint` is configured solely through `buf.yaml` and follows Buf's
standard CLI behavior.

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
src/main/proto/parmet/service/test/test.proto:9:1:Previously present field "1" with name "test_content" on message "TestMessage" was deleted.
```

#### Checking against a static version

If for some reason you do not want to dynamically check against the latest
published version of your schema, you can specify a constant version with
`previousVersion`:

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

## Additional Configuration

The version of Buf used can be configured using the `toolVersion` property on
the extension:

``` kotlin
buf {
    toolVersion = "1.0.0-rc10"
}
```

### Prerequisites

The plugin delegates calls to a Buf Docker image. It requires Docker to be installed wherever it is run.
