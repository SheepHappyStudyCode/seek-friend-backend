package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.mapper.PostThumbMapper;
import com.yupi.friend.model.entity.PostThumb;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.service.PostThumbService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
* @author Administrator
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service实现
* @createDate 2024-09-05 16:09:57
*/
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
    implements PostThumbService{

    @Resource
    private RedissonClient redisson;

    private static final String LOCK_KEY = "friend:post:thumb:";

    @Override
    public Boolean updateThumb(Long id, User loginUser) {
        // 获取锁对象实例（无法保证是按线程的顺序获取到）
        RLock lock = redisson.getLock(LOCK_KEY + loginUser.getId());

        try {
            // 尝试获取锁，最多等待 5s
            boolean res = lock.tryLock(0, 5000, TimeUnit.MILLISECONDS);
            if (res) {
                try {
                    // 执行需要加锁的逻辑
                    System.out.println("Lock acquired, executing critical section...");
                    // 模拟一些工作
                    long userId = loginUser.getId();
                    QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
                    postThumbQueryWrapper.eq("postId", id).eq("userId", userId);
                    if(count(postThumbQueryWrapper) > 0){
                        this.remove(postThumbQueryWrapper);
                    }
                    else{
                        PostThumb postThumb = new PostThumb();
                        postThumb.setPostId(id);
                        postThumb.setUserId(userId);
                        this.save(postThumb);
                    }

                    return true;

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

        return false;
    }
}




