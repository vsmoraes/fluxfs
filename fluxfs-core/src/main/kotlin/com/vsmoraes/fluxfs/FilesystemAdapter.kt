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
     */
    suspend fun read(fileName: FileName): FluxResult<ByteArray>

    /**
     * Writes content to a new file.
     */
    suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ): FluxResult<Unit>

    /**
     * Checks if a regular file (not a directory) exists at the given path.
     */
    suspend fun fileExists(fileName: FileName): FluxResult<Boolean>

    /**
     * Checks if a directory exists at the given path.
     */
    suspend fun directoryExists(directoryName: DirectoryName): FluxResult<Boolean>

    /**
     * Creates a directory at the specified path.
     *
     * @param recursive when true, creates all missing parent directories;
     *                  when false, throws DirectoryNotFound if the immediate parent doesn't exist
     */
    suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean = true,
    ): FluxResult<Unit>
}
