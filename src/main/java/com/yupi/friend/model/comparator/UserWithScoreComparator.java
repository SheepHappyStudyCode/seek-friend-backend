package com.yupi.friend.model.comparator;

import com.yupi.friend.model.entity.UserWithScore;

import java.util.Comparator;

public class UserWithScoreComparator implements Comparator<UserWithScore> {
    @Override
    public int compare(UserWithScore o1, UserWithScore o2) {
        return o1.getScore() - o2.getScore();
    }
}
