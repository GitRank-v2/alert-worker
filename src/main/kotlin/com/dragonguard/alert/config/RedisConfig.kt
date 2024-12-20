package com.dragonguard.alert.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.core.task.VirtualThreadTaskExecutor
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(
        @Value("\${spring.data.redis.host}") redisHost: String,
        @Value("\${spring.data.redis.port}") redisPort: String,
        @Value("\${spring.data.redis.password}") redisPassword: String
    ): RedisConnectionFactory {
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration().apply {
                hostName = redisHost
                port = redisPort.toInt()
                password = RedisPassword.of(redisPassword)
            }
        )
    }

    @Bean
    fun redisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
        }
    }

    @Bean
    fun redisMessageListenerContainer(
        redisConnectionFactory: RedisConnectionFactory,
        virtualThreadTaskExecutor: TaskExecutor
    ): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(redisConnectionFactory)
            setTaskExecutor(virtualThreadTaskExecutor)
        }
    }

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {
        registerKotlinModule()
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    @Bean
    fun virtualThreadTaskExecutor(): TaskExecutor {
        return VirtualThreadTaskExecutor()
    }
}
