package com.yupi.friend.utils;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Component
public class RedisCacheClient {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    public <T> void setJsonObject(String key, T value, Long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    public <T> T getJsonObject(String key, Class<T> clazz) {
        Object o = redisTemplate.opsForValue().get(key);
        if(o != null){
            return (T) o;
        }
        return null;
    }

    public <T> void setHashObject(String key, T value, Long time, TimeUnit unit) {
        Map<String, Object> objectMap = BeanUtil.beanToMap(value);
        redisTemplate.opsForHash().putAll(key, objectMap);
        redisTemplate.expire(key, time, unit);
    }

    public <T> void setHashObject(String key, T value) {
        Map<String, Object> objectMap = BeanUtil.beanToMap(value);
        redisTemplate.opsForHash().putAll(key, objectMap);
    }

    public <T> T getHashObject(String key, Class<T> clazz) {
        Map<Object, Object> objectMap = redisTemplate.opsForHash().entries(key);
        if (!objectMap.isEmpty()) {
            return BeanUtil.mapToBean(objectMap, clazz, false);
        }
        return null;
    }

    public <T> void multiSetHashObject(List<String> keyList, List<T> valueList) {
        List<Map<String, Object>> mapList = valueList.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
        RedisSerializer<String> keySerializer = (RedisSerializer<String>)redisTemplate.getKeySerializer();
        RedisSerializer<String> hashKeySerializer = (RedisSerializer<String>)redisTemplate.getHashKeySerializer();
        RedisSerializer<Object> hashValueSerializer = (RedisSerializer<Object>)redisTemplate.getHashValueSerializer();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (int i = 0; i < keyList.size(); i++) {
                String key = keyList.get(i);
                Map<String, Object> map = mapList.get(i);
                Map<byte[], byte[]> byteMap = map.entrySet().stream().collect(Collectors.toMap(entry -> hashKeySerializer.serialize(entry.getKey()), entry -> hashValueSerializer.serialize(entry.getValue())));
                connection.hashCommands().hMSet(keySerializer.serialize(key), byteMap);

            }

            return null;
        });
    }

    public <T> void multiSetHashObjectIfNotExist(List<String> keyList, List<T> valueList) {
        List<Map<String, Object>> mapList = valueList.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
        RedisSerializer<String> keySerializer = (RedisSerializer<String>)redisTemplate.getKeySerializer();
        RedisSerializer<String> hashKeySerializer = (RedisSerializer<String>)redisTemplate.getHashKeySerializer();
        RedisSerializer<Object> hashValueSerializer = (RedisSerializer<Object>)redisTemplate.getHashValueSerializer();

        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (int i = 0; i < keyList.size(); i++) {
                String key = keyList.get(i);
                byte[] serializeKey = keySerializer.serialize(key);
                if(Boolean.TRUE.equals(connection.exists(serializeKey))){
                    continue;
                }
                Map<String, Object> map = mapList.get(i);
                Map<byte[], byte[]> byteMap = map.entrySet().stream().collect(Collectors.toMap(entry -> hashKeySerializer.serialize(entry.getKey()), entry -> hashValueSerializer.serialize(entry.getValue())));
                connection.hashCommands().hMSet(serializeKey, byteMap);
            }

            return null;
        });
    }
    public <T> void multiSetHashObject(List<String> keyList, List<T> valueList, Long time, TimeUnit unit) {
        List<Map<String, Object>> mapList = valueList.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
        RedisSerializer<String> keySerializer = (RedisSerializer<String>)redisTemplate.getKeySerializer();
        RedisSerializer<String> hashKeySerializer = (RedisSerializer<String>)redisTemplate.getHashKeySerializer();
        RedisSerializer<Object> hashValueSerializer = (RedisSerializer<Object>)redisTemplate.getHashValueSerializer();

        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (int i = 0; i < keyList.size(); i++) {
                    String key = keyList.get(i);
                    Map<String, Object> map = mapList.get(i);
                    Map<byte[], byte[]> byteMap = map.entrySet().stream().collect(Collectors.toMap(entry -> hashKeySerializer.serialize(entry.getKey()), entry -> hashValueSerializer.serialize(entry.getValue())));
                    connection.hashCommands().hMSet(keySerializer.serialize(key), byteMap);
                    connection.expire(key.getBytes(), unit.toSeconds(time));
                }

                return null;
            }
        });
    }

    public <T> List<T> multiGetHashObject(List<String> keyList, Class<T> clazz) {
        List<Object> objects = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (String key : keyList) {
                    connection.hashCommands().hGetAll(key.getBytes());
                }
                return null;
            }
        });

        return objects.stream().filter(o ->{
            Map map = (Map) o;
            return !map.isEmpty();
        }).map(object -> {
            Map<Object, Object> objectMap = (Map<Object, Object>) object;
            if (!objectMap.isEmpty()) {
                return BeanUtil.mapToBean(objectMap, clazz, false);
            }
            return null;
        }).collect(Collectors.toList());
    }

    public <T> List<T> multiGetHashObject(List<String> keyList, Class<T> clazz, long time, TimeUnit unit) {
        List<Object> objects = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                for (String key : keyList) {
                    connection.hashCommands().hGetAll(key.getBytes());
                    connection.expire(key.getBytes(),unit.toSeconds(time));
                }
                return null;
            }
        });

        return objects.stream().filter(o -> !(o instanceof Boolean)).filter(o ->{
            Map map = (Map) o;
            return !map.isEmpty();
        }).map(object -> {
            Map<Object, Object> objectMap = (Map<Object, Object>) object;
            if (!objectMap.isEmpty()) {
                return BeanUtil.mapToBean(objectMap, clazz, false);
            }
            return null;
        }).collect(Collectors.toList());
    }

    public void expireKey(String key, Long time, TimeUnit unit) {
        redisTemplate.expire(key, time, unit);
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }


}
