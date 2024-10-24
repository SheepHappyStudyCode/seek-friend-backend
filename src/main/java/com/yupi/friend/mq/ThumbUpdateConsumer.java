package com.yupi.friend.mq;

import com.rabbitmq.client.Channel;
import com.yupi.friend.model.message.ThumbUpdateMessage;
import com.yupi.friend.service.PostThumbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;

import javax.annotation.Resource;

import static com.yupi.friend.constant.RabbitConstant.POST_THUMB_QUEUE;

//@Component
@Slf4j
public class ThumbUpdateConsumer {
    @Resource
    private PostThumbService postThumbService;

    @RabbitListener(queues = POST_THUMB_QUEUE)
    public void updateThumb(@Payload ThumbUpdateMessage message, Channel channel, org.springframework.amqp.core.Message msg) throws Exception {
        try {
            // 更新缓存的逻辑
            String key = message.getKey();
            long postId = message.getPostId();
            long userId = message.getUserId();

            if(key.equals("add")){
                postThumbService.addThumb(postId, userId);
            }
            else if(key.equals("remove")){
                postThumbService.removeThumb(postId, userId);
            }

            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 处理异常，可以选择重新入队或记录日志
            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, true);
            log.error("Failed to update thumb: " + e.getMessage());
        }
    }
}
