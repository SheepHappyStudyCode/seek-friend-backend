package com.yupi.friend.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.mapper.PostMapper;
import com.yupi.friend.mapper.PostThumbMapper;
import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.PostThumb;
import com.yupi.friend.model.message.PostThumbUpdateMessage;
import com.yupi.friend.mq.MessageProducer;
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

import static com.yupi.friend.constant.RabbitConstant.POST_THUMB_QUEUE;
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

    @Resource
    private MessageProducer updateProducer;


    /**
     * 使用 lua 脚本更新 redis 的点赞信息
     * @param
     * @param
     * @return
     */
    @Override
    public Boolean updateThumbInRedis(Long postId, Long userId) {
        Long result = stringRedisTemplate.execute(UPDATE_SCRIPT, Collections.emptyList(), postId.toString(), userId.toString());
        return true;
    }

    /**
     * 点餐或取消点赞
     * @param postId
     * @param userId
     * @return 0-点赞成功  1-取消点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateThumbInDb(Long postId, Long userId) {
        if(postId == null || userId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        // 判断帖子是否存在
        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
        postQueryWrapper.eq("id", postId);
        Long count = postMapper.selectCount(postQueryWrapper);

        if(count == null || count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }

        PostThumb postThumb = this.query().eq("postId", postId).eq("userId", userId).select("id").one();
        if(postThumb == null){
            // 点赞
            // 帖子点赞数 +1
            UpdateWrapper<Post> postUpdateWrapper = new UpdateWrapper<>();
            postUpdateWrapper.setSql("likeCount = likeCount + 1");
            postUpdateWrapper.eq("id", postId);
            int update = postMapper.update(null, postUpdateWrapper);
            if(update != 1){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子点赞数更新失败");
            }

            // 更新点赞表
            postThumb = new PostThumb();
            postThumb.setUserId(userId);
            postThumb.setPostId(postId);
            boolean save = this.save(postThumb);
            if(!save){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "点赞记录增加失败");
            }
            return 0;
        }
        else{
            // 取消点赞
            // 帖子点赞数 -1
            UpdateWrapper<Post> postUpdateWrapper = new UpdateWrapper<>();
            postUpdateWrapper.setSql("likeCount = likeCount - 1");
            postUpdateWrapper.eq("id", postId);
            int update = postMapper.update(null, postUpdateWrapper);
            if(update != 1){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子点赞数更新失败");
            }

            // 更新点赞表
            boolean ret = this.removeById(postThumb.getId());
            if(!ret){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "点赞记录删除失败");
            }
            return 1;
        }

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

    /**
     * 通过向 mq 发送消息异步处理点赞请求
     * @param postId
     * @param userId
     * @return 0-成功
     */
    @Override
    public Integer updateThumb(Long postId, Long userId) {
        PostThumbUpdateMessage msg = new PostThumbUpdateMessage();
        // 全局唯一 id，避免重复消费
        msg.setId(UUID.randomUUID().toString(true));
        msg.setPostId(postId);
        msg.setUserId(userId);
        updateProducer.sendToQueue(msg, POST_THUMB_QUEUE);
        return 0;
    }
}




