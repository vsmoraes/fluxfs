package com.vsmoraes.fluxfs.spring

import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.local.LocalFilesystemAdapter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(name = ["com.vsmoraes.fluxfs.local.LocalFilesystemAdapter"])
class LocalFluxFSConfiguration {
    @Bean
    fun localAdapter(): FilesystemAdapter = LocalFilesystemAdapter()
}
