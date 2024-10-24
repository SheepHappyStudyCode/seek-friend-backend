package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.constant.UserConstant;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.interceptor.UserHolder;
import com.yupi.friend.mapper.CommentMapper;
import com.yupi.friend.mapper.PostMapper;
import com.yupi.friend.mapper.PostThumbMapper;
import com.yupi.friend.mapper.UserMapper;
import com.yupi.friend.model.dto.PostAddDTO;
import com.yupi.friend.model.dto.PostQueryDTO;
import com.yupi.friend.model.dto.PostUpdateDTO;
import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.PostVO;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.PostService;
import com.yupi.friend.service.UserService;
import com.yupi.friend.utils.RedisCacheClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RedisConstant.POST_ID_KEY;
import static com.yupi.friend.constant.RedisConstant.POST_THUMB_IDS_KEY;

/**
* @author Administrator
* @description 针对表【post(帖子)】的数据库操作Service实现
* @createDate 2024-08-27 21:58:05
*/
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>
    implements PostService{

    @Resource
    UserMapper userMapper;

    @Resource
    CommentMapper commentMapper;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisCacheClient redisCacheClient;

    @Override
    public Long addPost(PostAddDTO postAddDTO, User loginUser) {
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        if(postAddDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String title = postAddDTO.getTitle();
        String content = postAddDTO.getContent();
        Integer category = postAddDTO.getCategory();
        Long createUserId = loginUser.getId();

        if(StringUtils.isBlank(content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
        }

        if(category == null || category < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "分类不存在");
        }

        if(createUserId == null || createUserId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>();
        userQueryWrapper.eq("id", createUserId);
        User user = userMapper.selectOne(userQueryWrapper);
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        Post post = new Post();
        BeanUtils.copyProperties(postAddDTO, post);
        post.setCreateUserId(createUserId);
        boolean result = this.save(post);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发布失败");
        }
        Post newPost = this.getById(post.getId());
        // 写入缓存
        String key = POST_ID_KEY + post.getId();
        redisCacheClient.setHashObject(key, newPost);

        return newPost.getId();
    }

    @Override
    public boolean deletePostById(Long id, User loginUser) {
        if(id == null || id < 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Post post = this.getById(id);
        if(post == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }

        if(!loginUser.getId().equals(post.getCreateUserId()) && loginUser.getId() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return this.removeById(id);
    }

    @Override
    public Boolean updatePost(PostUpdateDTO postUpdateDTO, User loginUser) {
        if(postUpdateDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long id = postUpdateDTO.getId();
        String title = postUpdateDTO.getTitle();
        String content = postUpdateDTO.getContent();
        Integer category = postUpdateDTO.getCategory();

        Post post = this.getById(id);
        if(post == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }

        if(!loginUser.getId().equals(post.getCreateUserId()) && loginUser.getId() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return this.update().set(StringUtils.isNotBlank(title), "title", title)
                .set(StringUtils.isNotBlank(content), "content", content)
                .set(category != null, "category", category)
                .eq("id", id).update();

    }

    @Override
    public List<PostVO> selectPosts(PostQueryDTO postQueryDTO) {
        if(postQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        Page<Post> postPage = new Page<>(postQueryDTO.getCurrent(), postQueryDTO.getPageSize());

        String searchText = postQueryDTO.getSearchText();
        Long id = postQueryDTO.getId();
        String title = postQueryDTO.getTitle();
        String content = postQueryDTO.getContent();
        Integer category = postQueryDTO.getCategory();
        Long createUserId = postQueryDTO.getCreateUserId();

        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();

        postQueryWrapper.select("id").eq(id != null, "id", id).eq(createUserId != null, "createUserId", createUserId).eq(category != null, "category", category);
        
        postQueryWrapper.and(StringUtils.isNotBlank(searchText), wrapper -> wrapper.like("title", searchText).or().like("content", searchText))
                .like(StringUtils.isNotBlank(title), "title", title)
                .like(StringUtils.isNotBlank(content), "content", content);
        postQueryWrapper.orderByDesc("createTime");

        Page<Post> page = this.page(postPage, postQueryWrapper);

        List<Post> records = page.getRecords();
        List<String> keyList = records.stream().map(record -> POST_ID_KEY + record.getId()).collect(Collectors.toList());
        List<Post> posts = redisCacheClient.multiGetHashObject(keyList, Post.class);

        return posts.stream().map(this::getPostVO).collect(Collectors.toList());
        
    }

    @Override
    public PostVO getPostVO(Post post) {
        if(post == null)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");

        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);

        // 获取用户信息
        long userId = post.getCreateUserId();
        UserVO userVO = userService.queryById(userId);
        postVO.setCreateUser(userVO);

        // 判断是否点赞
        if(UserHolder.getUser() != null){
            // 登录时
            String postThumbKey = POST_THUMB_IDS_KEY + post.getId();
            Boolean member = stringRedisTemplate.opsForSet().isMember(postThumbKey, UserHolder.getUser().getId().toString());
            postVO.setLike(Boolean.TRUE.equals(member));
        }
        else{
            postVO.setLike(false);
        }

        return postVO;

    }

//    @Override
//    public PostVO getPostVO(Post post) {
//        if(post == null)
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
//
//        PostVO postVO = new PostVO();
//        BeanUtils.copyProperties(post, postVO);
//
//        // 获取用户信息
//        long userId = post.getCreateUserId();
//        UserVO userVO = userService.queryById(userId);
//        postVO.setCreateUser(userVO);
//
//        // 判断是否点赞
//        if(UserHolder.getUser() != null){
//            // 登录时
//            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
//            postThumbQueryWrapper.eq("userId", UserHolder.getUser().getId());
//            Long res = postThumbMapper.selectCount(postThumbQueryWrapper);
//            postVO.setLike(res > 0);
//        }
//        else{
//            postVO.setLike(false);
//        }
//
//
//        return postVO;
//
//    }


}




