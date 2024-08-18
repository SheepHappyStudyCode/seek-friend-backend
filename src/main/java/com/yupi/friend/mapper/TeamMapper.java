package com.yupi.friend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.friend.model.entity.Team;
import com.yupi.friend.model.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Administrator
* @description 针对表【team(队伍)】的数据库操作Mapper
* @createDate 2024-08-02 17:30:41
* @Entity com.yupi.friend.model.domain.Team
*/
public interface TeamMapper extends BaseMapper<Team> {
    // select ut.teamId from user u, user_team ut where u.id = ut.userId where username like "sheephappy";
    @Select("select ut.teamId from user_team ut inner join user u on ut.userId = u.id ${ew.customSqlSegment} ")
    List<Long> queryTeamByUsername(@Param("ew") QueryWrapper<User> queryWrapper);

//    @Select("select * from team t where not exists (select * from user_team ut where t.id = ut.teamId and ut.userId = ${userId} and ut.isDelete = 0) and isDelete = 0 limit ${num}")
//    List<Team> recommendTeams(@Param("userId") Long userId, @Param("num") int num);

    @Select("select * from team t where not exists (select * from user_team ut where t.id = ut.teamId and ut.userId = ${userId} and ut.isDelete = 0) and isDelete = 0 limit ${num}")
    List<Team> recommendTeams(@Param("userId") Long userId, @Param("num") int num);
}




