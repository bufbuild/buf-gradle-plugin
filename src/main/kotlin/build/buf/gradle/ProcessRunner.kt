// Copyright 2023 Buf Technologies, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package build.buf.gradle

import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors

// See https://github.com/diffplug/spotless/blob/0fd20bb80c6c426d20e0a3157c3c2b89317032da/lib/src/main/java/com/diffplug/spotless/ProcessRunner.java
internal class ProcessRunner : Closeable {
    private val threadStdOut = Executors.newSingleThreadExecutor()
    private val threadStdErr = Executors.newSingleThreadExecutor()
    private val bufStdOut = ByteArrayOutputStream()
    private val bufStdErr = ByteArrayOutputStream()

    fun shell(workingDir: File, args: List<Any>): Result {
        val processBuilder = ProcessBuilder(args.map(Any::toString))
        processBuilder.directory(workingDir)
        val process = processBuilder.start()
        val out = threadStdOut.submit<ByteArray> { drain(process.inputStream, bufStdOut) }
        val err = threadStdOut.submit<ByteArray> { drain(process.errorStream, bufStdErr) }
        val exitCode = process.waitFor()
        return Result(args, exitCode, out.get(), err.get())
    }

    private fun drain(input: InputStream, output: ByteArrayOutputStream): ByteArray {
        output.reset()
        input.copyTo(output)
        return output.toByteArray()
    }

    override fun close() {
        threadStdOut.shutdown()
        threadStdErr.shutdown()
    }

    class Result(
        private val args: List<Any>,
        val exitCode: Int,
        val stdOut: ByteArray,
        val stdErr: ByteArray
    ) {
        override fun toString(): String {
            val builder = StringBuilder()
            builder.appendLine("> arguments: $args")
            builder.appendLine("> exit code: $exitCode")
            val perStream = { name: String, content: ByteArray ->
                val string = content.toString(StandardCharsets.UTF_8)
                if (string.isEmpty()) {
                    builder.appendLine("> $name: (empty)")
                } else {
                    val lines = string.replace("\r", "").lines()
                    if (lines.size == 1) {
                        builder.appendLine("> $name: ${lines.single()}")
                    } else {
                        builder.appendLine("> $name: (below)")
                        lines.forEach { builder.appendLine("> $it") }
                    }
                }
            }
            perStream("   stdout", stdOut)
            perStream("   stderr", stdErr)
            return builder.toString()
        }
    }
}
