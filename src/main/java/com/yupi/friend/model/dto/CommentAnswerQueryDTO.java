package com.yupi.friend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class CommentAnswerQueryDTO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 评论id
     */
    private Long commentId;

    /**
     * 创建人id
     */
    private Long createUserId;


    private static final long serialVersionUID = 1L;
}