package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.DirectoryName
import com.vsmoraes.fluxfs.FileName
import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.PathNormalizer.parent
import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.ReadFileException
import com.vsmoraes.fluxfs.exception.WriteFileException
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.isDirectory
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.isFile
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.requireDirectoryExists
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.requireFileExists
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.requireFileNotExists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class LocalFilesystemAdapter : FilesystemAdapter {
    override suspend fun read(fileName: FileName): ByteArray =
        withContext(Dispatchers.IO) {
            val file = fileName.requireFileExists()

            try {
                Path(file).readBytes()
            } catch (e: Throwable) {
                throw ReadFileException("Error reading $file from local storage", e)
            }
        }

    override suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ) = withContext(Dispatchers.IO) {
        val file = fileName.requireFileNotExists()
        fileName.parent().requireDirectoryExists()

        try {
            Path(file).writeBytes(content)
        } catch (e: Throwable) {
            throw WriteFileException("Error writing $file to local storage", e)
        }
    }

    override suspend fun fileExists(fileName: FileName): Boolean =
        withContext(Dispatchers.IO) {
            fileName.isFile()
        }

    override suspend fun directoryExists(directoryName: DirectoryName) =
        withContext(Dispatchers.IO) {
            directoryName.isDirectory()
        }

    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean,
    ) {
        val dir = Path(directoryName).parent

        if (withContext(Dispatchers.IO) { dir.exists() }) {
            return
        }

        if (recursive) {
            withContext(Dispatchers.IO) {
                dir.createDirectories()
            }
            return
        }

        if (!directoryExists(dir.parent.toString())) {
            throw DirectoryNotFound(dir.parent.toString())
        }

        withContext(Dispatchers.IO) {
            dir.createDirectory()
        }
    }
}
