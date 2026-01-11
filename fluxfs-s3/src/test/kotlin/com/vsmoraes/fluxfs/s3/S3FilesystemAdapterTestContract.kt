package com.vsmoraes.fluxfs.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectResponse
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Response
import aws.smithy.kotlin.runtime.content.ByteStream
import com.vsmoraes.fluxfs.FileName
import com.vsmoraes.fluxfs.FilesystemAdapterTestContract
import com.vsmoraes.fluxfs.PathNormalizer.ensureSuffix
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import aws.sdk.kotlin.services.s3.model.Object as S3Object

class S3FilesystemAdapterTestContract :
    FilesystemAdapterTestContract(
        name = "S3FilesystemAdapter",
        createAdapter = {
            setupListObjectsMock()
            S3FilesystemAdapter(s3Mock, BUCKET_NAME)
        },
        createValidFile = { fileName, fileContent ->
            validFiles[fileName] = fileContent
            setupListObjectsMock()
            setupGetObjectMock(fileName, fileContent)
        },
        createValidDirectory = { directoryName ->
            validFiles[directoryName.ensureSuffix("/")] = ""
            setupListObjectsMock()
        },
        mockReadException = {
            coEvery {
                s3Mock.getObject(
                    any<GetObjectRequest>(),
                    any<suspend (GetObjectResponse) -> ByteArray>(),
                )
            } coAnswers {
                throw Exception("File not found")
            }
        },
        mockDirectoryNotExist = { },
        beforeEach = {
            clearMocks(s3Mock)
            validFiles.clear()
        },
    ) {
    companion object {
        private const val BUCKET_NAME = "fluxfs-test"
        private val s3Mock = mockk<S3Client>(relaxed = true)
        private val validFiles = mutableMapOf<String, String>()

        private fun setupGetObjectMock(
            fileName: FileName,
            content: String,
        ) {
            coEvery {
                s3Mock.getObject(
                    any<GetObjectRequest>(),
                    any<suspend (GetObjectResponse) -> ByteArray>(),
                )
            } coAnswers {
                val request = firstArg<GetObjectRequest>()
                val block = secondArg<suspend (GetObjectResponse) -> ByteArray>()

                if (request.key == fileName && validFiles.containsKey(fileName)) {
                    val response =
                        GetObjectResponse {
                            body = ByteStream.fromBytes(content.toByteArray())
                        }

                    block(response)
                } else {
                    throw Exception("File not found: $fileName")
                }
            }
        }

        private fun setupListObjectsMock() {
            coEvery {
                s3Mock.listObjectsV2(any<ListObjectsV2Request>())
            } answers {
                val request = firstArg<ListObjectsV2Request>()
                val prefix = request.prefix ?: ""

                if (validFiles.containsKey(prefix)) {
                    ListObjectsV2Response {
                        contents =
                            listOf(
                                S3Object {
                                    key = prefix
                                    size = validFiles[prefix]!!.length.toLong()
                                },
                            )
                    }
                } else {
                    ListObjectsV2Response {
                        contents = emptyList()
                    }
                }
            }
        }
    }
}
