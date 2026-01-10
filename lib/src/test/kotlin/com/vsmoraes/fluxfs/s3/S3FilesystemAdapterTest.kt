package com.vsmoraes.fluxfs.s3

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.createBucket
import aws.sdk.kotlin.services.s3.deleteBucket
import aws.sdk.kotlin.services.s3.deleteObject
import aws.sdk.kotlin.services.s3.listBuckets
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.decodeToString
import aws.smithy.kotlin.runtime.net.url.Url
import com.vsmoraes.fluxfs.ensureSuffix
import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.FileAlreadyExists
import com.vsmoraes.fluxfs.exception.FileNotFound
import com.vsmoraes.fluxfs.parent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class S3FilesystemAdapterTest :
    FunSpec({
        val s3Client =
            S3Client {
                region = AWS_REGION
                endpointUrl = Url.parse(AWS_ENDPOINT)

                credentialsProvider =
                    StaticCredentialsProvider(
                        Credentials(
                            accessKeyId = AWS_ACCESS_KEY_ID,
                            secretAccessKey = AWS_SECRET_ACCESS_KEY,
                        ),
                    )

                forcePathStyle = true // Important for LocalStack
            }
        val adapter = S3FilesystemAdapter(s3Client, BUCKET_NAME)

        suspend fun writeTmpFile() =
            s3Client.putObject {
                bucket = BUCKET_NAME
                key = TMP_OBJECT_KEY
                body = ByteStream.fromBytes(TMP_FILE_CONTENT.toByteArray())
            }

        suspend fun maybeDeleteBucket() {
            val buckets =
                s3Client.listBuckets {
                    prefix = BUCKET_NAME
                }

            if (buckets.buckets.isNullOrEmpty()) return

            s3Client
                .listObjectsV2 {
                    bucket = BUCKET_NAME
                }.contents
                ?.forEach { obj ->
                    s3Client.deleteObject {
                        bucket = BUCKET_NAME
                        key = obj.key
                    }
                }

            s3Client.deleteBucket { bucket = BUCKET_NAME }
        }

        suspend fun createDirectory() {
            s3Client.putObject {
                bucket = BUCKET_NAME
                key = TMP_OBJECT_KEY.parent().ensureSuffix("/")
                body = ByteStream.fromBytes(byteArrayOf())
            }
        }

        beforeEach {
            maybeDeleteBucket()
            s3Client.createBucket { bucket = BUCKET_NAME }
        }

        context("read file") {
            test("should succeed when reading a file") {
                writeTmpFile()
                val result = adapter.read(TMP_OBJECT_KEY)

                result.decodeToString() shouldBe TMP_FILE_CONTENT
            }

            test("should fail when trying to read a non-existent file") {
                shouldThrow<FileNotFound> {
                    adapter.read(INVALID_OBJECT_KEY)
                }
            }
        }

        context("write file") {
            test("should fail to write a file if directory doesn't exist") {
                shouldThrow<DirectoryNotFound> {
                    adapter.write(TMP_OBJECT_KEY, TMP_FILE_CONTENT.toByteArray())
                }
            }

            test("should fail to write a file that already exists") {
                createDirectory()
                writeTmpFile()
                shouldThrow<FileAlreadyExists> {
                    adapter.write(TMP_OBJECT_KEY, TMP_FILE_CONTENT.toByteArray())
                }
            }

            test("should successfully write a file") {
                createDirectory()
                adapter.write(TMP_OBJECT_KEY, TMP_FILE_CONTENT.toByteArray())

                val result =
                    s3Client.getObject(
                        GetObjectRequest {
                            bucket = BUCKET_NAME
                            key = TMP_OBJECT_KEY
                        },
                    ) { response ->
                        response.body?.decodeToString() ?: ""
                    }

                result shouldBe TMP_FILE_CONTENT
            }
        }
    }) {
    companion object {
        const val AWS_REGION = "us-east-1"
        const val AWS_ENDPOINT = "http://localhost:4566"
        const val AWS_ACCESS_KEY_ID = "test"
        const val AWS_SECRET_ACCESS_KEY = "test"
        const val BUCKET_NAME = "fluxfs-test"
        const val TMP_OBJECT_KEY = "/tmp/test.txt"
        const val INVALID_OBJECT_KEY = "/non/existent/test.txt"
        const val TMP_FILE_CONTENT = "fluxfs-s3-test"
    }
}
