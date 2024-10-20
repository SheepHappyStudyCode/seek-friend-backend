package com.yupi.friend;

import com.yupi.friend.model.entity.User;
import com.yupi.friend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
public class InsertUsers {
    @Resource
    private UserService userService;

    @Test
    void testInsertUsers(){
        List<User> userList = new ArrayList<>();
        int cnt = 300000;

        for(int i = 0; i  < cnt; i++){
            User user = new User();
            user.setUsername("fakeUsre");
            user.setUserAccount("fakeUsre");
            user.setAvatarUrl("https://ts1.cn.mm.bing.net/th/id/R-C.cf6fdc8ce5cf10ef8a1e3d25ba1cf5af?rik=77GnZ5J6xVI9iQ&riu=http%3a%2f%2fi2.hdslb.com%2fbfs%2farchive%2f24418dc1f90f56a818978d849f53695ae545ee69.jpg&ehk=%2bFoPa4qMssXgghxBCqryhm8lhmLvWQ8f%2bJpw0eZFO8Y%3d&risl=&pid=ImgRaw&r=0");
            user.setGender(0);
            user.setUserPassword("b0dd3697a192885d7c055db46155b26a");
            user.setPhone("12345678");
            user.setEmail("eve@example.com");
            user.setTags("[\"reading\", \"photography\", \"java\", \"女\"]");
            user.setUserDescription("我不是小黑子 我不是小黑子 我不是小黑子");
            userList.add(user);

        }

        userService.saveBatch(userList, cnt / 10);
    }

    @Test
    void test(){

    }


}
