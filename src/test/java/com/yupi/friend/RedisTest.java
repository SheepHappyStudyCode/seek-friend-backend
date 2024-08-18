package com.yupi.friend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testSimple() {
//        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
//        valueOperations.set("baidu", "www.jenkins_baidu.com");
//        User user = new User();
//        valueOperations.set("javaObject", user);
//        redisTemplate.opsForList().leftPush("javaList", user);
//        redisTemplate.opsForList().leftPush("javaList02", new ArrayList<User>());
//        List<Object> javaList02 = redisTemplate.opsForList().range("javaList02", 0, 1);

//        List<Integer> list = Arrays.asList(1,2,3,4,5);
//        redisTemplate.opsForList().rightPush("javaList03", list);
//        List<Object> javaList03 = redisTemplate.opsForList().range("javaList03", 0, 2);
//
//        for (Object o : javaList03) {
//            System.out.println(o);
//        }

//        List<Integer> list = Arrays.asList(1,2,3,4,5);
//        redisTemplate.opsForValue().set("javaList", list);
//        List<Integer> javaList = (List<Integer>) redisTemplate.opsForValue().get("javaList");
//        for (Integer integer : javaList) {
//            System.out.println(integer);
//        }

        List<String> list = Arrays.asList("a", "b", "c");
        stringRedisTemplate.opsForList().rightPushAll("list", list);
        List<String> range = stringRedisTemplate.opsForList().range("list", 1, 2);
        for (String s : range) {
            System.out.println(s);
        }

    }



    @Test
    void testRedis(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set("test", "123", 60, TimeUnit.SECONDS);

    }
}
