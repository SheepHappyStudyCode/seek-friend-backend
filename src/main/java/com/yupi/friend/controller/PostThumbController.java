package com.yupi.friend.controller;

import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.IdRequest;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.service.PostThumbService;
import com.yupi.friend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/post/thumb")
public class PostThumbController {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbService postThumbService;

    /**
     *
     * @param idRequest 帖子id
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateThumb(@RequestBody IdRequest idRequest, HttpServletRequest request){
        if(idRequest == null || idRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        Boolean result = postThumbService.updateThumb(idRequest.getId(), loginUser);

        return ResultUtils.success(result);

    }
}
