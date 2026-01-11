package com.vsmoraes.fluxfs

typealias FileName = String
typealias DirectoryName = String

class FluxFS(
    val filesystem: FilesystemAdapter,
) : FilesystemAdapter by filesystem
