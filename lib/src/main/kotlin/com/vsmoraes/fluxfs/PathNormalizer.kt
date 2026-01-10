package com.vsmoraes.fluxfs

import kotlin.io.path.Path

fun String.parent() = Path(this).parent.toString()

fun String.ensureSuffix(suffix: String) = if (endsWith(suffix)) this else "$this$suffix"
