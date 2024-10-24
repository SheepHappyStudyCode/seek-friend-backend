package com.yupi.friend.job;
import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.PostThumb;
import com.yupi.friend.service.PostService;
import com.yupi.friend.service.PostThumbService;
import com.yupi.friend.utils.RedisCacheClient;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RedisConstant.POST_ID_KEY;
import static com.yupi.friend.constant.RedisConstant.POST_THUMB_IDS_KEY;

@Component
public class CacheLoader {

    @Resource
    private PostService postService;

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private RedisCacheClient redisCacheClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent() {
        // 加载数据到缓存
        List<Post> postList  = postService.list();
        List<String> keys = postList.stream().map(post -> POST_ID_KEY + post.getId()).collect(Collectors.toList());
        List<String> updateKeys = keys.stream().filter(key -> !Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))).collect(Collectors.toList());
        redisCacheClient.multiSetHashObjectIfNotExist(updateKeys, postList);

        for (Post post : postList) {
            Long postId = post.getId();
            List<PostThumb> postThumbList = postThumbService.query().eq("postId", postId).list();
            List<String> userIds = postThumbList.stream().map(postThumb -> postThumb.getUserId() + "").collect(Collectors.toList());

            String key = POST_THUMB_IDS_KEY + postId;
            if(stringRedisTemplate.hasKey(key) || userIds.isEmpty()){
                continue;
            }

            stringRedisTemplate.opsForSet().add(key, userIds.toArray(new String[0]));
        }
    }
}