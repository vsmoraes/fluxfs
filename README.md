# 🗂️ FluxFS

> **A modern Kotlin filesystem abstraction library that provides a unified, type-safe API for working with different storage backends.**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Gradle](https://img.shields.io/badge/gradle-9.2.1-blue.svg?logo=gradle)](https://gradle.org)

FluxFS eliminates the complexity of working with different storage systems by providing a single, consistent API. Whether you're working with local files, S3 buckets, or planning to add Google Cloud Storage, FluxFS lets you write code once and swap backends effortlessly.

## ✨ Features

- **🎯 Unified API** - One interface for all storage backends
- **🛡️ Type-Safe** - No runtime exceptions from file operations; explicit error handling with `FluxResult`
- **⚡ Coroutine-Friendly** - All operations are suspendable and non-blocking
- **🔌 Pluggable Adapters** - Built-in support for local filesystem and AWS S3, with easy extension points
- **🧪 Testable** - Includes test contracts and fixtures for implementing custom adapters
- **🌱 Spring Boot Ready** - Auto-configuration for seamless Spring integration

## 📦 Modules

| Module | Description | Artifacts |
|--------|-------------|-----------|
| **fluxfs-core** | Core interfaces and result types | `com.vsmoraes.fluxfs:fluxfs-core` |
| **fluxfs-local** | Local filesystem adapter | `com.vsmoraes.fluxfs:fluxfs-local` |
| **fluxfs-s3** | AWS S3 adapter | `com.vsmoraes.fluxfs:fluxfs-s3` |
| **fluxfs-spring-boot** | Spring Boot auto-configuration | `com.vsmoraes.fluxfs:fluxfs-spring-boot-starter` |

## 🚀 Quick Start

### Installation

Add FluxFS to your project using Gradle:

```kotlin
dependencies {
    // Core library (required)
    implementation("com.vsmoraes.fluxfs:fluxfs-core:1.0.0")
    
    // Choose your adapter(s)
    implementation("com.vsmoraes.fluxfs:fluxfs-local:1.0.0")
    implementation("com.vsmoraes.fluxfs:fluxfs-s3:1.0.0")
    
    // Optional: Spring Boot support
    implementation("com.vsmoraes.fluxfs:fluxfs-spring-boot-starter:1.0.0")
}
```

### Basic Usage

```kotlin
import com.vsmoraes.fluxfs.FluxFS
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapter

suspend fun main() {
    // Create a FluxFS instance with local filesystem
    val fluxFS = FluxFS.local()
    
    // Write a file
    fluxFS.write("/tmp/hello.txt", "Hello, FluxFS!".encodeToByteArray())
        .onSuccess { println("✓ File written successfully") }
        .onError { error -> println("✗ Failed: $error") }
    
    // Read the file back
    fluxFS.read("/tmp/hello.txt")
        .onSuccess { bytes -> 
            println("File content: ${bytes.decodeToString()}") 
        }
        .onError { error -> 
            println("Failed to read: $error") 
        }
}
```

## 📚 Core Concepts

### FilesystemAdapter

The heart of FluxFS is the `FilesystemAdapter` interface, which defines five core operations:

```kotlin
interface FilesystemAdapter {
    suspend fun read(fileName: FileName): FluxResult<ByteArray>
    suspend fun write(fileName: FileName, content: ByteArray): FluxResult<Unit>
    suspend fun fileExists(fileName: FileName): FluxResult<Boolean>
    suspend fun directoryExists(directoryName: DirectoryName): FluxResult<Boolean>
    suspend fun createDirectory(directoryName: DirectoryName, recursive: Boolean = true): FluxResult<Unit>
}
```

### FluxResult

FluxFS uses `FluxResult` instead of throwing exceptions, making error handling explicit and type-safe:

```kotlin
sealed interface FluxResult<out T> {
    data class Success<T>(val value: T) : FluxResult<T>
    
    sealed interface Error : FluxResult<Nothing> {
        data class FileNotFound(val path: String) : Error
        data class FileAlreadyExists(val path: String) : Error
        data class DirectoryNotFound(val path: String) : Error
        data class DirectoryAlreadyExists(val path: String) : Error
        data class IOError(val message: String, val cause: Throwable?) : Error
    }
}
```

### Working with Results

FluxFS provides rich extension functions for handling results:

```kotlin
// Pattern matching
when (val result = fluxFS.read("file.txt")) {
    is FluxResult.Success -> println(result.value.decodeToString())
    is FluxResult.Error.FileNotFound -> println("File not found!")
    is FluxResult.Error -> println("Other error: $result")
}

// Functional style with chaining
fluxFS.read("file.txt")
    .map { it.decodeToString() }
    .map { it.uppercase() }
    .onSuccess { println(it) }
    .onError { println("Error: $it") }

// Get value or default
val content = fluxFS.read("file.txt")
    .getOrElse { "Default content".encodeToByteArray() }

// Get value or throw
val bytes = fluxFS.read("file.txt").getOrThrow()
```

## 💡 Usage Examples

### Local Filesystem

```kotlin
import com.vsmoraes.fluxfs.FluxFS
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapter

val fluxFS = FluxFS(LocalFilesystemAdapter())

// Create a directory
fluxFS.createDirectory("/tmp/myapp/data", recursive = true)
    .onSuccess { println("Directory created") }

// Check if file exists
val exists = fluxFS.fileExists("/tmp/myapp/config.json")
    .getOrElse { false }

if (!exists) {
    // Write configuration
    val config = """{"version": "1.0", "enabled": true}"""
    fluxFS.write("/tmp/myapp/config.json", config.encodeToByteArray())
}

// Read and parse
fluxFS.read("/tmp/myapp/config.json")
    .map { it.decodeToString() }
    .onSuccess { json -> 
        println("Config: $json")
    }
```

### AWS S3

```kotlin
import aws.sdk.kotlin.services.s3.S3Client
import com.vsmoraes.fluxfs.FluxFS
import com.vsmoraes.fluxfs.s3.S3FilesystemAdapter

// Create S3 client
val s3Client = S3Client { 
    region = "us-east-1" 
}

// Create FluxFS with S3 adapter
val fluxFS = FluxFS(S3FilesystemAdapter(s3Client, "my-bucket"))

// S3 works exactly like local filesystem!
fluxFS.createDirectory("reports/2024", recursive = true)

fluxFS.write(
    "reports/2024/summary.txt",
    "Q4 Summary: Revenue increased 20%".encodeToByteArray()
)

fluxFS.read("reports/2024/summary.txt")
    .onSuccess { bytes -> 
        println(bytes.decodeToString()) 
    }
```

### Backend-Agnostic Code

Write once, run anywhere:

```kotlin
class DocumentRepository(private val fluxFS: FluxFS) {
    
    suspend fun saveDocument(id: String, content: String): FluxResult<Unit> {
        val path = "documents/$id.txt"
        return fluxFS.write(path, content.encodeToByteArray())
    }
    
    suspend fun loadDocument(id: String): FluxResult<String> {
        return fluxFS.read("documents/$id.txt")
            .map { it.decodeToString() }
    }
    
    suspend fun documentExists(id: String): Boolean {
        return fluxFS.fileExists("documents/$id.txt")
            .getOrElse { false }
    }
}

// Use with local filesystem
val localRepo = DocumentRepository(FluxFS.local())

// Or with S3 - same code!
val s3Repo = DocumentRepository(FluxFS(S3FilesystemAdapter(s3Client, "docs-bucket")))
```

### Spring Boot Integration

FluxFS provides auto-configuration for Spring Boot applications:

#### Configuration

```yaml
# application.yml
fluxfs:
  s3:
    bucket: my-application-bucket
    region: us-east-1
```

#### Usage

```kotlin
import com.vsmoraes.fluxfs.FluxFS
import org.springframework.stereotype.Service

@Service
class FileStorageService(
    private val fluxFS: FluxFS  // Auto-injected!
) {
    
    suspend fun uploadUserFile(userId: String, filename: String, content: ByteArray) {
        fluxFS.createDirectory("users/$userId", recursive = true)
        
        fluxFS.write("users/$userId/$filename", content)
            .onError { error -> 
                logger.error("Failed to upload file: $error")
            }
    }
    
    suspend fun getUserFile(userId: String, filename: String): ByteArray? {
        return fluxFS.read("users/$userId/$filename")
            .getOrNull()
    }
}
```

## 🔧 Advanced Usage

### Custom Error Handling

```kotlin
suspend fun safeRead(path: String): String? {
    return fluxFS.read(path).fold(
        onSuccess = { it.decodeToString() },
        onError = { error ->
            when (error) {
                is FluxResult.Error.FileNotFound -> {
                    logger.warn("File not found: ${error.path}")
                    null
                }
                is FluxResult.Error.IOError -> {
                    logger.error("IO Error: ${error.message}", error.cause)
                    null
                }
                else -> {
                    logger.error("Unexpected error: $error")
                    null
                }
            }
        }
    )
}
```

### Retry Logic

```kotlin
suspend fun readWithRetry(
    path: String, 
    maxAttempts: Int = 3
): FluxResult<ByteArray> {
    repeat(maxAttempts) { attempt ->
        val result = fluxFS.read(path)
        if (result.isSuccess()) return result
        
        if (attempt < maxAttempts - 1) {
            delay(100 * (attempt + 1))  // Exponential backoff
        }
    }
    return FluxResult.Error.IOError("Failed after $maxAttempts attempts")
}
```

### Building Custom Adapters

Implementing your own storage backend is straightforward:

```kotlin
class InMemoryFilesystemAdapter : FilesystemAdapter {
    private val storage = mutableMapOf<String, ByteArray>()
    private val directories = mutableSetOf<String>()
    
    override suspend fun read(fileName: FileName): FluxResult<ByteArray> {
        return storage[fileName]?.let { FluxResult.Success(it) }
            ?: FluxResult.Error.FileNotFound(fileName)
    }
    
    override suspend fun write(
        fileName: FileName, 
        content: ByteArray
    ): FluxResult<Unit> {
        if (storage.containsKey(fileName)) {
            return FluxResult.Error.FileAlreadyExists(fileName)
        }
        storage[fileName] = content
        return FluxResult.Success(Unit)
    }
    
    override suspend fun fileExists(fileName: FileName): FluxResult<Boolean> {
        return FluxResult.Success(storage.containsKey(fileName))
    }
    
    override suspend fun directoryExists(
        directoryName: DirectoryName
    ): FluxResult<Boolean> {
        return FluxResult.Success(directories.contains(directoryName))
    }
    
    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean
    ): FluxResult<Unit> {
        directories.add(directoryName)
        return FluxResult.Success(Unit)
    }
}
```

### Testing Your Code

Use FluxFS's test fixtures to verify your custom adapters:

```kotlin
import com.vsmoraes.fluxfs.FilesystemAdapterTestContract

class InMemoryFilesystemAdapterTest : FilesystemAdapterTestContract(
    name = "InMemory",
    createAdapter = { InMemoryFilesystemAdapter() },
    // ... implement test hooks
)
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :fluxfs-local:test

# Run integration tests with LocalStack (S3)
docker-compose up -d
./gradlew :fluxfs-s3:test
docker-compose down
```

### CI/CD

FluxFS includes comprehensive GitHub Actions workflows:

- **CI with Integration Tests** - Runs all tests including S3 integration tests against LocalStack
- **Dependabot Auto-Merge** - Automatically merges patch and minor dependency updates

## 📖 API Reference

### Core Operations

| Method | Parameters | Returns | Description |
|--------|-----------|---------|-------------|
| `read` | `fileName: FileName` | `FluxResult<ByteArray>` | Reads file contents into memory |
| `write` | `fileName: FileName`<br>`content: ByteArray` | `FluxResult<Unit>` | Writes content to a new file |
| `fileExists` | `fileName: FileName` | `FluxResult<Boolean>` | Checks if file exists |
| `directoryExists` | `directoryName: DirectoryName` | `FluxResult<Boolean>` | Checks if directory exists |
| `createDirectory` | `directoryName: DirectoryName`<br>`recursive: Boolean = true` | `FluxResult<Unit>` | Creates a directory |

### FluxResult Extensions

| Function | Description |
|----------|-------------|
| `onSuccess(action)` | Executes action if Success |
| `onError(action)` | Executes action if Error |
| `map(transform)` | Transforms Success value |
| `flatMap(transform)` | FlatMaps Success value |
| `getOrNull()` | Returns value or null |
| `getOrThrow()` | Returns value or throws |
| `getOrElse(default)` | Returns value or default |
| `isSuccess()` | Checks if Success |
| `isError()` | Checks if Error |
| `isTrue()` | For Boolean results |
| `isFalse()` | For Boolean results |

## 🗺️ Roadmap

- [x] Local filesystem adapter
- [x] AWS S3 adapter
- [x] Spring Boot integration
- [ ] In-memory adapter for testing
- [ ] Streaming support for large files
- [ ] Directory listing operations
- [ ] File metadata (size, timestamps, permissions)
- [ ] Kotlin Multiplatform support
- [ ] Google Cloud Storage adapter
- [ ] Azure Blob Storage adapter
- [ ] File watching/monitoring
- [ ] Batch operations
- [ ] Transaction support

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

```bash
# Clone the repository
git clone https://github.com/vsmoraes/fluxfs.git
cd fluxfs

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run with LocalStack for S3 testing
docker-compose up -d
./gradlew :fluxfs-s3:test
```

## 📄 License

FluxFS is released under the [MIT License](https://opensource.org/licenses/MIT).

```
MIT License

Copyright (c) 2024 Vinicius Moraes

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🙏 Acknowledgments

FluxFS was inspired by the need for a modern, Kotlin-idiomatic filesystem abstraction that embraces coroutines and explicit error handling.

---

**Made with ❤️ using Kotlin**

For questions, issues, or feature requests, please [open an issue](https://github.com/vsmoraes/fluxfs/issues) on GitHub.