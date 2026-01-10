package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.DirectoryName
import com.vsmoraes.fluxfs.FileName
import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.FileAlreadyExists
import com.vsmoraes.fluxfs.exception.FileNotFound
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile

object LocalFilesystemAdapterExtensions {
    internal fun FileName.isFile(): Boolean {
        val f = Path(this)

        return f.exists() && f.isRegularFile()
    }

    internal fun DirectoryName.isDirectory(): Boolean {
        val d = Path(this)

        if (d.isRegularFile()) {
            return d.parent.exists()
        }

        return d.exists() && d.isDirectory()
    }

    internal fun FileName.requireFileExists(): String =
        also {
            if (!isFile()) throw FileNotFound(this)
        }

    internal fun FileName.requireFileNotExists(): String =
        also {
            if (isFile()) throw FileAlreadyExists(this)
        }

    internal fun DirectoryName.requireDirectoryExists(): String =
        also {
            if (!isDirectory()) throw DirectoryNotFound(this)
        }
}
