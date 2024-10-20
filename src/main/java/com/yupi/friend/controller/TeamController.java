package com.yupi.friend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.friend.common.BaseResponse;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.common.ResultUtils;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.model.dto.TeamAddDTO;
import com.yupi.friend.model.dto.TeamJoinDTO;
import com.yupi.friend.model.dto.TeamQueryDTO;
import com.yupi.friend.model.dto.TeamUpdateDTO;
import com.yupi.friend.model.entity.Team;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.TeamVO;
import com.yupi.friend.service.TeamService;
import com.yupi.friend.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST}, allowCredentials = "true")
public class TeamController {

    @Resource
    private TeamService teamService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    BaseResponse<Long> addTeam(@RequestBody TeamAddDTO teamAddDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long id = teamService.addTeam(teamAddDTO, loginUser);
        return ResultUtils.success(id);
    }

    @PostMapping("/delete")
    BaseResponse<Boolean> deleteTeam(@RequestBody Long id, HttpServletRequest request){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.deleteTeamById(id, loginUser);

        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateDTO teamUpdateDTO, HttpServletRequest request){
        User loginUsre = userService.getLoginUser(request);
        Boolean result = teamService.updateTeam(teamUpdateDTO, loginUsre);
        return ResultUtils.success(result);
    }

    @GetMapping
    BaseResponse<Team> queryTeamById(Long id){
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Team team = teamService.getById(id);

        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    BaseResponse<List<TeamVO>> listTeams(TeamQueryDTO teamQueryDTO, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);

        List<TeamVO> teamVOList = teamService.listTeams(teamQueryDTO, loginUser);
        return ResultUtils.success(teamVOList);
    }

    @GetMapping("/list/page")
    BaseResponse<Page<TeamVO>> pageQueryTeams(TeamQueryDTO teamQueryDTO){
        if(teamQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        Page<TeamVO> teamPage = teamService.pageSearch(teamQueryDTO);
        return ResultUtils.success(teamPage);
    }

    @PostMapping("/join")
    BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinDTO teamJoinDTO, HttpServletRequest request){
        if(teamJoinDTO == null ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }

        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.joinTeam(teamJoinDTO, loginUser);
        return ResultUtils.success(result);
    }

    @ApiOperation(value = "非队长离开队伍")
    @PostMapping("/leave")
    BaseResponse<Boolean> leaveTeam(@RequestBody Long teamId, HttpServletRequest request){
        if(teamId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);

        boolean result = teamService.leaveTeam(teamId, loginUser);

        return ResultUtils.success(result);
    }

    @GetMapping("/my")
    BaseResponse<List<TeamVO>> myTeams(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        List<TeamVO> teamVOList= teamService.myTeams(loginUser);
        return ResultUtils.success(teamVOList);
    }



}
