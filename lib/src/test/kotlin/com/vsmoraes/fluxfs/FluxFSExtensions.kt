package com.vsmoraes.fluxfs

import io.kotest.matchers.types.shouldBeInstanceOf

object FluxFSExtensions {
    fun <T> FluxResult<T>.shouldBeSuccess(): T {
        this.shouldBeInstanceOf<FluxResult.Success<T>>()
        return this.value
    }
}
