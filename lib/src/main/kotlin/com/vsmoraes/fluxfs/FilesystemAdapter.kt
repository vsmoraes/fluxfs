package com.vsmoraes.fluxfs

interface FilesystemAdapter {
    suspend fun read(file: String): ByteArray

    suspend fun write(
        file: String,
        content: ByteArray,
    )

    fun fileExists(file: String): Boolean

    fun directoryExists(path: String): Boolean

    fun createDirectory(
        path: String,
        recursive: Boolean = true,
    )
}
