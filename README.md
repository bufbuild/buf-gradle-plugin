# buf-gradle-plugin

Integration for [Buf](https://github.com/bufbuild/buf) with the
[protobuf-gradle-plugin](https://github.com/google/protobuf-gradle-plugin).

### Usage

Create a Buf configuration file in the project directory:

``` yaml
# buf.yaml

build:
  roots:
    - src/main/proto

    # The `protobuf-gradle-plugin` extracts and merges protobuf dependencies to
    # `build/extracted-include-protos`, so we tell Buf where to find them.
    - build/extracted-include-protos/main
lint:
  ignore:
    - google
  use:
    - DEFAULT
```

Apply the plugin:

``` groovy
plugins {
  id "com.parmet.buf" version "<version>"
}
```

or

``` groovy
buildscript {
  dependencies {
    classpath 'com.parmet.buf:buf-gradle-plugin:<version>'
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
the protobuf schema to validate the current version. At the moment, this plugin
uses Buf to create an image from the current protobuf schema and publishes it as
a Maven publication. Then, in the next build, you can specify a `previousVersion`
in the plugin configuration, and the plugin will use that Maven artifact as its
input for validation.

For example, first publish the project with no configuration and version `0.1.0`.
This requires configuring the plugin: 

``` groovy
buf {
  publishSchema = true
}
```

Then configure the plugin:

```groovy
buf {
  previousVersion = "0.1.0"
}
```

And the plugin will run Buf checking the project's current schema against
version `0.1.0`.

The plugin simply delegates calls to the latest Buf Docker image. It requires
Docker to be installed wherever it is run.
