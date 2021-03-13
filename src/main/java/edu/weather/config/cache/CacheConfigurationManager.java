package edu.weather.config.cache;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;
import java.util.List;

/**
 * Configuration component that gathers defined cache configurations and creates caches based on them.
 *
 * @author andris
 * @since 1.0.0
 */
@org.springframework.context.annotation.Configuration
@EnableCaching
public class CacheConfigurationManager {

    @Bean
    public CacheManager ehCacheManager(List<Pair<String, Configuration>> cacheConfigs) {
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        cacheConfigs.forEach(cc -> cacheManager.createCache(cc.getKey(), cc.getValue()));

        return cacheManager;
    }
}
