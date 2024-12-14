package com.yupi.friend.mq;

import com.rabbitmq.client.Channel;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.message.CacheUpdateMessage;
import com.yupi.friend.model.message.PostThumbUpdateMessage;
import com.yupi.friend.service.PostThumbService;
import com.yupi.friend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RabbitConstant.POST_THUMB_QUEUE;
import static com.yupi.friend.constant.RabbitConstant.USER_CACHE_QUEUE;
import static com.yupi.friend.constant.RedisConstant.POST_THUMB_MESSAGE_KEY;
import static com.yupi.friend.constant.RedisConstant.USER_RECOMMEND_KEY;

@Component
@Slf4j
public class MessageConsumer {
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

    @Resource
    private PostThumbService postThumbService;
    @RabbitListener(queues = USER_CACHE_QUEUE)
    public void updateRecommendUsers(@Payload CacheUpdateMessage message, Channel channel, org.springframework.amqp.core.Message msg) throws Exception {
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

    @RabbitListener(queues = POST_THUMB_QUEUE, concurrency = "1")
    public void updatePostThumb(@Payload PostThumbUpdateMessage message, Channel channel, org.springframework.amqp.core.Message msg) throws Exception {
        if(message == null){
            log.warn("消息为空");
            throw new BusinessException(ErrorCode.MQ_ERROR, "消息为空");
        }

        String messageId = message.getId();
        if(messageId == null){
            log.warn("消息 id 为空");
            throw new BusinessException(ErrorCode.MQ_ERROR, "消息 id 为空");
        }

        String key = POST_THUMB_MESSAGE_KEY + messageId;
        boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "0", 30, TimeUnit.SECONDS);
        if(flag){
            // 处理消息
            Integer ret;
            try{
                ret = postThumbService.updateThumbInDb(message.getPostId(), message.getUserId());
                log.info(ret.equals(0) ? "点赞成功" : "取消点赞");
                stringRedisTemplate.opsForValue().set(key, "1", 600, TimeUnit.SECONDS);
                // ack 操作一定要放在代码的最后
                channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
            }
            catch (IOException e){
                log.error("消息确认失败", e);
            }
            catch (Exception e){
                log.error("消息消费失败：", e);
            }
        }
        else{
            // 消息被别人消费
            if("1".equals(stringRedisTemplate.opsForValue().get(key))){
                try{
                    channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
                } catch (IOException e) {
                    log.error("消息确认失败：", e);
                }
            }
        }

    }

}