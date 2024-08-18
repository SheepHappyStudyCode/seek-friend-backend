package com.yupi.friend.model.entity;

import lombok.Data;

@Data
public class UserWithScore {

    private long id;

    private int score = 0;

}
