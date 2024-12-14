package com.yupi.friend.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public <T> void sendToQueue(T message, String queueName) {
        rabbitTemplate.convertAndSend(queueName, message);
    }
}