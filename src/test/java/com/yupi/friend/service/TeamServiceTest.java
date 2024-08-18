package com.yupi.friend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.friend.model.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class TeamServiceTest {

    @Resource
    private TeamService teamService;


    @Test
    void testUpdate(){
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();

        teamQueryWrapper.eq("name", "张三").or().eq("description", "里斯").lt("createTime", new Date()).eq("status", 1);


        List<Team> teamList = teamService.list(teamQueryWrapper);






    }
}
