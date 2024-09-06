package com.yupi.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.friend.model.dto.PostAddDTO;
import com.yupi.friend.model.dto.PostQueryDTO;
import com.yupi.friend.model.dto.PostUpdateDTO;
import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.PostVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【post(帖子)】的数据库操作Service
* @createDate 2024-08-27 21:58:05
*/
public interface PostService extends IService<Post> {

    Long addPost(PostAddDTO postAddDTO, User loginUser);

    boolean deletePostById(Long id, User loginUser);

    Boolean updatePost(PostUpdateDTO postUpdateDTO, User loginUser);

    List<PostVO> selectPosts(PostQueryDTO postQueryDTO);

    PostVO getPostVO(Post post);


}
