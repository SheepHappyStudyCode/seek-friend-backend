package com.yupi.friend.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 */
@Data
public class CommentAnswerUpdateDTO implements Serializable {
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



    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}