package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.DirectoryName
import com.vsmoraes.fluxfs.FileName
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
}
