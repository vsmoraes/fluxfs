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
import com.vsmoraes.fluxfs.PathNormalizer.ensureSuffix
import com.vsmoraes.fluxfs.PathNormalizer.parent
import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.FileAlreadyExists
import com.vsmoraes.fluxfs.exception.FileNotFound
import com.vsmoraes.fluxfs.exception.WriteFileException

class S3FilesystemAdapter(
    private val s3Client: S3Client,
    private val bucketName: String,
) : FilesystemAdapter {
    override suspend fun read(fileName: FileName): ByteArray {
        if (!fileExists(fileName)) {
            throw FileNotFound(fileName)
        }

        return try {
            s3Client.getObject(
                GetObjectRequest {
                    bucket = bucketName
                    key = fileName
                },
            ) { response ->
                response.body?.toByteArray() ?: byteArrayOf()
            }
        } catch (e: Throwable) {
            throw WriteFileException("Error reading $fileName from S3", e)
        }
    }

    override suspend fun write(
        fileName: FileName,
        content: ByteArray,
    ) {
        if (!directoryExists(fileName.parent())) {
            throw DirectoryNotFound(fileName.parent())
        }

        if (fileExists(fileName)) {
            throw FileAlreadyExists(fileName)
        }

        try {
            s3Client.putObject {
                bucket = bucketName
                key = fileName
                body = ByteStream.fromBytes(content)
            }
        } catch (e: Throwable) {
            throw WriteFileException("Error writing file $fileName to S3", e)
        }
    }

    override suspend fun fileExists(fileName: FileName): Boolean =
        s3Client
            .listObjectsV2 {
                bucket = bucketName
                prefix = fileName
            }.contents
            ?.isNotEmpty() ?: false

    override suspend fun directoryExists(directoryName: DirectoryName): Boolean {
        val dir = directoryName.parent().ensureSuffix("/")

        return fileExists(dir)
    }

    override suspend fun createDirectory(
        directoryName: DirectoryName,
        recursive: Boolean,
    ) {
        if (directoryExists(directoryName)) {
            return
        }

        val dir = directoryName.ensureSuffix("/")

        try {
            s3Client.putObject {
                bucket = bucketName
                key = dir
                body = ByteStream.fromBytes(byteArrayOf())
            }
        } catch (e: Throwable) {
            throw WriteFileException("Error creating directory $dir in S3", e)
        }
    }
}
