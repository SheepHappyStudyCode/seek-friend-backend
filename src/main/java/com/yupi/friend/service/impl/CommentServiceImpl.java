package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.constant.UserConstant;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.mapper.CommentMapper;
import com.yupi.friend.mapper.PostMapper;
import com.yupi.friend.mapper.UserMapper;
import com.yupi.friend.model.dto.CommentAddDTO;
import com.yupi.friend.model.dto.CommentAnswerQueryDTO;
import com.yupi.friend.model.dto.CommentQueryDTO;
import com.yupi.friend.model.dto.CommentUpdateDTO;
import com.yupi.friend.model.entity.Comment;
import com.yupi.friend.model.entity.Post;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.CommentAnswerVO;
import com.yupi.friend.model.vo.CommentVO;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.CommentAnswerService;
import com.yupi.friend.service.CommentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RedisConstant.POST_ID_KEY;

/**
* @author Administrator
* @description 针对表【comment(评论)】的数据库操作Service实现
* @createDate 2024-08-28 10:10:27
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService {

    @Resource
    private PostMapper postMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommentAnswerService commentAnswerService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Override
    public Long addComment(CommentAddDTO commentAddDTO, User loginUser) {
        if (commentAddDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long postId = commentAddDTO.getPostId();
        String content = commentAddDTO.getContent();

        if (StringUtils.isBlank(content) || content.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论内容不能为空且不能超过256字");
        }


        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子不存在");
        }
        
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentAddDTO, comment);
        comment.setCreateUserId(loginUser.getId());
        boolean save = this.save(comment);
        
        if(!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 更改帖子的缓存
        String key = POST_ID_KEY + postId;
        stringRedisTemplate.opsForHash().increment(key, "commentCount", 1);

        return comment.getId();
    }

    @Override
    public boolean deleteCommentById(Long id, User loginUser) {
        if(id == null || id < 0)
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论不存在");

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Comment comment = this.getById(id);
        if(comment == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论不存在");
        }

        if(!loginUser.getId().equals(comment.getCreateUserId()) && loginUser.getId() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return this.removeById(id);
    }

    @Override
    public Boolean updateComment(CommentUpdateDTO commentUpdateDTO, User loginUser) {
        if(commentUpdateDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        Long id = commentUpdateDTO.getId();
        String content = commentUpdateDTO.getContent();
        Integer hot = commentUpdateDTO.getHot();
        
        Comment comment = this.getById(id);
        if(comment == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "评论不存在");
        }

        if(!loginUser.getId().equals(comment.getCreateUserId()) && loginUser.getId() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        

        return this.update().set(StringUtils.isNotBlank(content), "content", content)
                .set(hot != null, "category", hot)
                .eq("id", id).update();
    }

    @Override
    public List<CommentVO> selectComments(CommentQueryDTO commentQueryDTO) {
        if(commentQueryDTO == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        String searchText = commentQueryDTO.getSearchText();
        Long id = commentQueryDTO.getId();
        Long postId = commentQueryDTO.getPostId();
        Long createUserId = commentQueryDTO.getCreateUserId();
        String content = commentQueryDTO.getContent();
        Integer hot = commentQueryDTO.getHot();


        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();

        commentQueryWrapper.eq(id != null, "id", id)
                .eq(createUserId != null, "createUserId", createUserId)
                .eq(hot != null, "hot", hot)
                .eq(postId != null, "postId", postId);

        commentQueryWrapper.like(StringUtils.isNotBlank(searchText), "content", searchText)
                .like(StringUtils.isNotBlank(content), "content", content);

        commentQueryWrapper.orderByDesc("createTime");

        List<Comment> commentList = this.list(commentQueryWrapper);
        return commentList.stream().map(this::getCommentVO).collect(Collectors.toList());
    }

    CommentVO getCommentVO(Comment comment){
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        Long createUserId = comment.getCreateUserId();

        User user = userMapper.selectById(createUserId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        commentVO.setCreateUser(userVO);
        CommentAnswerQueryDTO commentAnswerQueryDTO = new CommentAnswerQueryDTO();
        commentAnswerQueryDTO.setCommentId(comment.getId());
        List<CommentAnswerVO> commentAnswerVOList = commentAnswerService.selectCommentAnswers(commentAnswerQueryDTO);
        commentVO.setCommentAnswerVOList(commentAnswerVOList);

        return commentVO;
    }
}




