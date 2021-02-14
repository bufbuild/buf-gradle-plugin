# buf-gradle-plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.parmet/buf-gradle-plugin)](https://search.maven.org/artifact/com.parmet/buf-gradle-plugin)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/parmet/buf-gradle-plugin/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/com.parmet.buf)

Integration for [Buf](https://github.com/bufbuild/buf) with the
[protobuf-gradle-plugin](https://github.com/google/protobuf-gradle-plugin).

### Usage

Create a Buf configuration file in the project directory:

``` yaml
# buf.yaml

version: v1beta1
build:
  roots:
    - src/main/proto

    # The protobuf-gradle-plugin extracts and merges protobuf dependencies to
    # `build/extracted-include-protos`, so tell Buf where to find them.
    - build/extracted-include-protos/main
lint:
  ignore:
    - google
  use:
    - DEFAULT
```

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

### Configuration

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

#### `bufLint`

`bufLint` is configured solely through `buf.yaml` and follows Buf's
standard CLI behavior.

#### `bufBreaking`

`bufBreaking` is more complicated since it requires a previous version of
the protobuf schema to validate the current version. Buf's built-in Git
integration isn't quite enough since it requires a buildable protobuf source set
and the `protobuf-gradle-plugin`'s merge step typically targets the project
build directory, which is ephemeral and not committed.

At the moment, this plugin uses Buf to create an image from the current protobuf
schema and publishes it as a Maven publication. Then, in the next build, you can
specify a `previousVersion` in the plugin configuration, and the plugin will use
that Maven artifact as its input for validation.

For example, first publish the project with `publishSchema` set to `true` and
version `0.1.0`:

``` kotlin
buf {
    publishSchema = true
    // previousVersion = null (default)
}
```

Then configure the plugin to check against that version:

``` kotlin
buf {
    // continue to publish schema
    publishSchema = true

    previousVersion = "0.1.0"
}
```

The plugin will run Buf checking the project's current schema against
version `0.1.0`:

```
> Task :bufBreaking FAILED
src/main/proto/parmet/service/test/test.proto:9:1:Previously present field "1" with name "test_content" on message "TestMessage" was deleted.
```

By default the published image artifact will infer its details from an existing
Maven publication if one exists. If one doesn't exist, you have more than one,
or you'd like to specify the details yourself, you can configure them:

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

### Additional Configuration

The version of Buf used can be configured using the `toolVersion` property on
the extension:

``` kotlin
buf {
    toolVersion = "0.37.0"
}
```

### Prerequisites

The plugin delegates calls to a Buf Docker image. It requires
Docker to be installed wherever it is run.
