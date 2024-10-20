package com.yupi.friend.mq;

import com.yupi.friend.model.message.CacheUpdateMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.yupi.friend.constant.RabbitConstant.USER_CACHE_QUEUE;

@Service
public class CacheUpdateProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendCacheUpdateMessage(CacheUpdateMessage message) {
        rabbitTemplate.convertAndSend(USER_CACHE_QUEUE, message);
    }
}