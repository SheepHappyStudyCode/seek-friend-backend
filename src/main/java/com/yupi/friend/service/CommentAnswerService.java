package com.yupi.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.friend.model.dto.CommentAnswerAddDTO;
import com.yupi.friend.model.dto.CommentAnswerQueryDTO;
import com.yupi.friend.model.dto.CommentAnswerUpdateDTO;
import com.yupi.friend.model.entity.CommentAnswer;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.CommentAnswerVO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【comment_answer(评论回复表)】的数据库操作Service
* @createDate 2024-08-28 11:10:23
*/
public interface CommentAnswerService extends IService<CommentAnswer> {

    Long addCommentAnswer(CommentAnswerAddDTO commentAnswerAddDTO, User loginUser);

    boolean deleteCommentAnswerById(Long id, User loginUser);

    Boolean updateCommentAnswer(CommentAnswerUpdateDTO commentAnswerUpdateDTO, User loginUser);

    List<CommentAnswerVO> selectCommentAnswers(CommentAnswerQueryDTO commentAnswerQueryDTO);
}
