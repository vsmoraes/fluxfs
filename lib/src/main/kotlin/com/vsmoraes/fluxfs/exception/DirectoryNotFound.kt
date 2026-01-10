package com.vsmoraes.fluxfs.exception

import java.io.IOException

class DirectoryNotFound(
    directory: String,
) : IOException("Directory $directory not found")
