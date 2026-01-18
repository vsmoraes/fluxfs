package com.vsmoraes.fluxfs.spring

import aws.sdk.kotlin.services.s3.S3Client
import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.s3.S3FilesystemAdapter
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(name = ["com.vsmoraes.fluxfs.s3.S3FilesystemAdapter"])
@ConditionalOnProperty(prefix = "fluxfs.s3", name = ["bucketName"])
@ConditionalOnBean(S3Client::class)
class S3FluxFSConfiguration(
    private val props: FluxFSProperties,
    private val s3ClientProvider: ObjectProvider<S3Client>,
) {
    @Bean
    fun s3Adapter(): FilesystemAdapter =
        S3FilesystemAdapter(
            s3Client =
                s3ClientProvider.getIfAvailable()
                    ?: error(
                        "FluxFS S3 is enabled but no S3Client bean was found. " +
                            "Please define a bean of type aws.sdk.kotlin.services.s3.S3Client.",
                    ),
            bucketName = props.s3.bucket,
        )
}
