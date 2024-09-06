package com.yupi.friend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子
 */
@Data
public class PostUpdateDTO implements Serializable {

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

}