import com.vsmoraes.fluxfs.FilesystemAdapter
import com.vsmoraes.fluxfs.FluxFS
import com.vsmoraes.fluxfs.spring.FluxFSProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(FluxFSProperties::class)
class FluxFSAutoConfiguration(
    private val adapter: FilesystemAdapter,
) {
    @Bean
    @ConditionalOnMissingBean
    fun fluxFS() = FluxFS(adapter)
}
