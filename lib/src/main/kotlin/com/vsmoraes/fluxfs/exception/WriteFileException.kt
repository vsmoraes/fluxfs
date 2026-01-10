package com.vsmoraes.fluxfs.exception

import aws.smithy.kotlin.runtime.io.IOException

class WriteFileException(
    override val message: String?,
    override val cause: Throwable?,
) : IOException(message, cause)
