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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Local filesystem adapter implementation for FluxFS.
 *
 * This adapter provides access to the local operating system's filesystem
 * using standard Kotlin/JVM file operations.
 */
class LocalFilesystemAdapter(
    private val fileManager: FileManager = FileManager(),
) : FilesystemAdapter {
    override suspend fun read(fileName: FileName) =
        withContext(Dispatchers.IO) {
            if (!fileManager.isFile(fileName)) {
                return@withContext FileNotFound(fileName)
            }

            runCatching {
                fileManager.readFile(fileName)
            }.fold(
                onSuccess = { FluxResult.Success(it) },
                onFailure = { IOError("Error reading $fileName from local storage", it) },
            )
        }

    override suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ) = withContext(Dispatchers.IO) {
        val parentDir = fileName.parent()

        if (!fileManager.isDirectory(parentDir)) {
            return@withContext DirectoryNotFound(parentDir)
        }

        if (fileManager.isFile(fileName)) {
            return@withContext FileAlreadyExists(fileName)
        }

        runCatching {
            fileManager.writeFile(fileName, content)
        }.fold(
            onSuccess = { FluxResult.Success(Unit) },
            onFailure = { IOError("Error writing $fileName to local storage", it) },
        )
    }

    override suspend fun fileExists(fileName: FileName) =
        withContext(Dispatchers.IO) {
            runCatching {
                fileManager.isFile(fileName)
            }.fold(
                onSuccess = { FluxResult.Success(it) },
                onFailure = { IOError("Error checking if file exists: $fileName", it) },
            )
        }

    override suspend fun directoryExists(directoryName: DirectoryName) =
        withContext(Dispatchers.IO) {
            runCatching {
                fileManager.isDirectory(directoryName)
            }.fold(
                onSuccess = { FluxResult.Success(it) },
                onFailure = { IOError("Error checking if directory exists: $directoryName", it) },
            )
        }

    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean,
    ) = withContext(Dispatchers.IO) {
        if (fileManager.isDirectory(directoryName)) {
            return@withContext DirectoryAlreadyExists(directoryName)
        }

        if (!recursive) {
            val parent = directoryName.parent()
            if (!fileManager.isDirectory(parent)) {
                return@withContext DirectoryNotFound(parent)
            }
        }

        runCatching {
            if (recursive) {
                fileManager.createDirectoryRecursively(directoryName)
            } else {
                fileManager.createDirectory(directoryName)
            }
        }.fold(
            onSuccess = { FluxResult.Success(Unit) },
            onFailure = { IOError("Error creating directory $directoryName", it) },
        )
    }
}
