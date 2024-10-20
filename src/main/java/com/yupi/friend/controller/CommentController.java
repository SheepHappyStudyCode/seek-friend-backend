package com.yupi.friend.controller;

import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.IdRequest;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.dto.CommentAddDTO;
import com.yupi.friend.model.dto.CommentQueryDTO;
import com.yupi.friend.model.dto.CommentUpdateDTO;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.CommentVO;
import com.yupi.friend.service.CommentService;
import com.yupi.friend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    BaseResponse<Long> addComment(@RequestBody CommentAddDTO CommentAddDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long id = commentService.addComment(CommentAddDTO, loginUser);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    BaseResponse<Boolean> deleteComment(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if(idRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);

        boolean result = commentService.deleteCommentById(idRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    BaseResponse<Boolean> updateComment(@RequestBody CommentUpdateDTO CommentUpdateDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Boolean result = commentService.updateComment(CommentUpdateDTO, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/query")
    BaseResponse<List<CommentVO>> queryComments(CommentQueryDTO commentQueryDTO){
        List<CommentVO> commentVOList= commentService.selectComments(commentQueryDTO);
        return ResultUtils.success(commentVOList);
    }



}
