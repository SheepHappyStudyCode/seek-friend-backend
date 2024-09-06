package com.yupi.friend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 帖子点赞表
 * @TableName post_thumb
 */
@TableName(value ="post_thumb")
@Data
public class PostThumb implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 帖子id
     */
    private Long postId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}