package com.yupi.friend.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostThumbUpdateMessage implements Serializable {
    private String id;
    private long postId; // 帖子 id
    private long userId; // 点赞的用户 id

}