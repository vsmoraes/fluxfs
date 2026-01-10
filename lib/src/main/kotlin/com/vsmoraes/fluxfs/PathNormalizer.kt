package com.vsmoraes.fluxfs

import kotlin.io.path.Path

object PathNormalizer {
    fun FileName.parent() = Path(this).parent.toString()

    fun FileName.ensureSuffix(suffix: String) = if (endsWith(suffix)) this else "$this$suffix"
}
