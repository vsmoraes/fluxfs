package com.vsmoraes.fluxfs

/**
 * Abstraction for filesystem operations across different storage backends.
 *
 * All implementations must adhere to the same behavioral contract to ensure consistent
 * behavior regardless of the underlying storage mechanism.
 */
interface FilesystemAdapter {
    /**
     * Reads the entire file content into memory.
     *
     * @throws com.vsmoraes.fluxfs.exception.FileNotFound if the file doesn't exist
     */
    suspend fun read(fileName: FileName): ByteArray

    /**
     * Writes content to a new file.
     *
     * @throws com.vsmoraes.fluxfs.exception.DirectoryNotFound if the parent directory doesn't exist
     * @throws com.vsmoraes.fluxfs.exception.FileAlreadyExists if the provided file already exists
     */
    suspend fun write(
        fileName: FileName,
        content: ByteArray,
    )

    /**
     * Checks if a regular file (not a directory) exists at the given path.
     */
    suspend fun fileExists(fileName: FileName): Boolean

    /**
     * Checks if a directory exists at the given path.
     */
    suspend fun directoryExists(directoryName: DirectoryName): Boolean

    /**
     * Creates a directory at the specified path.
     *
     * @param recursive when true, creates all missing parent directories;
     *                  when false, throws DirectoryNotFound if the immediate parent doesn't exist
     * @throws com.vsmoraes.fluxfs.exception.DirectoryNotFound when recursive is false and parent directory is missing
     */
    suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean = true,
    )
}
