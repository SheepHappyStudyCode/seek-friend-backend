package com.yupi.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.friend.model.dto.CommentAddDTO;
import com.yupi.friend.model.dto.CommentQueryDTO;
import com.yupi.friend.model.dto.CommentUpdateDTO;
import com.yupi.friend.model.entity.Comment;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.CommentVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【comment(评论)】的数据库操作Service
* @createDate 2024-08-28 10:10:27
*/
public interface CommentService extends IService<Comment> {

    Long addComment(CommentAddDTO commentAddDTO, User loginUser);

    boolean deleteCommentById(Long id, User loginUser);

    Boolean updateComment(CommentUpdateDTO commentUpdateDTO, User loginUser);

    List<CommentVO> selectComments(CommentQueryDTO commentQueryDTO);


}
