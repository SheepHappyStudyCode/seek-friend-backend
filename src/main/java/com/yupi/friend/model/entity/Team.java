package com.yupi.friend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class Team implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
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
     * 创建人id
     */
    private Long userId;

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}