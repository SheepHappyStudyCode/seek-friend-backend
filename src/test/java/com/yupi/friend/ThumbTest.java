package com.yupi.friend;

import com.yupi.friend.service.PostThumbService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class ThumbTest {

    @Resource
    private PostThumbService postThumbService;

    @Test
    public void testThumb(){
        long userId = 1;
        long postId = 4;
        for(int i = 0; i < 10; i++){
            Integer ret = postThumbService.updateThumbInDb(postId, userId);
            if(ret == 0){
                System.out.println("点赞成功");
            }
            else{
                System.out.println("取消点赞成功");
            }
        }
    }
}
