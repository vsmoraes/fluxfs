package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.FileAlreadyExists
import com.vsmoraes.fluxfs.exception.FileNotFound
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class LocalFilesystemAdapter : FilesystemAdapter {
    override fun read(file: String): ByteArray {
        if (fileExists(file).not()) throw FileNotFound(file)

        return Path(file).readBytes()
    }

    override fun write(
        file: String,
        content: ByteArray,
    ) {
        if (directoryExists(Path(file).parent.toString()).not()) throw DirectoryNotFound(file)
        if (fileExists(file)) throw FileAlreadyExists(file)

        Path(file).writeBytes(content)
    }

    override fun fileExists(file: String): Boolean {
        val f = Path(file)

        return f.exists() && f.isRegularFile()
    }

    override fun directoryExists(path: String): Boolean {
        val d = Path(path)

        if (d.isRegularFile()) {
            return d.parent.exists()
        }

        return d.exists() && d.isDirectory()
    }

    override fun createDirectory(
        path: String,
        recursive: Boolean,
    ) {
        val dir = Path(path).parent

        if (dir.exists()) return

        if (recursive) {
            dir.createDirectories()
            return
        }

        if (!directoryExists(dir.parent.toString())) throw DirectoryNotFound(dir.parent.toString())
        dir.createDirectory()
    }
}
