package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.model.entity.UserTeam;
import com.yupi.friend.service.UserTeamService;
import com.yupi.friend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【user_team(用户-队伍关系表)】的数据库操作Service实现
* @createDate 2024-08-02 21:55:24
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




