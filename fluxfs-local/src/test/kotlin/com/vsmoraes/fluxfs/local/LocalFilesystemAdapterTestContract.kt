package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.FilesystemAdapterTestContract
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class LocalFilesystemAdapterTestContract :
    FilesystemAdapterTestContract(
        name = "LocalFilesystemAdapter",
        createAdapter = {
            every { fileManager.isFile(INVALID_FILE) } returns false
            every { fileManager.isFile(VALID_FILE) } returns false

            LocalFilesystemAdapter(fileManager)
        },
        createValidFile = { fileName, fileContent ->
            every { fileManager.isFile(fileName) } returns true
            every { fileManager.readFile(fileName) } returns fileContent.toByteArray()
        },
        createValidDirectory = { directoryName ->
            every { fileManager.isDirectory(directoryName) } returns true
        },
        mockReadException = {
            every { fileManager.readFile(VALID_FILE) } throws Exception("Something went wrong")
        },
        mockDirectoryNotExist = { directory ->
            every { fileManager.isDirectory(directory) } returns false
        },
        beforeEach = {
            clearMocks(fileManager)
        },
    ) {
    companion object {
        private val fileManager = mockk<FileManager>(relaxed = true)
    }
}
