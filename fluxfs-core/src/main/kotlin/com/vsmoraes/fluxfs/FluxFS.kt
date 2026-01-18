package com.vsmoraes.fluxfs

typealias FileName = String
typealias DirectoryName = String

/**
 * Main entry point for FluxFS filesystem operations.
 *
 * FluxFS provides a unified API for working with different filesystem backends
 * through the adapter pattern. All operations return [FluxResult] for explicit
 * error handling without exceptions.
 *
 * Example usage:
 * ```kotlin
 * fluxFS.write("file.txt", "Hello".encodeToByteArray())
 *     .onSuccess { println("File written!") }
 *     .onError { error -> println("Failed: $error") }
 * ```
 */
class FluxFS(
    val filesystem: FilesystemAdapter,
) : FilesystemAdapter by filesystem
