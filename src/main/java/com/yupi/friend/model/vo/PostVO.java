package com.yupi.friend.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class PostVO {
    /**
     * id
     */
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子分类
     */
    private Integer category;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private UserVO createUser;

    private Integer likeCount;

    private Integer commentCount;

    private boolean like;
}
