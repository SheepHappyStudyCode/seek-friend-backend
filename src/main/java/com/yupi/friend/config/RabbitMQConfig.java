package com.yupi.friend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.yupi.friend.constant.RabbitConstant.USER_CACHE_QUEUE;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue userCacheQueue() {
        return QueueBuilder.durable(USER_CACHE_QUEUE).build();
    }



}
