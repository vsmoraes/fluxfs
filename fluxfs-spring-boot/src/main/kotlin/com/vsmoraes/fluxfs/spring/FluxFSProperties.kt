package com.vsmoraes.fluxfs.spring

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("fluxfs")
data class FluxFSProperties(
    var s3: S3Properties,
) {
    data class S3Properties(
        var bucket: String,
        var region: String,
    )
}
