package com.yupi.friend.controller;

import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private UserService userService;

    @PostMapping("/user/avatar")
    public BaseResponse<Boolean> updateUserAvatar(MultipartFile avatar) {
        User loginUser = userService.getLoginUser();

        boolean result = userService.uploadAvatar(avatar, loginUser);
        return ResultUtils.success(result);
    }
}
