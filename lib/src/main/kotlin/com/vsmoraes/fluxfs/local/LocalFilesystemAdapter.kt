package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.DirectoryName
import com.vsmoraes.fluxfs.FileName
import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.FluxResult
import com.vsmoraes.fluxfs.FluxResult.Error.DirectoryAlreadyExists
import com.vsmoraes.fluxfs.FluxResult.Error.DirectoryNotFound
import com.vsmoraes.fluxfs.FluxResult.Error.FileAlreadyExists
import com.vsmoraes.fluxfs.FluxResult.Error.FileNotFound
import com.vsmoraes.fluxfs.FluxResult.Error.IOError
import com.vsmoraes.fluxfs.PathNormalizer.parent
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.isDirectory
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapterExtensions.isFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

class LocalFilesystemAdapter : FilesystemAdapter {
    override suspend fun read(fileName: FileName) =
        withContext(Dispatchers.IO) {
            if (!fileName.isFile()) return@withContext FileNotFound(fileName)

            runCatching { Path(fileName).readBytes() }
                .fold(
                    onSuccess = { FluxResult.Success(it) },
                    onFailure = { IOError("Error reading $fileName from local storage", it) },
                )
        }

    override suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ) = withContext(Dispatchers.IO) {
        if (!fileName.parent().isDirectory()) return@withContext DirectoryNotFound(fileName.parent())
        if (fileName.isFile()) return@withContext FileAlreadyExists(fileName)

        runCatching { Path(fileName).writeBytes(content) }
            .fold(
                onSuccess = { FluxResult.Success(it) },
                onFailure = { IOError("Error writing $fileName to local storage", it) },
            )
    }

    override suspend fun fileExists(fileName: FileName) =
        withContext(Dispatchers.IO) {
            runCatching { fileName.isFile() }
                .fold(
                    onSuccess = { FluxResult.Success(it) },
                    onFailure = { IOError("Error checking if file exists $fileName", it) },
                )
        }

    override suspend fun directoryExists(directoryName: DirectoryName) =
        withContext(Dispatchers.IO) {
            runCatching { directoryName.isDirectory() }
                .fold(
                    onSuccess = { FluxResult.Success(it) },
                    onFailure = { IOError("Error checking if directory exists $directoryName", it) },
                )
        }

    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean,
    ) = withContext(Dispatchers.IO) {
        val dir = Path(directoryName)
        val parent = dir.parent

        if (dir.exists()) return@withContext DirectoryAlreadyExists(dir.toString())
        if (!recursive && !parent.exists()) return@withContext DirectoryNotFound(parent.toString())

        runCatching {
            if (recursive) parent.createDirectories() else parent.createDirectory()
        }.fold(
            onSuccess = { FluxResult.Success(Unit) },
            onFailure = { IOError("Error creating directory $directoryName", it) },
        )
    }
}
