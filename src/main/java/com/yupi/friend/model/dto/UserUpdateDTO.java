package com.yupi.friend.model.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户描述
     */
    private String userDescription;


    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * qq号
     */
    private String qq;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0 - 正常
     */
    private Integer userStatus;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 标签 json 数组
     */
    private String tags;


}
