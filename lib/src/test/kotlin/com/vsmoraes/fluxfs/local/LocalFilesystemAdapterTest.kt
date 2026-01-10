package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.FileAlreadyExists
import com.vsmoraes.fluxfs.exception.FileNotFound
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class LocalFilesystemAdapterTest :
    FunSpec({
        val adapter = LocalFilesystemAdapter()

        afterEach {
            Path(TMP_FILE).deleteIfExists()
            Path(INVALID_FILE).deleteIfExists()
        }

        context("read file") {
            test("should fail when trying to read a non-existent file") {
                shouldThrow<FileNotFound> { adapter.read(INVALID_FILE) }
            }

            test("should succeed when reading a file") {
                val tmpFile = Path(TMP_FILE)
                tmpFile.exists() shouldBe false

                tmpFile.writeText(FILE_CONTENT)

                adapter.read(TMP_FILE).decodeToString() shouldBe FILE_CONTENT
            }
        }

        context("write file") {
            test("should write content to a file") {
                File(TMP_FILE).exists() shouldBe false

                adapter.write(TMP_FILE, FILE_CONTENT.encodeToByteArray())

                val result = Path(TMP_FILE).readText()
                result shouldBe FILE_CONTENT
            }

            test("should fail when directory doesn't exit") {
                shouldThrow<DirectoryNotFound> {
                    adapter.write(TMP_FILE_INVALID_DIRECTORY, FILE_CONTENT.encodeToByteArray())
                }
            }

            test("should fail when trying to write to a file that already exists") {
                adapter.write(TMP_FILE, FILE_CONTENT.encodeToByteArray())
                Path(TMP_FILE).exists() shouldBe true
                shouldThrow<FileAlreadyExists> {
                    adapter.write(TMP_FILE, FILE_CONTENT.encodeToByteArray())
                }
            }
        }
    }) {
    companion object {
        const val TMP_FILE = "/tmp/local-adapter-fluxfs-test.txt"
        const val TMP_FILE_INVALID_DIRECTORY = "/tmp/invalid/directory/local-adapter-fluxfs-test.txt"
        const val INVALID_FILE = "/tmp/non-invalid-file.txt"
        const val FILE_CONTENT = "foo bar baz biz"
    }
}
