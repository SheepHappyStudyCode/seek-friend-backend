package com.yupi.friend.model.dto;

import com.yupi.friend.common.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostQueryDTO extends PageQuery implements Serializable {

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