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
    public void testAdd() {
        postThumbService.addThumb(4, 2);
    }

    @Test
    public void testRemove(){
        postThumbService.removeThumb(4, 2);
    }
}
