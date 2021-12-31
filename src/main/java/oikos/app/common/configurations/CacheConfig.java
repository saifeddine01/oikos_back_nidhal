package oikos.app.common.configurations;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/** Created by Mohamed Haamdi on 23/04/2021. */
@EnableCaching
@Configuration
public class CacheConfig {
  @Bean
  public Caffeine<Object, Object> caffeineConfig() {
    return Caffeine.newBuilder().maximumSize(500).initialCapacity(100).expireAfterWrite(10, TimeUnit.MINUTES);
  }

  @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    var caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }
}
