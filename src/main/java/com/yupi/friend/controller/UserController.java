package com.yupi.friend.controller;

import com.yupi.friend.annotation.AuthCheck;
import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.dto.UserQueryDTO;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.request.UserLoginRequest;
import com.yupi.friend.model.request.UserRegisterRequest;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.UserService;
import com.yupi.friend.utils.JWTUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        
        String token = userService.userLogin(userAccount, userPassword);
        
        return ResultUtils.success(token);
    }
    

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Map<String, Object> userInfo = JWTUtils.verifyToken(token);
        if(userInfo == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Integer id = (Integer) userInfo.get("id");
        UserVO safetyUser = userService.queryById(id.longValue());
        return ResultUtils.success(safetyUser);
    }


    /**
     *
     * @param
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<UserVO>> searchUsers(UserQueryDTO userQueryDTO, HttpServletRequest request) {
        if(userQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);

        List<UserVO> userVOList = userService.queryUsers(userQueryDTO, loginUser);

        return ResultUtils.success(userVOList);
    }

    @GetMapping("/recommend")
    public BaseResponse<List<UserVO>> recommendUsers(int num, HttpServletRequest request) {
        if(num < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "至少推荐一个用户");
        }

        User loginUser = userService.getLoginUser(request);
        List<UserVO> userVOList = userService.recommendUsers(num, loginUser);
        return ResultUtils.success(userVOList);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = "管理员")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id) {

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }



    @GetMapping("/search/tags")
    public BaseResponse<List<UserVO>> getUsersByTags(@RequestParam List<String> tagNameList){
        List<UserVO> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);

    }

    /**
     *
     * @param user 修改的用户
     * @param request
     * @return 更新的数据条数， 失败返回 0， 成功返回 1
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody User user, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);

        boolean result = userService.updateUser(user, loginUser);

        return ResultUtils.success(result);
    }


    /**
     * 通过队伍id得到队伍的成员信息，并且队长一定在第一位
     * @param teamId 队伍id
     * @return 成员信息
     */
    @GetMapping("/team/members")
    public BaseResponse<List<UserVO>> queryUsersByTeamId(@RequestParam Long teamId) {
        if(teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不能为空");
        }

        List<UserVO> userVOList = userService.queryUsersbyTeamId(teamId);

        return ResultUtils.success(userVOList);
    }








}
