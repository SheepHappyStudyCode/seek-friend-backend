package com.yupi.friend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论回复表
 * @TableName comment_answer
 */
@TableName(value ="comment_answer")
@Data
public class CommentAnswer implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 热度 0-普通 1-热评
     */
    private Integer hot;

    /**
     * 回复类型 0-普通 1-二级回复
     */
    private Integer type;

    /**
     * 如果是二级回复， 回复的answerId
     */
    private Long toAnswerId;

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