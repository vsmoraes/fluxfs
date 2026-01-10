package com.vsmoraes.fluxfs

class FluxFS(
    val filesystem: FilesystemAdapter,
) : FilesystemAdapter by filesystem
