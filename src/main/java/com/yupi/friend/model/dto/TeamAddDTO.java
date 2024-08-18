package com.yupi.friend.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TeamAddDTO {

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
     * 创建人id
     */
    private Long userId;

    /**
     * 队伍过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
