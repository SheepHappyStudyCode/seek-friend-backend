package com.yupi.friend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子
 */
@Data
public class PostQueryDTO implements Serializable {

    private String searchText;

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
     * 创建人id
     */
    private Long createUserId;


}