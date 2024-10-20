package com.yupi.friend.controller;

import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.IdRequest;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.dto.PostAddDTO;
import com.yupi.friend.model.dto.PostQueryDTO;
import com.yupi.friend.model.dto.PostUpdateDTO;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.PostVO;
import com.yupi.friend.service.PostService;
import com.yupi.friend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    BaseResponse<Long> addPost(@RequestBody PostAddDTO PostAddDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long id = postService.addPost(PostAddDTO, loginUser);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    BaseResponse<Boolean> deletePost(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if(idRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);

        boolean result = postService.deletePostById(idRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    BaseResponse<Boolean> updatePost(@RequestBody PostUpdateDTO PostUpdateDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Boolean result = postService.updatePost(PostUpdateDTO, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/query")
    BaseResponse<List<PostVO>> queryPosts(PostQueryDTO postQueryDTO){
        List<PostVO> postVOList= postService.selectPosts(postQueryDTO);
        return ResultUtils.success(postVOList);
    }



}
