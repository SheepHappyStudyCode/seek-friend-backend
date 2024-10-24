package com.yupi.friend;

import com.yupi.friend.model.message.CacheUpdateMessage;
import com.yupi.friend.mq.CacheUpdateProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RabbitTest {

    @Resource
    private CacheUpdateProducer cacheUpdateProducer;

    @Test
    public void testSend() {
        CacheUpdateMessage msg = new CacheUpdateMessage();
        msg.setKey("recommend");
        msg.setValue(1L);
    }

}
