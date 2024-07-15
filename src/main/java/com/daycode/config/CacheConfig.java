package com.daycode.config;

import lombok.RequiredArgsConstructor;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfig {

    private final ResourceLoader resourceLoader;

    public static final String TEMPERATURES_CACHE_NAME = "temperatures";

    /**
     * Spring cache manager.
     *
     * @return CacheManager for sprig cache.
     * @throws Exception in case of accessing ehcache configuration.
     */
    @Bean
    public org.springframework.cache.CacheManager cacheManager() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:ehcache.xml");
        URI uri = resource.getURI();
        CachingProvider provider = Caching.getCachingProvider(EhcacheCachingProvider.class.getName());
        javax.cache.CacheManager cacheManager = provider.getCacheManager(uri, getClass().getClassLoader());
        return new JCacheCacheManager(cacheManager);
    }
}
