# FluxFS

FluxFS is a Kotlin filesystem abstraction library that provides a consistent, cross-backend API for filesystem operations. 

FluxFS is structured as a multi-module project:

- `fluxfs-core` – Core interfaces, value types, and result model
- `fluxfs-local` – Local filesystem adapter
- `fluxfs-s3` – AWS S3 adapter

## Table of Contents

- [Features](#features)
- [Modules](#modules)
- [Architecture](#architecture)
- [Installation](#installation)
- [Core API Reference](#core-api-reference)
- [Advanced Usage](#advanced-usage)
  - [Handling Results](#handling-results)
  - [Custom Adapters](#custom-adapters)
- [Usage Examples](#usage-examples)
  - [Local Filesystem](#local-filesystem)
  - [AWS S3](#aws-s3)
- [Roadmap](#roadmap)
- [Testing](#testing)
- [License](#license)

---

## Features

- Unified filesystem API across different backends
- Built-in adapters for local filesystem and AWS S3 (more coming soon!)
- Result-oriented API instead of exceptions
- Suspendable, coroutine-friendly API
- Easy to implement custom adapters for other storage systems

---

## Modules

**fluxfs-core**  
Contains core interfaces and value types.

**fluxfs-local**  
Implements `FilesystemAdapter` using the local OS filesystem.

**fluxfs-s3**  
Implements `FilesystemAdapter` using AWS S3.

---

## Architecture

FluxFS is built around the **adapter pattern**. The core defines the contract:

- `FilesystemAdapter` – interface for file operations
- `FluxResult` – result type encapsulating success or error

Adapters implement `FilesystemAdapter` to provide backend-specific behavior while returning `FluxResult` for all operations. This ensures consistent behavior across all backends.

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("com.vsmoraes:fluxfs-core:<version>")
    implementation("com.vsmoraes:fluxfs-local:<version>")
    implementation("com.vsmoraes:fluxfs-s3:<version>")
}
```

Replace `<version>` with the latest published version.

---

## Core API Reference

All filesystem operations are performed through the `FilesystemAdapter` interface. All methods return a `FluxResult` representing either success or error.

| Method | Parameters | Return Type | Description |
|--------|------------|-------------|-------------|
| `read` | `fileName: FileName` | `FluxResult<ByteArray>` | Reads the contents of the specified file. Returns `Success` with the file bytes or `Error` if the file does not exist or cannot be read. |
| `write` | `fileName: FileName`, `content: ByteArray` | `FluxResult<Unit>` | Writes the byte content to the specified file. Returns `Success` on success or `Error` on failure (e.g., IO error, permission issue). |
| `fileExists` | `fileName: FileName` | `FluxResult<Boolean>` | Checks if the specified file exists. Returns `Success(true)` if it exists, `Success(false)` if not, or `Error` on IO issues. |
| `directoryExists` | `directoryName: DirectoryName` | `FluxResult<Boolean>` | Checks if the specified directory exists. Returns `Success(true)` if it exists, `Success(false)` if not, or `Error` on IO issues. |
| `createDirectory` | `directoryName: DirectoryName`, `recursive: Boolean = true` | `FluxResult<Unit>` | Creates the specified directory. If `recursive` is true, all missing parent directories are created. Returns `Success` on success or `Error` if creation fails (e.g., directory already exists or IO error). |

### Notes

- **All methods are suspendable** – they must be called from a coroutine or another suspend function.
- **Error handling is explicit** – no method throws exceptions for expected failures. All errors are represented via `FluxResult.Error` types: `FileNotFound`, `FileAlreadyExists`, `DirectoryNotFound`, `DirectoryAlreadyExists`, `IOError`.
- **Consistency across adapters** – the same method called on `LocalFilesystemAdapter` or `S3FilesystemAdapter` returns standardized errors for similar conditions.
- **Helper functions**: You can use `isSuccess()`, `isError()`, `onSuccess { }`, `onError { }` for convenient, fluent handling of results.

Example usage:

```kotlin
adapter.read("file.txt")
    .onSuccess { bytes -> println("File content: ${bytes.decodeToString()}") }
    .onError { error -> println("Failed to read file: $error") }
```

---

## Advanced Usage

### Handling Results

```kotlin
val result = adapter.read("file.txt")

when (result) {
    is FluxResult.Success -> println(result.value.decodeToString())
    is FluxResult.Error -> println("Read failed: $result")
}

adapter.read("file.txt")
    .onSuccess { bytes -> println(bytes.decodeToString()) }
    .onError { error -> println("Failed: $error") }
```

All adapters produce consistent errors. Missing files, permission errors, or IO failures return the same standardized `FluxResult.Error` types regardless of backend.

### Custom Adapters

Implement `FilesystemAdapter` in a new module:

```kotlin
class MyCustomAdapter : FilesystemAdapter {
    override suspend fun read(fileName: FileName): FluxResult<ByteArray> { /* ... */ }
    override suspend fun write(fileName: FileName, content: ByteArray): FluxResult<Unit> { /* ... */ }
    override suspend fun fileExists(fileName: FileName): FluxResult<Boolean> { /* ... */ }
    override suspend fun directoryExists(directoryName: DirectoryName): FluxResult<Boolean> { /* ... */ }
    override suspend fun createDirectory(directoryName: DirectoryName, recursive: Boolean): FluxResult<Unit> { /* ... */ }
}
```

---

## Usage Examples

### Local Filesystem

```kotlin
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapter

val adapter = LocalFilesystemAdapter()

adapter.write("/tmp/hello.txt", "Hello FluxFS".encodeToByteArray())
    .onError { println("Write failed: $it") }

adapter.read("/tmp/hello.txt")
    .onSuccess { println(it.decodeToString()) }
    .onError { println("Read failed: $it") }

adapter.fileExists("/tmp/hello.txt")
    .onSuccess { exists -> println("Exists: $exists") }
```

### AWS S3

```kotlin
import aws.sdk.kotlin.services.s3.S3Client
import com.vsmoraes.fluxfs.s3.S3FilesystemAdapter

val s3 = S3Client { region = "us-east-1" }
val adapter = S3FilesystemAdapter(s3, "my-bucket")

adapter.write("folder/file.txt", "Hello S3".encodeToByteArray())
adapter.read("folder/file.txt")
```

AWS credentials are resolved via standard SDK mechanisms.

---

## Roadmap

- [ ] In-memory adapter for testing
- [ ] Streaming support for large files
- [ ] Directory listing API
- [ ] File metadata support (timestamps, permissions)
- [ ] Kotlin Multiplatform support
- [ ] Google Cloud Storage adapter
- [ ] Azure Blob Storage adapter

---

## License

FluxFS is released under the MIT License.
