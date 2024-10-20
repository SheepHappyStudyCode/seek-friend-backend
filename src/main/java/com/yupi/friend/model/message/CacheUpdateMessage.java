package com.yupi.friend.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class CacheUpdateMessage implements Serializable {
    private String key;
    private Object value;

}