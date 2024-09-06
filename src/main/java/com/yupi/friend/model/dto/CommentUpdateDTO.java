package com.yupi.friend.model.dto;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论
 */
@TableName(value ="comment")
@Data
public class CommentUpdateDTO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 热度 0-普通 1-热评
     */
    private Integer hot;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


}