# buf-gradle-plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.parmet/buf-gradle-plugin)](https://search.maven.org/artifact/com.parmet/buf-gradle-plugin)
[![Gradle Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/parmet/buf-gradle-plugin/maven-metadata.xml.svg?label=gradle-portal&color=yellowgreen)](https://plugins.gradle.org/plugin/com.parmet.buf)

Integration for [Buf](https://github.com/bufbuild/buf) with the
[protobuf-gradle-plugin](https://github.com/google/protobuf-gradle-plugin).

### Usage

Create a Buf configuration file in the project directory:

``` yaml
# buf.yaml

build:
  roots:
    - src/main/proto

    # The protobuf-gradle-plugin extracts and merges protobuf dependencies to
    # `build/extracted-include-protos`, so we tell Buf where to find them.
    - build/extracted-include-protos/main
lint:
  ignore:
    - google
  use:
    - DEFAULT
```

Alternatively you can specify a configuration file location, for example to share a config file between subprojects:

``` groovy
buf {
    configFileLocation = rootProject.file("buf.yaml")
}
```

Apply the plugin:

``` groovy
plugins {
  id 'com.parmet.buf' version '<version>'
}
```

or

``` groovy
buildscript {
  dependencies {
    classpath 'com.parmet:buf-gradle-plugin:<version>'
  }
}

apply plugin: 'com.parmet.buf'
```

When applied the plugin creates two useful tasks:
- `bufCheckLint` lints protobuf code
- `bufCheckBreaking` checks protobuf against a previous version for
backwards-incompatible changes.

`bufCheckLint` is configured solely through `buf.yaml` and follows Buf's
standard CLI behavior.

`bufCheckBreaking` is more complicated since it requires a previous version of
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

``` groovy
buf {
  publishSchema = true
  // previousVersion = null (default)
}
```

Then configure the plugin to check against that version:

```groovy
buf {
  // continue to publish schema
  publishSchema = true

  previousVersion = "0.1.0"
}
```

The plugin will run Buf checking the project's current schema against
version `0.1.0`:

```
> Task :bufCheckBreaking FAILED
src/main/proto/parmet/service/test/test.proto:9:1:Previously present field "1" with name "test_content" on message "TestMessage" was deleted.
```

### Prerequisites

The plugin delegates calls to the latest Buf Docker image. It requires
Docker to be installed wherever it is run.
