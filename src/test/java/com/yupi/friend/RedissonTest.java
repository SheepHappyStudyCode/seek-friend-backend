package com.yupi.friend;

import com.yupi.friend.service.UserService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redisson;


    private List<Long> keyUserIds = Arrays.asList(1l, 2l);

    private static final String LOCK_KEY = "friend:job:precache:docache";
    private static final long LOCK_EXPIRE_TIME = 30000; // 锁的过期时间，单位为毫秒


    @Test
    void testRedisson(){
        RList<String> list = redisson.getList("test-list");

        list.add("data01");
        list.add("data02");
        list.add("data03");
        list.remove(0);
    }

    @Test
    void testWatchDog(){
        RLock lock = redisson.getLock(LOCK_KEY);

        try {

            boolean res = lock.tryLock(0, -1, TimeUnit.MILLISECONDS);
            if (res) {
                try {
                    // 执行需要加锁的逻辑
                    System.out.println("Lock acquired, executing critical section...");
                    // 模拟一些工作
                    Thread.sleep(20000);
                } finally {
                    // 释放锁
                    lock.unlock();
                    System.out.println("Lock released");
                }
            } else {
                System.out.println("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            redisson.shutdown();
        }

    }
}
