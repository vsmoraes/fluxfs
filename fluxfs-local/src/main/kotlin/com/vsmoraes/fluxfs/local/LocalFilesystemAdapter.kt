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

class LocalFilesystemAdapter(
    private val fileManager: FileManager = FileManager(),
) : FilesystemAdapter {
    override suspend fun read(fileName: FileName) =
        withContext(Dispatchers.IO) {
            if (!fileManager.isFile(fileName)) {
                return@withContext FileNotFound(fileName)
            }

            runCatching { fileManager.readFile(fileName) }
                .fold(
                    onSuccess = {
                        FluxResult.Success(it)
                    },
                    onFailure = {
                        IOError("Error reading $fileName from local storage", it)
                    },
                )
        }

    override suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ) = withContext(Dispatchers.IO) {
        if (!fileManager.isDirectory(fileName.parent())) {
            return@withContext DirectoryNotFound(fileName.parent())
        }
        if (fileManager.isFile(fileName)) {
            return@withContext FileAlreadyExists(fileName)
        }

        runCatching { fileManager.writeFile(fileName, content) }
            .fold(
                onSuccess = {
                    FluxResult.Success(it)
                },
                onFailure = {
                    IOError("Error writing $fileName to local storage", it)
                },
            )
    }

    override suspend fun fileExists(fileName: FileName) =
        withContext(Dispatchers.IO) {
            runCatching { fileManager.isFile(fileName) }
                .fold(
                    onSuccess = {
                        FluxResult.Success(it)
                    },
                    onFailure = {
                        IOError("Error checking if file exists $fileName", it)
                    },
                )
        }

    override suspend fun directoryExists(directoryName: DirectoryName) =
        withContext(Dispatchers.IO) {
            runCatching { fileManager.isDirectory(directoryName) }
                .fold(
                    onSuccess = {
                        FluxResult.Success(it)
                    },
                    onFailure = {
                        IOError("Error checking if directory exists $directoryName", it)
                    },
                )
        }

    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean,
    ) = withContext(Dispatchers.IO) {
        val parent = directoryName.parent()

        if (fileManager.isDirectory(directoryName)) {
            return@withContext DirectoryAlreadyExists(directoryName)
        }
        if (!recursive && !fileManager.isDirectory(parent)) {
            return@withContext DirectoryNotFound(parent)
        }

        runCatching {
            if (recursive) {
                fileManager.createDirectory(directoryName)
            } else {
                fileManager.createDirectoryRecursively(directoryName)
            }
        }.fold(
            onSuccess = {
                FluxResult.Success(Unit)
            },
            onFailure = {
                IOError("Error creating directory $directoryName", it)
            },
        )
    }
}
