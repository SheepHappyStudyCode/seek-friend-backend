package com.yupi.friend;

import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.PostThumb;
import com.yupi.friend.service.PostService;
import com.yupi.friend.service.PostThumbService;
import com.yupi.friend.utils.RedisCacheClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RedisConstant.POST_ID_KEY;
import static com.yupi.friend.constant.RedisConstant.POST_THUMB_IDS_KEY;

@SpringBootTest
public class PostTest {
    @Resource
    private PostService postService;

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private RedisCacheClient redisCacheClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void preCache(){
        List<Post> postList  = postService.list();
        List<String> keys = postList.stream().map(post -> POST_ID_KEY + post.getId()).collect(Collectors.toList());

        redisCacheClient.multiSetHashObject(keys, postList);

        for (Post post : postList) {
            Long postId = post.getId();
            
            List<PostThumb> postThumbList = postThumbService.query().eq("postId", postId).list();
            List<String> userIds = postThumbList.stream().map(postThumb -> postThumb.getUserId() + "").collect(Collectors.toList());
            if(userIds.isEmpty()){
                break;
            }

            String key = POST_THUMB_IDS_KEY + postId;
            stringRedisTemplate.opsForSet().add(key, userIds.toArray(new String[0]));
        }


    }
}
