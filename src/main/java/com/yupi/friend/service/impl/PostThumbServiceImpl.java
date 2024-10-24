package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.mapper.PostMapper;
import com.yupi.friend.mapper.PostThumbMapper;
import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.PostThumb;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.service.PostThumbService;
import com.yupi.friend.utils.RedisCacheClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RedisConstant.POST_ID_KEY;
import static com.yupi.friend.constant.RedisConstant.POST_THUMB_IDS_KEY;

/**
* @author Administrator
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service实现
* @createDate 2024-09-05 16:09:57
*/
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
    implements PostThumbService{

    private static final DefaultRedisScript<Long> UPDATE_SCRIPT;
    static {
        UPDATE_SCRIPT = new DefaultRedisScript<>();
        UPDATE_SCRIPT.setLocation(new ClassPathResource("updatePostThumb.lua"));
        UPDATE_SCRIPT.setResultType(Long.class);
    }
    @Resource
    private RedisCacheClient redisCacheClient;

    @Resource
    private PostMapper postMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public Boolean updateThumb(Long id, User loginUser) {

        Long result = stringRedisTemplate.execute(UPDATE_SCRIPT, Collections.emptyList(), id.toString(), loginUser.getId().toString());

        return true;
    }

    @Override
    @Transactional
    public boolean addThumb(long postId, long userId) {
        Long count = this.query().eq("postId", postId).eq("userId", userId).count();
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点赞记录已经存在");
        }

        PostThumb postThumb = new PostThumb();
        postThumb.setUserId(userId);
        postThumb.setPostId(postId);

        boolean save = this.save(postThumb);
        if(!save){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        UpdateWrapper<Post> postUpdateWrapper = new UpdateWrapper<>();
        postUpdateWrapper.setSql("likeCount = likeCount + 1");
        postUpdateWrapper.eq("id", postId);
        int update = postMapper.update(null, postUpdateWrapper);
        if(update != 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean removeThumb(long postId, long userId) {
        Long count = this.query().eq("postId", postId).eq("userId", userId).count();
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "点赞记录不存在，无法删除");
        }

        QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
        postThumbQueryWrapper.eq("postId", postId).eq("userId", userId);

        boolean remove = this.remove(postThumbQueryWrapper);
        if(!remove){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UpdateWrapper<Post> postUpdateWrapper = new UpdateWrapper<>();
        postUpdateWrapper.setSql("likeCount = likeCount - 1");
        postUpdateWrapper.eq("id", postId);
        int update = postMapper.update(null, postUpdateWrapper);
        if(update != 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateAllThumbs() {
        List<Post> posts = postMapper.selectList(null);
        List<Long> ids = posts.stream().map(Post::getId).collect(Collectors.toList());
        // 更新帖子
        List<String> keyList = ids.stream().map(id -> POST_ID_KEY + id).collect(Collectors.toList());
        List<Post> newPosts = redisCacheClient.multiGetHashObject(keyList, Post.class);
        for (Post newPost : newPosts) {
            postMapper.updateById(newPost);
        }


        // 更新点赞记录
        keyList = ids.stream().map(id -> POST_THUMB_IDS_KEY + id).collect(Collectors.toList());
        for (int i = 0; i < ids.size(); i++) {
            String key = keyList.get(i);
            long postId = ids.get(i);
            Set<String> idstrSet = stringRedisTemplate.opsForSet().members(key);
            if(idstrSet == null){
                break;
            }

            Set<Long> userIds = idstrSet.stream().map(Long::parseLong).collect(Collectors.toSet());

            List<PostThumb> postThumbList = this.query().eq("postId", postId).list();

            Set<Long> oldUserIds = postThumbList.stream().map(PostThumb::getUserId).collect(Collectors.toSet());

            Set<Long> deleteIds = oldUserIds.stream().filter(id -> !userIds.contains(id)).collect(Collectors.toSet());

            Set<Long> insertIds = userIds.stream().filter(id -> !oldUserIds.contains(id)).collect(Collectors.toSet());

            if(!deleteIds.isEmpty()){
                QueryWrapper<PostThumb> wrapper = new QueryWrapper<>();
                wrapper.eq("postId", postId).in("userId", deleteIds);
                this.remove(wrapper);
            }

            if(!insertIds.isEmpty()){
                List<PostThumb> postThumbs = insertIds.stream().map(userId -> {
                    PostThumb postThumb = new PostThumb();
                    postThumb.setUserId(userId);
                    postThumb.setPostId(postId);
                    return postThumb;
                }).collect(Collectors.toList());
                this.saveBatch(postThumbs);
            }

        }

        return true;
    }
}




