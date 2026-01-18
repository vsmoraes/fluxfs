package com.vsmoraes.fluxfs.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray
import com.vsmoraes.fluxfs.DirectoryName
import com.vsmoraes.fluxfs.FileName
import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.FluxResult
import com.vsmoraes.fluxfs.FluxResult.Error.DirectoryAlreadyExists
import com.vsmoraes.fluxfs.FluxResult.Error.DirectoryNotFound
import com.vsmoraes.fluxfs.FluxResult.Error.FileAlreadyExists
import com.vsmoraes.fluxfs.FluxResult.Error.FileNotFound
import com.vsmoraes.fluxfs.FluxResult.Error.IOError
import com.vsmoraes.fluxfs.PathNormalizer.ensureSuffix
import com.vsmoraes.fluxfs.PathNormalizer.parent
import com.vsmoraes.fluxfs.isFalse
import com.vsmoraes.fluxfs.isTrue

/**
 * AWS S3 filesystem adapter implementation for FluxFS.
 *
 * This adapter treats S3 objects as files and simulates directory structures
 * using S3 prefixes and special directory marker objects (keys ending with /).
 *
 * @param s3Client The AWS S3 client to use for operations
 * @param bucketName The S3 bucket name where files will be stored
 */
class S3FilesystemAdapter(
    private val s3Client: S3Client,
    private val bucketName: String,
) : FilesystemAdapter {
    override suspend fun read(fileName: FileName): FluxResult<ByteArray> {
        if (fileExists(fileName).isFalse()) {
            return FileNotFound(fileName)
        }

        return runCatching {
            s3Client.getObject(
                GetObjectRequest {
                    bucket = bucketName
                    key = fileName
                },
            ) { response ->
                response.body?.toByteArray() ?: byteArrayOf()
            }
        }.fold(
            onSuccess = { FluxResult.Success(it) },
            onFailure = { IOError("Error reading $fileName from S3", it) },
        )
    }

    override suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ): FluxResult<Unit> {
        val parentDir = fileName.parent()

        if (directoryExists(parentDir).isFalse()) {
            return DirectoryNotFound(parentDir)
        }

        if (fileExists(fileName).isTrue()) {
            return FileAlreadyExists(fileName)
        }

        return runCatching {
            s3Client.putObject {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(content)
            }
        }.fold(
            onSuccess = { FluxResult.Success(Unit) },
            onFailure = { IOError("Error writing $fileName to S3", it) },
        )
    }

    override suspend fun fileExists(fileName: FileName) =
        runCatching {
            val objects =
                s3Client.listObjectsV2 {
                    bucket = bucketName
                    prefix = fileName
                    maxKeys = 1
                }

            objects.contents?.any { it.key == fileName } ?: false
        }.fold(
            onSuccess = { FluxResult.Success(it) },
            onFailure = { IOError("Error checking if file exists: $fileName", it) },
        )

    override suspend fun directoryExists(directoryName: DirectoryName) = fileExists(directoryName.ensureSuffix("/"))

    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean,
    ): FluxResult<Unit> {
        val dir = directoryName.ensureSuffix("/")

        if (directoryExists(directoryName).isTrue()) {
            return DirectoryAlreadyExists(directoryName)
        }

        return runCatching {
            s3Client.putObject {
                bucket = bucketName
                key = dir
                body = ByteStream.fromBytes(byteArrayOf())
            }
        }.fold(
            onSuccess = { FluxResult.Success(Unit) },
            onFailure = { IOError("Error creating directory $directoryName in S3", it) },
        )
    }
}
