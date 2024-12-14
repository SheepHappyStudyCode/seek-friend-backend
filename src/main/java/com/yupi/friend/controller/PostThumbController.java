package com.yupi.friend.controller;

import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.IdRequest;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.interceptor.UserDTO;
import com.yupi.friend.interceptor.UserHolder;
import com.yupi.friend.service.PostThumbService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/post/thumb")
public class PostThumbController {
    @Resource
    private PostThumbService postThumbService;

    /**
     *
     * @param idRequest 帖子id
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateThumb(@RequestBody IdRequest idRequest){
        if(idRequest == null || idRequest.getId() == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserDTO loginUser = UserHolder.getUser();
        Integer result = postThumbService.updateThumb(idRequest.getId(), loginUser.getId());
        return ResultUtils.success(result);

    }
}
