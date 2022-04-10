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

import me.champeau.gradle.igp.gitRepositories

buildscript { repositories { gradlePluginPortal() } }

plugins {
    id("me.champeau.includegit") version "0.1.5"
    id("net.vivin.gradle-semantic-build-versioning") version "4.0.0"
}

rootProject.name = "buf-gradle-plugin"

gitRepositories {
    checkoutsDirectory.set(file("build/checkouts"))

    include("junit") {
        uri.set("https://github.com/junit-team/junit5.git")
        commit.set("c8fa9c3")
    }
}
