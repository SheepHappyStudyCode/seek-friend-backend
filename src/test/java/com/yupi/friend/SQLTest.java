package com.yupi.friend;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.friend.mapper.TeamMapper;
import com.yupi.friend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class SQLTest {

    @Resource
    private TeamMapper teamMapper;

    @Test
    void testSql(){
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>();
        userQueryWrapper.like("u.username", "sheephappy");
        List<Long> idList = teamMapper.queryTeamByUsername(userQueryWrapper);

        for (Long id : idList) {
            System.out.println(id);
        }

    }
}
