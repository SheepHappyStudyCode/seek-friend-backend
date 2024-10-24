package com.yupi.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.friend.model.entity.PostThumb;
import com.yupi.friend.model.entity.User;

/**
* @author Administrator
* @description 针对表【post_thumb(帖子点赞表)】的数据库操作Service
* @createDate 2024-09-05 16:09:57
*/
public interface PostThumbService extends IService<PostThumb> {

    Boolean updateThumb(Long id, User loginUser);

    boolean addThumb(long postId, long userId);

    boolean removeThumb(long postId, long userId);

    boolean updateAllThumbs();
}
