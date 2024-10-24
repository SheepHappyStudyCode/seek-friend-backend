package com.yupi.friend.mq;

import com.rabbitmq.client.Channel;
import com.yupi.friend.model.message.CacheUpdateMessage;
import com.yupi.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RabbitConstant.USER_CACHE_QUEUE;
import static com.yupi.friend.constant.RedisConstant.USER_RECOMMEND_KEY;

@Component
@Slf4j
public class CacheUpdateConsumer {
    private static final DefaultRedisScript<Long> UPDATE_SCRIPT;
    static {
        UPDATE_SCRIPT = new DefaultRedisScript<>();
        UPDATE_SCRIPT.setLocation(new ClassPathResource("updateIds.lua"));
        UPDATE_SCRIPT.setResultType(Long.class);
    }
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;



    @RabbitListener(queues = USER_CACHE_QUEUE)
    public void receiveMessage(@Payload CacheUpdateMessage message, Channel channel, org.springframework.amqp.core.Message msg) throws Exception {
        try {
            // 更新缓存的逻辑
            updateCache(message.getKey(), message.getValue());
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 处理异常，可以选择重新入队或记录日志
            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
            log.error("用户推荐更新失败" + e);
        }
    }

    private void updateCache(String key, Object value) {
        // 实现具体的缓存更新逻辑
        if("recommend".equals(key)){
            System.out.println("Updating cache with key: " + key + " and value: " + value);
            List<Long> ids = userService.getRecommendUserIds((Long) value);

            String recommendKey = USER_RECOMMEND_KEY + value;

            // 使用 lua 脚本更新缓存
            // 定义 Lua 脚本

            List<String> idstrList = ids.stream().map(String::valueOf).collect(Collectors.toList());
            // 执行 Lua 脚本
            Long result = stringRedisTemplate.execute(UPDATE_SCRIPT, Arrays.asList(recommendKey), idstrList.toArray());

            System.out.println("Number of elements added: " + result);
        }

    }


}