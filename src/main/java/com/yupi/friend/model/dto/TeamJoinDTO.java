package com.yupi.friend.model.dto;

import lombok.Data;

@Data
public class TeamJoinDTO {
    /**
     * id
     */
    private Long id;

    /**
     * 加密队伍的密码
     */
    private String password;

}
