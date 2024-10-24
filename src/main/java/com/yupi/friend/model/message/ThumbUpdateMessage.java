package com.yupi.friend.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThumbUpdateMessage implements Serializable {
    private String key;
    private long postId;
    private long userId;

}