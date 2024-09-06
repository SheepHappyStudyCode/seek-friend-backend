package com.yupi.friend.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class CommentAnswerAddDTO implements Serializable {
    /**
     * 评论id
     */
    private Long commentId;

    /**
     * 创建人id
     */
    private Long createUserId;

    /**
     * 回复内容
     */
    private String content;


    /**
     * 回复类型 0-普通 1-二级回复
     */
    private Integer type;

    /**
     * 如果是二级回复， 回复的answerId
     */
    private Long toAnswerId;


    private static final long serialVersionUID = 1L;
}