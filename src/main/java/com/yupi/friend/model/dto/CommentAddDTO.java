package com.yupi.friend.model.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论
 */
@TableName(value ="comment")
@Data
public class CommentAddDTO implements Serializable {
    /**
     * 帖子id
     */
    private Long postId;

    /**
     * 评论内容
     */
    private String content;

}