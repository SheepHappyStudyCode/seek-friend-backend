package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.constant.UserConstant;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.mapper.CommentAnswerMapper;
import com.yupi.friend.mapper.CommentMapper;
import com.yupi.friend.mapper.UserMapper;
import com.yupi.friend.model.dto.CommentAnswerAddDTO;
import com.yupi.friend.model.dto.CommentAnswerQueryDTO;
import com.yupi.friend.model.dto.CommentAnswerUpdateDTO;
import com.yupi.friend.model.entity.Comment;
import com.yupi.friend.model.entity.CommentAnswer;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.CommentAnswerVO;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.CommentAnswerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【comment_answer(评论回复表)】的数据库操作Service实现
* @createDate 2024-08-28 11:10:23
*/
@Service
public class CommentAnswerServiceImpl extends ServiceImpl<CommentAnswerMapper, CommentAnswer>
    implements CommentAnswerService {
    
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Long addCommentAnswer(CommentAnswerAddDTO commentAnswerAddDTO, User loginUser) {
        if (commentAnswerAddDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long commentId = commentAnswerAddDTO.getCommentId();
        String content = commentAnswerAddDTO.getContent();
        Integer type = commentAnswerAddDTO.getType();
        Long toAnswerId = commentAnswerAddDTO.getToAnswerId();

        if (StringUtils.isBlank(content) || content.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复内容不能为空且不能超过512字");
        }

        Comment comment = commentMapper.selectById(commentId);
        
        if (comment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }

        CommentAnswer commentAnswer = new CommentAnswer();
        BeanUtils.copyProperties(commentAnswerAddDTO, commentAnswer);
        commentAnswer.setCreateUserId(loginUser.getId());
        boolean save = this.save(commentAnswer);

        if(!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return commentAnswer.getId();
    }

    @Override
    public boolean deleteCommentAnswerById(Long id, User loginUser) {
        if(id == null || id < 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复不存在");

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        CommentAnswer commentAnswer = this.getById(id);
        if(commentAnswer == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复不存在");
        }

        if(!loginUser.getId().equals(commentAnswer.getCreateUserId()) && loginUser.getId() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return this.removeById(id);
    }

    @Override
    public Boolean updateCommentAnswer(CommentAnswerUpdateDTO commentAnswerUpdateDTO, User loginUser) {
        if(commentAnswerUpdateDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long id = commentAnswerUpdateDTO.getId();
        String content = commentAnswerUpdateDTO.getContent();
        Integer hot = commentAnswerUpdateDTO.getHot();
        

        CommentAnswer commentAnswer = this.getById(id);
        if(commentAnswer == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "回复不存在");
        }

        if(!loginUser.getId().equals(commentAnswer.getCreateUserId()) && loginUser.getId() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }


        return this.update().set(StringUtils.isNotBlank(content), "content", content)
                .set(hot != null, "category", hot)
                .eq("id", id).update();
    }

    @Override
    public List<CommentAnswerVO> selectCommentAnswers(CommentAnswerQueryDTO commentAnswerQueryDTO) {
        if(commentAnswerQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        Long id = commentAnswerQueryDTO.getId();
        Long commentId = commentAnswerQueryDTO.getCommentId();
        Long createUserId = commentAnswerQueryDTO.getCreateUserId();



        QueryWrapper<CommentAnswer> commentAnswerQueryWrapper = new QueryWrapper<>();

        commentAnswerQueryWrapper.eq(id != null, "id", id)
                .eq(commentId != null, "commentId", commentId)
                .eq(createUserId != null, "createUserId", createUserId);



        commentAnswerQueryWrapper.orderByAsc("createTime");

        List<CommentAnswer> commentAnswerList = this.list(commentAnswerQueryWrapper);

        List<CommentAnswerVO> resultList = commentAnswerList.stream().map(this::getCommentAnswerVO).collect(Collectors.toList());
        List<CommentAnswerVO> secondAnswers = resultList.stream().filter(commentAnswerVO -> commentAnswerVO.getType() == 1).collect(Collectors.toList());

        return resultList.stream().map(commentAnswerVO -> {
            for (CommentAnswerVO secondAnswer : secondAnswers) {
                if(secondAnswer.getToAnswerId().equals(commentAnswerVO.getId())){
                    if(commentAnswerVO.getChildAnswerList() == null){
                        commentAnswerVO.setChildAnswerList(new ArrayList<>());
                    }
                    commentAnswerVO.getChildAnswerList().add(secondAnswer);
                }
            }
            return commentAnswerVO;
        }).collect(Collectors.toList());


    }

    private CommentAnswerVO getCommentAnswerVO(CommentAnswer commentAnswer){
        CommentAnswerVO commentAnswerVO = new CommentAnswerVO();
        BeanUtils.copyProperties(commentAnswer, commentAnswerVO);
        User user =userMapper.selectById(commentAnswer.getCreateUserId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        commentAnswerVO.setCreateUserVO(userVO);
        return commentAnswerVO;
    }


}




