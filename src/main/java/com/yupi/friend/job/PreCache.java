package com.yupi.friend.job;

import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.UserService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PreCache {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redisson;


    private List<Long> keyUserIds = Arrays.asList(1l, 2l);

    private static final String LOCK_KEY = "friend:job:precache:docache";
    private static final long LOCK_EXPIRE_TIME = 30000; // 锁的过期时间，单位为毫秒

    // 每天12小时执行
    @Scheduled(cron = "0 0 12 * * ? ")
    void preCacheRecommendUsers(){

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();

        // 获取锁对象实例（无法保证是按线程的顺序获取到）
        RLock lock = redisson.getLock(LOCK_KEY);

        try {
            // 尝试获取锁，最多等待100秒，上锁以后10秒自动解锁
            ;boolean res = lock.tryLock(0, LOCK_EXPIRE_TIME, TimeUnit.MILLISECONDS);
            if (res) {
                try {
                    // 执行需要加锁的逻辑
                    System.out.println("Lock acquired, executing critical section...");
                    // 模拟一些工作
                    for(Long id : keyUserIds){
                        String redisKey = String.format("friend:user:recommend:%s", id);
                        User user = new User();
                        user.setId(id);
                        List<UserVO> userVOList = userService.recommendUsers(10, user);
                        valueOperations.set(redisKey, userVOList, 1, TimeUnit.HOURS);
                    }
                } finally {
                    // 释放锁，只能释放自己的锁
                    if (lock.isHeldByCurrentThread()) {
                        System.out.println("unLock" + Thread.currentThread().getId());
                        lock.unlock();
                    }
                }
            } else {
                System.out.println("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
