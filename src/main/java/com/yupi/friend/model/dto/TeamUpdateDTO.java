package com.yupi.friend.model.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TeamUpdateDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 队伍的最大人数
     */
    private Integer maxNum;


    /**
     * 队伍过期时间
     */
    private Date expireTime;

    /**
     * 队伍类型 0 - 公开， 1 - 私密， 2 - 加密
     */
    private Integer status;

    /**
     * 加密队伍的密码
     */
    private String password;


}
