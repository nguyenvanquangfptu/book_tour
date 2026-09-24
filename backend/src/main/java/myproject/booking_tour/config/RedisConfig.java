package myproject.booking_tour.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig implements org.springframework.cache.annotation.CachingConfigurer {

    /**
     * Redis hong thi bo qua cache, khong lam hong request.
     *
     * Cache o day chi la toi uu: tourOptions va popularDestinations deu tinh lai
     * duoc tu database. Nhung trinh xu ly loi mac dinh cua Spring NEM LAI moi
     * loi cache, nen khi Redis tat, khoi dong lai hay mat ket noi thi:
     *
     *   - GET /api/tours/options va /api/tours/popular-destinations tra 500 -
     *     bo loc o trang tour va muc diem den o trang chu trang tron;
     *   - createTour, updateTour, deleteTour, changeStatus, updateAccommodation
     *     deu mang @CacheEvict, nen moi thao tac quan tri tren tour cung bao 500
     *     du du lieu co the da duoc ghi xong.
     *
     * Gio loi cache chi ghi log WARN va phuong thuc chay nhu khong co cache. Doi
     * lai: mot lan xoa cache that bai vi Redis chap chon co the de lai du lieu cu
     * toi het TTL 1 gio.
     */
    @Override
    public org.springframework.cache.interceptor.CacheErrorHandler errorHandler() {
        return new org.springframework.cache.interceptor.LoggingCacheErrorHandler(RedisConfig.class.getName(), false);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // Mặc định thời gian sống của cache là 1 giờ
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
