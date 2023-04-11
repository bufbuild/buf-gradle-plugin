package build.buf.gradle

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import java.util.zip.GZIPInputStream

fun downloadBufCLI(sourcePath: String, destinationDirectory: File): File {
    val bufFile = File(destinationDirectory, "buf")
    val file = File(".", "buf.tar.gz")
    val url = URL(sourcePath)
    url.openStream().use {
        Channels.newChannel(it).use { rbc ->
            FileOutputStream(file).use { fos ->
                fos.channel.transferFrom(rbc, 0, Long.MAX_VALUE)
            }
        }
    }
    val stream = TarArchiveInputStream(GZIPInputStream(file.inputStream()))
    var entry: TarArchiveEntry? = stream.nextTarEntry
    while (entry != null) {
        val currentEntry = entry
        entry = stream.nextTarEntry
        if (currentEntry.isDirectory) {
            continue
        }
        if (currentEntry.name == "buf/bin/buf") {
            val bytes = ByteArray(currentEntry.size.toInt())
            stream.read(bytes, 0, currentEntry.size.toInt())

            if (!bufFile.parentFile.exists()) {
                bufFile.parentFile.mkdirs()
            }
            IOUtils.write(bytes, bufFile.outputStream())
        }
    }
    stream.close()
    return bufFile
}
