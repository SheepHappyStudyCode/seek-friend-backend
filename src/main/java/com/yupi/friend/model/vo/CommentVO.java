package com.yupi.friend.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class CommentVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 帖子id
     */
    private Long postId;

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

    /**
     * 创建人
     */
    private UserVO createUser;

    private List<CommentAnswerVO> commentAnswerVOList;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}