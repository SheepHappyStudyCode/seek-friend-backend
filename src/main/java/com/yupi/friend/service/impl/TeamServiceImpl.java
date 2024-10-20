package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.constant.RedisConstant;
import com.yupi.friend.constant.UserConstant;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.mapper.TeamMapper;
import com.yupi.friend.model.entity.Team;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.entity.UserTeam;
import com.yupi.friend.model.dto.TeamAddDTO;
import com.yupi.friend.model.dto.TeamJoinDTO;
import com.yupi.friend.model.dto.TeamQueryDTO;
import com.yupi.friend.model.dto.TeamUpdateDTO;
import com.yupi.friend.model.enums.TeamStatusEnum;
import com.yupi.friend.model.vo.TeamVO;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.TeamService;
import com.yupi.friend.service.UserService;
import com.yupi.friend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
* @author Administrator
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-08-02 17:30:42
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Resource
    private UserTeamService userTeamService;
    
    @Resource
    private UserService userService;

    @Resource
    private TeamMapper teamMapper;

    @Resource
    private RedissonClient redisson;

    @Override
    @Transactional
    public Long addTeam(TeamAddDTO teamAddDTO, User loginUser) {
        if(teamAddDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddDTO, team);
        team.setUserId(loginUser.getId());

        //- 队伍姓名不能为空
        String name = team.getName();
        if(StringUtils.isBlank(name)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍名称不符合要求");
        }

        //- 最大人数不能超过10人
        int num = team.getMaxNum();
        if(num <= 0 || num > 10){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不符合要求");
        }

        //- 队伍过期时间不能比今天早
        Date date = team.getExpireTime();
        if(date != null && date.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍过期时间过早");
        }

        //- 队伍类型不能超范围， 如果是加密队伍必须有密码
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(team.getStatus());

        if(statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍类型不符合要求");
        }

        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            String password = team.getPassword();
            if(password == null || password.length() < 8 || password.length() > 256){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍密码不符合要求");
            }
        }

        boolean res = this.save(team);

        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 向 user-team 表插入数据
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(team.getUserId());
        userTeam.setTeamId(team.getId());
        userTeam.setJoinTime(team.getCreateTime());
        res = userTeamService.save(userTeam);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return team.getId();
    }

    @Override
    public Page<TeamVO> pageSearch(TeamQueryDTO teamQueryDTO) {
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if(teamQueryDTO != null){
            //2. 根据传入的 dto 进行条件查询
            //3. 可以通过输入的关键词进行查询
            //4. 如果队伍过期就不展示
            String searchtext = teamQueryDTO.getSearchtext();
            String name = teamQueryDTO.getName();
            String description = teamQueryDTO.getDescription();
            Integer maxNum = teamQueryDTO.getMaxNum();
            Long userId = teamQueryDTO.getUserId();
            Date expireTime = teamQueryDTO.getExpireTime();
            Integer status = teamQueryDTO.getStatus();

            teamQueryWrapper.and(StringUtils.isNotBlank(searchtext), qw -> qw.like( "name", searchtext).or().like("description", searchtext));

            teamQueryWrapper.like(StringUtils.isNotBlank(name), "name", name).like(StringUtils.isNotBlank(description), "description", description);


            teamQueryWrapper.eq(maxNum != null, "maxNum", maxNum).eq(userId != null, "userId", userId).eq(status != null, "status", status);

            teamQueryWrapper.lt(expireTime != null, "expireTime", expireTime);
        }

        teamQueryWrapper.and(wrapper -> wrapper.isNull("expireTime").or().gt("expireTime", new Date())).eq("status", TeamStatusEnum.PUBLIC.getValue());

        //5. 同时返回队伍创建人的信息
        long current = teamQueryDTO.getCurrent();
        long pageSize = teamQueryDTO.getPageSize();

        Page<Team> teamPage = this.page(new Page<>(current, pageSize), teamQueryWrapper);

        List<TeamVO> teamVOList = new ArrayList<>();
        for (Team team : teamPage.getRecords()) {
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            User user = userService.getById(team.getUserId());
            teamVO.setCreateUser(new UserVO());
            BeanUtils.copyProperties(user, teamVO.getCreateUser());

            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            Long teamId = team.getId();
            userTeamQueryWrapper.eq("teamId", teamId);
            long count = userTeamService.count(userTeamQueryWrapper);
            teamVO.setJoinNum((int)count);
            teamVOList.add(teamVO);
        }


        Page<TeamVO> teamVOPage = new Page<TeamVO>(current, pageSize);
        BeanUtils.copyProperties(teamPage, teamVOPage);
        teamVOPage.setRecords(teamVOList);

        return teamVOPage;

    }

    @Override
    public List<TeamVO> listTeams(TeamQueryDTO teamQueryDTO, User loginUser) {

        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        Long loginUserId = loginUser == null ? null : loginUser.getId();
        teamQueryWrapper.notExists(loginUserId != null, String.format("select * from user_team ut where ut.userId = %s and ut.teamId = team.id and isDelete = 0", loginUserId));

        if(teamQueryDTO != null){
            //1. 只有管理员才能查看非公开的房间
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(teamQueryDTO.getStatus());
            teamStatusEnum = teamStatusEnum == null ? TeamStatusEnum.PUBLIC : teamStatusEnum;
            if(!userService.isAdmin(loginUser) && teamStatusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            //2. 根据传入的 dto 进行条件查询
            //3. 可以通过输入的关键词进行查询
            //4. 如果队伍过期就不展示

            String searchtext = teamQueryDTO.getSearchtext();
            String name = teamQueryDTO.getName();
            String description = teamQueryDTO.getDescription();
            Integer maxNum = teamQueryDTO.getMaxNum();
            Long userId = teamQueryDTO.getUserId();
            Date expireTime = teamQueryDTO.getExpireTime();
            Integer status = teamQueryDTO.getStatus();


            teamQueryWrapper.and(StringUtils.isNotBlank(searchtext), qw -> qw.like( "name", searchtext).or().like("description", searchtext));

            teamQueryWrapper.like(StringUtils.isNotBlank(name), "name", name).like(StringUtils.isNotBlank(description), "description", description);

            teamQueryWrapper.eq(maxNum != null, "maxNum", maxNum).eq(userId != null, "userId", userId).eq(status != null, "status", status);

            teamQueryWrapper.and(wrapper -> wrapper.isNull("expireTime").or().gt("expireTime", new Date()).lt(expireTime != null, "expireTime", expireTime));


        }
        else{
            teamQueryWrapper.and(wrapper -> wrapper.isNull("expireTime").or().gt("expireTime", new Date())).eq("status", TeamStatusEnum.PUBLIC.getValue());
        }

        //5. 同时返回队伍创建人的信息
        List<Team> teamList = this.list(teamQueryWrapper);
        List<TeamVO> teamVOList = new ArrayList<>();
        for (Team team : teamList) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);
            User user = userService.getById(team.getUserId());
            teamVO.setCreateUser(new UserVO());
            BeanUtils.copyProperties(user, teamVO.getCreateUser());
            // 查询队伍已加入人数
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            Long teamId = team.getId();
            userTeamQueryWrapper.eq("teamId", teamId);
            long count = userTeamService.count(userTeamQueryWrapper);
            teamVO.setJoinNum((int)count);

            teamVOList.add(teamVO);
        }
        
        return teamVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateDTO teamUpdateDTO, User loginUser) {
        if(teamUpdateDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        if(!teamUpdateDTO.getId().equals(loginUser.getId()) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        Team team = this.getById(teamUpdateDTO.getId());
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新的队伍不存在");
        }

        BeanUtils.copyProperties(teamUpdateDTO, team);

        boolean res = this.updateById(team);

        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return res;
    }

    @Override
    public boolean joinTeam(TeamJoinDTO teamJoinDTO, User loginUser) {
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        long userId = loginUser.getId();
        long teamId = teamJoinDTO.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();

        // 1. 只能加入存在且未过期的队伍
        teamQueryWrapper.eq("id", teamId);
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        Date expireTime = team.getExpireTime();
        if(expireTime != null && expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期，无法加入");
        }

        // 如果加入的是加密队伍，需要密码
        if(TeamStatusEnum.SECRET.equals(TeamStatusEnum.getEnumByValue(team.getStatus()))){
            String password = teamJoinDTO.getPassword();
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "申请密码与队伍密码不符");
            }
        }

        // 分布式锁
        // 获取锁对象实例（无法保证是按线程的顺序获取到）
        RLock user_lock = redisson.getLock(RedisConstant.JOIN_TEAM_USER_LOCK + userId);
        RLock team_lock = redisson.getLock(RedisConstant.JOIN_TEAM_TEAM_LOCK + teamId);
        RedissonMultiLock multiLock = new RedissonMultiLock(user_lock, team_lock);

        try {
            // 尝试获取锁，最多等待100秒，上锁以后10秒自动解锁
            boolean res = multiLock.tryLock(1, 10, TimeUnit.SECONDS);
            if (res) {
                try {
                    // 不能加入已经满了的队伍
                    Integer maxNum = team.getMaxNum();
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long count = userTeamService.count(userTeamQueryWrapper);
                    if(count >= maxNum){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满，无法加入");
                    }

                    // 不能重复加入已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId", teamId);
                    userTeamQueryWrapper.eq("userId", userId);
                    count = userTeamService.count(userTeamQueryWrapper);
                    if(count > 0){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能重复加入一个队伍");
                    }

                    // 每个用户最多加入5个队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    count = userTeamService.count(userTeamQueryWrapper);
                    if(count > 5){
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "一个用户最多加入 5 个队伍");
                    }

                    // 修改队伍信息，补充人数
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    boolean result = userTeamService.save(userTeam);
                    return result;

                } finally {
                    // 释放锁
                    multiLock.unlock();
                    System.out.println("Lock released");


                }
            } else {
                System.out.println("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeamById(Long teamId, User loginUser) {
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除的队伍不存在");
        }

        Long userId = loginUser.getId();
        if(!team.getUserId().equals(userId) && UserConstant.ADMIN_ROLE != loginUser.getUserRole()){
            throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
        }

        boolean res = this.removeById(teamId);
        if(!res){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        res = userTeamService.remove(userTeamQueryWrapper);
        return res;

    }

    @Override
    public boolean leaveTeam(Long teamId, User loginUser) {
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "离开的队伍不存在");
        }

        Long userId = loginUser.getId();
        if(team.getUserId().equals(userId)){
            throw new BusinessException(ErrorCode.NO_AUTH, "队长不能退出队伍，只能解散队伍");
        }

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamQueryWrapper.eq("userId", userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count < 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该用户不在队伍，无法退出");
        }

        boolean result = userTeamService.remove(userTeamQueryWrapper);

        return result;
    }

    @Override
    public List<TeamVO> myTeams(User loginUser) {
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }

        Long userId = loginUser.getId();

        if(userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.exists(String.format("select * from user_team ut where ut.teamId = team.id and ut.userId = %s and isDelete = 0", userId));
        List<Team> teamList = this.list(teamQueryWrapper);

        List<TeamVO> teamVOList = new ArrayList<>();
        for (Team team : teamList) {
            TeamVO teamVO = new TeamVO();
            BeanUtils.copyProperties(team, teamVO);

            // 查询队伍已加入人数
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            Long teamId = team.getId();
            userTeamQueryWrapper.eq("teamId", teamId);
            long count = userTeamService.count(userTeamQueryWrapper);
            teamVO.setJoinNum((int)count);
            teamVOList.add(teamVO);
        }
        return teamVOList;
    }
}




