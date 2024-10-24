package com.yupi.friend.constant;

public interface RedisConstant {
    String JOIN_TEAM_USER_LOCK = "friend:team:join:userId:";

    String JOIN_TEAM_TEAM_LOCK = "friend:team:join:teamId:";

    String USER_ID_KEY = "friend:user:id:";
    String USER_RECOMMEND_KEY = "friend:recommend:user:id:";
    String POST_ID_KEY = "friend:post:id:";

    String POST_THUMB_IDS_KEY = "friend:post:thumb:ids:";

    Long USER_TTL = 10L;

    Long USER_RECOMMEND_TTL = 60L;
}
