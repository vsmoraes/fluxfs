package com.vsmoraes.fluxfs.exception

import aws.smithy.kotlin.runtime.io.IOException

class ReadFileException(
    override val message: String?,
    override val cause: Throwable?,
) : IOException(message, cause)
