package com.yupi.friend;

import com.yupi.friend.mapper.TeamMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class SQLTest {

    @Resource
    private TeamMapper teamMapper;

    @Test
    void testSql(){
        List<Long> ids = Arrays.asList(1l, 2l, 3l);
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        System.out.println(idStr);
    }


}
