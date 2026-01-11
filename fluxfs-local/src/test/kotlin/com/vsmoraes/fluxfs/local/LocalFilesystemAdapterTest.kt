package com.vsmoraes.fluxfs.local

import com.vsmoraes.fluxfs.FluxFSExtensions.shouldBeSuccess
import com.vsmoraes.fluxfs.FluxResult
import com.vsmoraes.fluxfs.isError
import com.vsmoraes.fluxfs.isSuccess
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
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
                val result = adapter.read(INVALID_FILE)

                result.isError() shouldBe true
                result.shouldBeTypeOf<FluxResult.Error.FileNotFound>()
            }

            test("should succeed when reading a file") {
                val tmpFile = Path(TMP_FILE)
                tmpFile.exists() shouldBe false

                tmpFile.writeText(FILE_CONTENT)

                val result = adapter.read(TMP_FILE)
                val value = result.shouldBeSuccess()
                value.decodeToString() shouldBe FILE_CONTENT
            }
        }

        context("write file") {
            test("should write content to a file") {
                Path(TMP_FILE).exists() shouldBe false

                val result = adapter.write(TMP_FILE, FILE_CONTENT.encodeToByteArray())
                result.isSuccess() shouldBe true

                val file = Path(TMP_FILE).readText()
                file shouldBe FILE_CONTENT
            }

            test("should fail when directory doesn't exit") {
                val result = adapter.write(TMP_FILE_INVALID_DIRECTORY, FILE_CONTENT.encodeToByteArray())
                result.isError() shouldBe true
                result.shouldBeTypeOf<FluxResult.Error.DirectoryNotFound>()
            }

            test("should fail when trying to write to a file that already exists") {
                adapter.write(TMP_FILE, FILE_CONTENT.encodeToByteArray())
                Path(TMP_FILE).exists() shouldBe true
                val result = adapter.write(TMP_FILE, FILE_CONTENT.encodeToByteArray())
                result.isError() shouldBe true
                result.shouldBeTypeOf<FluxResult.Error.FileAlreadyExists>()
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
