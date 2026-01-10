package com.vsmoraes.fluxfs.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.listObjectsV2
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.putObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray
import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.ensureSuffix
import com.vsmoraes.fluxfs.exception.DirectoryNotFound
import com.vsmoraes.fluxfs.exception.FileAlreadyExists
import com.vsmoraes.fluxfs.exception.FileNotFound
import com.vsmoraes.fluxfs.exception.WriteFileException
import com.vsmoraes.fluxfs.parent
import kotlinx.coroutines.runBlocking

class S3FilesystemAdapter(
    private val s3Client: S3Client,
    private val bucketName: String,
) : FilesystemAdapter {
    override fun read(file: String): ByteArray {
        if (fileExists(file).not()) {
            throw FileNotFound(file)
        }

        return try {
            runBlocking {
                s3Client.getObject(
                    GetObjectRequest {
                        bucket = bucketName
                        key = file
                    },
                ) { response ->
                    response.body?.toByteArray() ?: byteArrayOf()
                }
            }
        } catch (e: Throwable) {
            throw WriteFileException("Error reading $file from S3", e)
        }
    }

    override fun write(
        file: String,
        content: ByteArray,
    ) {
        if (directoryExists(file.parent()).not()) {
            throw DirectoryNotFound(file.parent())
        }

        if (fileExists(file)) {
            throw FileAlreadyExists(file)
        }

        try {
            runBlocking {
                s3Client.putObject {
                    bucket = bucketName
                    key = file
                    body = ByteStream.fromBytes(content)
                }
            }
        } catch (e: Throwable) {
            throw WriteFileException("Error writing file $file to S3", e)
        }
    }

    override fun fileExists(file: String): Boolean =
        runBlocking {
            val result =
                s3Client.listObjectsV2 {
                    bucket = bucketName
                    prefix = file
                }

            result.contents?.isNotEmpty() ?: false
        }

    override fun directoryExists(path: String): Boolean {
        val dir = path.parent().ensureSuffix("/")

        return fileExists(dir)
    }

    override fun createDirectory(
        path: String,
        recursive: Boolean,
    ) {
        if (directoryExists(path)) {
            return
        }

        val dir = path.ensureSuffix("/")

        try {
            runBlocking {
                s3Client.putObject {
                    bucket = bucketName
                    key = dir
                    body = ByteStream.fromBytes(byteArrayOf())
                }
            }
        } catch (e: Throwable) {
            throw WriteFileException("Error creating directory $dir in S3", e)
        }
    }
}
