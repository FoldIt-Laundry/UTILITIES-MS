package com.foldit.utilites.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

    private final String redisPassword;
    private final String redisHost;
    private final String redisPort;

    @Autowired
    public RedisConfig(@Value("${spring.redis.host}") String redisHost, @Value("${spring.redis.password}") String redisPassword, @Value("${spring.redis.port}") String redisPort) {
        this.redisHost = redisHost;
        this.redisPassword = redisPassword;
        this.redisPort = redisPort;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        Jedis jedis = new Jedis(String.format("redis://default:%s@%s:%s", redisPassword, redisHost, redisPort));
        Connection connection = jedis.getConnection();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        return poolConfig;
    }

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig jedisPoolConfig) {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(redisHost);
        factory.setPort(Integer.parseInt(redisPort));
        factory.setPassword(redisPassword);

        return factory;
    }

    @Bean
    public RedisTemplate<String, String> deliveryRedisTemplate(JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        return template;
    }


}