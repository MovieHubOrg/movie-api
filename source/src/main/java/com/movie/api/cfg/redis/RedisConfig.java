package com.movie.api.cfg.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@Slf4j
public class RedisConfig {
    @Value("#{'${redis.sentinel.hosts}'.split(',')}")
    private List<String> sentinelHosts;
    @Value("${redis.master.name}")
    private String masterName;
    @Value("${redis.host}")
    private String hostPort;
    @Value("${redis.type}")
    private int type;
    @Value("${redis.password}")
    private String password;

    /**
     * Jedis connection pool configuration.
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50); // Maximum total connections in the pool
        poolConfig.setMaxIdle(20); // Maximum idle connections kept in the pool
        poolConfig.setMinIdle(5); // Minimum idle connections maintained in the pool
        poolConfig.setMaxWaitMillis(3000); // Maximum wait time (ms) for a connection before timeout
        poolConfig.setTestOnBorrow(true); // Validate connection before borrowing from the pool
        poolConfig.setTestOnReturn(true); // Validate connection before returning to the pool
        poolConfig.setTestWhileIdle(true); // Periodically validate idle connections
        poolConfig.setTimeBetweenEvictionRunsMillis(30000); // Interval (ms) between idle connection eviction checks
        return poolConfig;
    }

    /**
     * Jedis client configuration with pooling and timeouts
     */
    @Bean
    public JedisClientConfiguration jedisClientConfiguration(
            JedisPoolConfig poolConfig) {

        return JedisClientConfiguration.builder()
                .usePooling()
                .poolConfig(poolConfig)
                .and()
                .readTimeout(Duration.ofMillis(2000))
                .connectTimeout(Duration.ofMillis(2000))
                .build();
    }

    /**
     * Primary Redis connection factory (Standalone or Sentinel).
     */
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig, JedisClientConfiguration jedisClientConfiguration) {
        JedisConnectionFactory factory;
        if (type == 1) {
            factory = createStandaloneConnection(jedisClientConfiguration);
            log.info("Redis Standalone connection configured");
        } else if (type == 2) {
            factory = createSentinelConnection(jedisClientConfiguration);
            log.info("Redis Sentinel connection configured");
        } else {
            throw new IllegalArgumentException("Unsupported Redis type: " + type);
        }

        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * Create Redis Standalone connection factory.
     */
    private JedisConnectionFactory createStandaloneConnection(JedisClientConfiguration clientConfig) {
        String[] hostAndPort = hostPort.split(":");
        if (hostAndPort.length != 2) {
            throw new IllegalArgumentException("Invalid Redis hostPort config: " + hostPort);
        }

        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);

        RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(host, port);
        standaloneConfig.setPassword(password);

        log.info("Redis Standalone configured at {}:{}", host, port);
        return new JedisConnectionFactory(standaloneConfig, clientConfig);
    }

    /**
     * Create Redis Sentinel connection factory.
     */
    private JedisConnectionFactory createSentinelConnection(JedisClientConfiguration clientConfig) {
        Set<String> sentinels = new HashSet<>(sentinelHosts);
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration(masterName, sentinels);
        sentinelConfig.setPassword(password);

        log.info("Redis Sentinel configured (master: {}, nodes: {})", masterName, sentinels);
        return new JedisConnectionFactory(sentinelConfig, clientConfig);
    }

    /**
     * RedisTemplate configuration.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);

        // Config serializers
        template.setDefaultSerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        // Only enable if Redis transactions are actually used
        template.setEnableTransactionSupport(false);

        template.afterPropertiesSet();

        log.info("RedisTemplate initialized");
        return template;
    }
}