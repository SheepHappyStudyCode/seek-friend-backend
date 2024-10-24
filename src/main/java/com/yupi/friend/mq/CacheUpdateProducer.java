package com.yupi.friend.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class CacheUpdateProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public <T> void sendCacheUpdateMessage(T message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}