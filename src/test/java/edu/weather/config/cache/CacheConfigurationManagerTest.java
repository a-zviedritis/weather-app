package edu.weather.config.cache;

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

public class CacheConfigurationManagerTest {

    @Test
    public void testEhCacheManager() {
        String cacheName1 = "cache-1";
        String cacheName2 = "cache-2";
        CacheConfigurationManager configManager = new CacheConfigurationManager();

        CacheManager manager = configManager.ehCacheManager(
                Lists.newArrayList(
                        Pair.of(cacheName1, new MutableConfiguration()),
                        Pair.of(cacheName2, new MutableConfiguration())));

        assertThat(manager).isNotNull();
        assertThat(manager.getCacheNames()).containsExactlyInAnyOrder(cacheName1, cacheName2);
    }

}
