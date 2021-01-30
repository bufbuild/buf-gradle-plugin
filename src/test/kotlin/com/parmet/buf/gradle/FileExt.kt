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
