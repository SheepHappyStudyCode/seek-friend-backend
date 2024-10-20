package com.yupi.friend;

import cn.hutool.core.bean.BeanUtil;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.UserService;
import com.yupi.friend.utils.RedisCacheClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yupi.friend.constant.RedisConstant.USER_ID_KEY;
import static com.yupi.friend.constant.RedisConstant.USER_RECOMMEND_KEY;

@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisCacheClient redisCacheClient;

    @Resource
    private UserService userService;

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
    void testRedisHash(){
        User user = userService.getById(2);
        System.out.println(user);
        Map<String, Object> map = BeanUtil.beanToMap(user);
        redisTemplate.opsForHash().putAll("user:2", map);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries("user:2");
        User newUser = BeanUtil.mapToBean(map, User.class, false);;

        System.out.println(newUser);

    }

    @Test
    void testRedisValue(){
        User user = userService.getById(2);
        System.out.println(user);

        redisCacheClient.setJsonObject("test", user, 10L, TimeUnit.MINUTES);

        User newUser = redisCacheClient.getJsonObject("test", User.class);
        System.out.println(newUser);

    }

    @Test
    void testMultiGet(){
        List<Long> list = Arrays.asList(2L, 3L, 4L);
        List<Object> objects = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (Long l : list) {
                    String key = USER_ID_KEY + l;
                    connection.hashCommands().hGetAll(key.getBytes());
                }

                return null;
            }
        });

        for (Object object : objects) {
//            Map<Object,Object> map = (Map) object;
            UserVO userVO = BeanUtil.mapToBean((Map) object, UserVO.class, false);
            System.out.println();
        }

    }

    @Test
    void testList(){
        String recommendKey = USER_RECOMMEND_KEY + 2;
        List<String> range = stringRedisTemplate.opsForList().range(recommendKey, 0, 9);
        System.out.println(range);
    }
}
