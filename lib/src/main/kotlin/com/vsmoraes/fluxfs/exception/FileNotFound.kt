package com.vsmoraes.fluxfs.exception

import java.io.IOException

class FileNotFound(
    file: String,
) : IOException("$file not found")
