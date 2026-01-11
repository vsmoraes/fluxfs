package com.vsmoraes.fluxfs

import com.vsmoraes.fluxfs.FluxFSExtensions.shouldBeSuccess
import com.vsmoraes.fluxfs.PathNormalizer.parent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

abstract class FilesystemAdapterTestContract(
    private val name: String,
    private val createAdapter: () -> FilesystemAdapter,
    private val createValidFile: (fileName: FileName, fileContent: String) -> Unit,
    private val createValidDirectory: (directoryName: DirectoryName) -> Unit,
    private val mockReadException: () -> Unit,
    private val mockDirectoryNotExist: (directory: DirectoryName) -> Unit,
    private val beforeEach: () -> Unit,
) : FunSpec({
        context("$name adapter") {
            lateinit var adapter: FilesystemAdapter

            beforeEach {
                beforeEach()
                adapter = createAdapter()
            }

            context("read file") {
                test("should fail if file doesn't exist") {
                    val result = adapter.read(INVALID_FILE)
                    result.isError() shouldBe true
                    result.shouldBeTypeOf<FluxResult.Error.FileNotFound>()
                }

                test("should fail if something went wrong") {
                    createValidFile(VALID_FILE, VALID_FILE_CONTENT)
                    mockReadException()

                    val result = adapter.read(VALID_FILE)
                    result.isError() shouldBe true
                    result.shouldBeTypeOf<FluxResult.Error.IOError>()
                }

                test("should return file content") {
                    createValidFile(VALID_FILE, VALID_FILE_CONTENT)
                    val result = adapter.read(VALID_FILE)
                    val value = result.shouldBeSuccess()
                    value.decodeToString() shouldBe VALID_FILE_CONTENT
                }
            }

            context("write file") {
                test("should fail when directory doesn't exit") {
                    mockDirectoryNotExist(VALID_FILE.parent())
                    val result = adapter.write(VALID_FILE, VALID_FILE_CONTENT.encodeToByteArray())
                    result.isError() shouldBe true
                    result.shouldBeTypeOf<FluxResult.Error.DirectoryNotFound>()
                }

                test("should fail when trying to write to a file that already exists") {
                    createValidDirectory(VALID_FILE.parent())
                    createValidFile(VALID_FILE, VALID_FILE_CONTENT)

                    val result = adapter.write(VALID_FILE, VALID_FILE_CONTENT.encodeToByteArray())
                    result.isError() shouldBe true
                    result.shouldBeTypeOf<FluxResult.Error.FileAlreadyExists>()
                }

                test("should write content to a file") {
                    createValidDirectory(VALID_FILE.parent())

                    val result = adapter.write(VALID_FILE, VALID_FILE_CONTENT.encodeToByteArray())
                    result.isSuccess() shouldBe true
                }
            }
        }
    }) {
    companion object {
        const val VALID_FILE = "/tmp/fluxfs/test.txt"
        const val VALID_FILE_CONTENT = "fluxfs-test\nfoo\nbar\nbaz\nbiz"
        const val INVALID_FILE = "/foo/bar/non-existent.txt"
    }
}
