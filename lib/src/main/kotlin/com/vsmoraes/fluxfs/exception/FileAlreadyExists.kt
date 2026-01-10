package com.vsmoraes.fluxfs.exception

import java.io.IOException

class FileAlreadyExists(
    file: String,
) : IOException("$file already exists")
