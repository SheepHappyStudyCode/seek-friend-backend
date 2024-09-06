package com.yupi.friend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.friend.model.vo.TeamVO;
import com.yupi.friend.model.entity.Team;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.dto.TeamAddDTO;
import com.yupi.friend.model.dto.TeamJoinDTO;
import com.yupi.friend.model.dto.TeamQueryDTO;
import com.yupi.friend.model.dto.TeamUpdateDTO;

import java.util.List;

/**
* @author Administrator
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-08-02 17:30:42
*/
public interface TeamService extends IService<Team> {

    Long addTeam(TeamAddDTO teamAddDTO, User loginUser);
    Page<TeamVO> pageSearch(TeamQueryDTO teamQueryDTO);

    List<TeamVO> listTeams(TeamQueryDTO teamQueryDTO, User loginUser);

    boolean updateTeam(TeamUpdateDTO teamUpdateDTO, User loginUser);

    boolean joinTeam(TeamJoinDTO teamJoinDTO, User loginUser);

    boolean deleteTeamById(Long id, User loginUser);

    boolean leaveTeam(Long teamId, User loginUser);

    List<TeamVO> myTeams(User loginUser);
}
