package com.yupi.friend.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserQueryDTO {


    /**
     * 搜索关键词
     */
    private String searchText;

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
     * 账号
     */
    private String userAccount;

    /**
     *  性别 0 - 男  1 - 女
     */
    private Integer gender;

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
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;


    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 标签 json 数组
     */
    private String[] tagList;


}
