package com.yupi.friend.controller;

import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.IdRequest;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.dto.CommentAnswerAddDTO;
import com.yupi.friend.model.dto.CommentAnswerQueryDTO;
import com.yupi.friend.model.dto.CommentAnswerUpdateDTO;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.CommentAnswerVO;
import com.yupi.friend.service.CommentAnswerService;
import com.yupi.friend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/commentAnswer")
public class CommentAnswerController {
    @Resource
    private CommentAnswerService commentAnswerService;

    @Resource
    private UserService userService;


    @PostMapping("/add")
    BaseResponse<Long> addCommentAnswer(@RequestBody CommentAnswerAddDTO CommentAnswerAddDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long id = commentAnswerService.addCommentAnswer(CommentAnswerAddDTO, loginUser);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    BaseResponse<Boolean> deleteCommentAnswer(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if(idRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);

        boolean result = commentAnswerService.deleteCommentAnswerById(idRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    BaseResponse<Boolean> updateCommentAnswer(@RequestBody CommentAnswerUpdateDTO CommentAnswerUpdateDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Boolean result = commentAnswerService.updateCommentAnswer(CommentAnswerUpdateDTO, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/query")
    BaseResponse<List<CommentAnswerVO>> queryCommentAnswers(CommentAnswerQueryDTO commentAnswerQueryDTO){
        List<CommentAnswerVO> commentAnswerVOList= commentAnswerService.selectCommentAnswers(commentAnswerQueryDTO);
        return ResultUtils.success(commentAnswerVOList);
    }



}
