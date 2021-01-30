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

import java.io.File
import java.io.IOException

fun File.newFolder(vararg folderNames: String): File {
    var last = this
    folderNames.forEach {
        validate(it)
        last = File(last, it)
        if (!last.mkdir()) {
            throw IOException("folder $it already exists")
        }
    }
    return last
}

private fun validate(name: String) {
    val tempFile = File(name)
    if (tempFile.parent != null) {
        throw IOException(
            "Folder name cannot consist of multiple path components. " +
                "Use newFolder('MyParentFolder', 'MyFolder') to create nested folders."
        )
    }
}

fun File.newFile(child: String) =
    File(this, child).apply {
        if (!createNewFile()) {
            throw IOException("a file with name $this already exists in the test folder")
        }
    }
