package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.DirectoryName
import com.vsmoraes.fluxfs.FileName
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class FileManager {
    fun isFile(file: FileName): Boolean {
        val f = Path(file)

        return f.exists() && f.isRegularFile()
    }

    fun readFile(file: FileName): ByteArray = Path(file).readBytes()

    fun writeFile(
        file: FileName,
        content: ByteArray,
    ) = Path(file).writeBytes(content)

    fun isDirectory(directory: DirectoryName): Boolean {
        val d = Path(directory)

        if (d.isRegularFile()) {
            return d.parent.exists()
        }

        return d.exists() && d.isDirectory()
    }

    fun createDirectory(directory: DirectoryName) = Path(directory).createDirectory()

    fun createDirectoryRecursively(directory: DirectoryName) = Path(directory).createDirectories()
}
