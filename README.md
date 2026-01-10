# FluxFS

**FluxFS** is a Kotlin library that provides a unified interface for filesystem operations across different storage backends.

## 🚀 Features

- **Unified API**: Single `FilesystemAdapter` interface for all storage operations
- **Multiple Storage Backends**:
    - Local filesystem support via `LocalFilesystemAdapter`
    - AWS S3 support via `S3FilesystemAdapter`
- **Consistent Behavior**: All adapters follow the same behavioral contract
- **Type-Safe**: Leverages Kotlin's type system with custom exceptions
- **Lightweight**: Minimal abstraction layer with no unnecessary overhead

## 📦 Installation

### Gradle (Kotlin DSL)

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.vsmoraes:fluxfs:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'com.vsmoraes:fluxfs:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.vsmoraes</groupId>
    <artifactId>fluxfs</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🎯 Quick Start

### Basic Usage with Local Filesystem

```kotlin
import com.vsmoraes.fluxfs.FluxFS
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapter

// Initialize FluxFS with local filesystem
val fluxfs = FluxFS(LocalFilesystemAdapter())

// Write a file
val content = "Hello, FluxFS!".toByteArray()
fluxfs.write("example.txt", content)

// Read a file
val data = fluxfs.read("example.txt")
println(String(data)) // Output: Hello, FluxFS!

// Check if file exists
if (fluxfs.fileExists("example.txt")) {
    println("File exists!")
}

// Check if directory exists
if (fluxfs.directoryExists("/path/to/dir")) {
    println("Directory exists!")
}
```

### Using AWS S3 Filesystem

```kotlin
import aws.sdk.kotlin.services.s3.S3Client
import com.vsmoraes.fluxfs.FluxFS
import com.vsmoraes.fluxfs.s3.S3FilesystemAdapter

// Initialize S3 client
val s3Client = S3Client {
    region = "us-east-1"
}

// Initialize FluxFS with S3 adapter
val fluxfs = FluxFS(S3FilesystemAdapter(s3Client, "my-bucket"))

// Use the same API as local filesystem
fluxfs.write("remote-file.txt", "Hello from S3!".toByteArray())
val data = fluxfs.read("remote-file.txt")
```

### Creating Directories

```kotlin
// Create directory recursively (default)
fluxfs.createDirectory("path/to/nested/dir")

// Create directory non-recursively (parent must exist)
fluxfs.createDirectory("existing/parent/newdir", recursive = false)
```

## 🏗️ Architecture

FluxFS uses a simple adapter pattern:

```
FluxFS
├── FilesystemAdapter (Interface)
│   ├── read(file: String): ByteArray
│   ├── write(file: String, content: ByteArray)
│   ├── fileExists(file: String): Boolean
│   ├── directoryExists(path: String): Boolean
│   └── createDirectory(path: String, recursive: Boolean)
│
└── Implementations
    ├── LocalFilesystemAdapter
    └── S3FilesystemAdapter
```

## 📚 API Reference

### FilesystemAdapter Interface

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `read` | `file: String` | `ByteArray` | Reads entire file content into memory |
| `write` | `file: String, content: ByteArray` | `Unit` | Writes content to a new file |
| `fileExists` | `file: String` | `Boolean` | Checks if a regular file exists |
| `directoryExists` | `path: String` | `Boolean` | Checks if a directory exists |
| `createDirectory` | `path: String, recursive: Boolean = true` | `Unit` | Creates a directory |

## 🔧 Advanced Usage

### Implementing Custom Storage Backend

You can create your own storage backend by implementing the `FilesystemAdapter` interface:

```kotlin
import com.vsmoraes.fluxfs.FilesystemAdapter

class CustomStorageAdapter : FilesystemAdapter {
    override fun read(file: String): ByteArray {
        // Your custom read implementation
        TODO("Implement read")
    }
    
    override fun write(file: String, content: ByteArray) {
        // Your custom write implementation
        TODO("Implement write")
    }
    
    override fun fileExists(file: String): Boolean {
        // Your custom file existence check
        TODO("Implement fileExists")
    }
    
    override fun directoryExists(path: String): Boolean {
        // Your custom directory existence check
        TODO("Implement directoryExists")
    }
    
    override fun createDirectory(path: String, recursive: Boolean) {
        // Your custom directory creation
        TODO("Implement createDirectory")
    }
}

// Use it with FluxFS
val fluxfs = FluxFS(CustomStorageAdapter())
```

### Switching Between Backends

```kotlin
// Development environment - use local filesystem
val devAdapter = LocalFilesystemAdapter()
val devFluxFS = FluxFS(devAdapter)

// Production environment - use S3
val prodAdapter = S3FilesystemAdapter(s3Client, "production-bucket")
val prodFluxFS = FluxFS(prodAdapter)

// Your code uses the same API regardless of backend
fun processFile(fs: FluxFS, filename: String) {
    val content = fs.read(filename)
    // Process content...
}
```

## 🧪 Testing

Run tests with:

```bash
./gradlew test
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Setup

1. Clone the repository:
```bash
git clone https://github.com/vsmoraes/fluxfs.git
cd fluxfs
```

2. Build the project:
```bash
./gradlew build
```

3. Run tests:
```bash
./gradlew test
```

## 📋 Requirements

- Kotlin 1.9.0 or higher
- Java 11 or higher
- For S3 support: AWS SDK for Kotlin

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/vsmoraes/fluxfs/issues)
- **Discussions**: [GitHub Discussions](https://github.com/vsmoraes/fluxfs/discussions)

## 🗺️ Roadmap

- [ ] Add Azure Blob Storage backend
- [ ] Add Google Cloud Storage backend
- [ ] Add in-memory filesystem adapter for testing
- [ ] Add file metadata support (size, timestamps, permissions)
- [ ] Add streaming support for large files
- [ ] Add file copy/move operations
- [ ] Add directory listing functionality
- [ ] Kotlin Multiplatform support

---

**Made with ❤️ by [Vinicius Moraes](https://github.com/vsmoraes)**

If you find this project useful, please consider giving it a ⭐️ on GitHub!