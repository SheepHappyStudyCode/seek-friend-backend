package com.yupi.friend.model.dto;

import com.yupi.friend.common.PageQuery;
import lombok.Data;

import java.util.Date;

@Data
public class TeamQueryDTO extends PageQuery {

    private String searchtext;


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
     * 创建时间
     */
    private Date createTime;

}
