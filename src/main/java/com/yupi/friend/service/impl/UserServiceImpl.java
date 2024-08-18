package com.yupi.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.friend.common.ErrorCode;
import com.yupi.friend.exception.BusinessException;
import com.yupi.friend.mapper.TeamMapper;
import com.yupi.friend.mapper.UserMapper;
import com.yupi.friend.model.comparator.UserWithScoreComparator;
import com.yupi.friend.model.dto.UserQueryDTO;
import com.yupi.friend.model.entity.Team;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.entity.UserTeam;
import com.yupi.friend.model.entity.UserWithScore;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.service.UserService;
import com.yupi.friend.service.UserTeamService;
import com.yupi.friend.utils.AliOSSUtils;
import com.yupi.friend.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.UserConstant.ADMIN_ROLE;
import static com.yupi.friend.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserTeamService userTeamService;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }


        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 账户不能包含特殊字符
        String validPattern = "[^a-zA-Z0-9]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户只能包含大小写字母和数字");
        }

        // 密码只能包含特定的字符
        validPattern = "[^a-zA-Z0-9!@#$%^&*(),<.>/?;:{}\\[\\]|]";
        matcher = Pattern.compile(validPattern).matcher(userPassword);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能包含中文或其他特殊字符");
        }

        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码不相同");
        }

        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能重复");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getId();
    }

    // [加入星球](https://www.code-nav.cn/) 从 0 到 1 项目实战，经验拉满！10+ 原创项目手把手教程、7 日项目提升训练营、60+ 编程经验分享直播、1000+ 项目经验笔记

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    @Override
    public String userLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "userRole");
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);

        Map<String, Object> map = this.getMap(queryWrapper);
        // 用户不存在
        if (map == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        String token = JWTUtils.getToken(map);
        return token;

    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public UserVO getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        UserVO safetyUser = new UserVO();
        BeanUtils.copyProperties(originUser, safetyUser);
        if(originUser.getTags() != null){
            Gson gson = new Gson();
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> tagList = gson.fromJson(originUser.getTags(), listType);
            safetyUser.setTagList(tagList);
        }

        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public List<UserVO> searchUsersByTags(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表不能为空");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        List<User> userList = this.list(queryWrapper);
        Gson gson = new Gson();
        Type setType = new TypeToken<Set<String>>(){}.getType();

        return userList.stream().filter( user -> {
            String tags = user.getTags();
            Set<String> tagSet = gson.fromJson(tags, setType);
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
            for(String tagName : tagNameList){
                if(!tagSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());


    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        if(request == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 当前登录用户
        String token = request.getHeader("Authorization");
        Map<String, Object> userInfo = JWTUtils.verifyToken(token);

        Integer userRole = (Integer)userInfo.get("userRole");
        if(userRole != ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return true;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() != null && loginUser.getUserRole().equals(ADMIN_ROLE);
    }

    @Override
    public boolean updateUser(User user, User loginUser) {
        if(isAdmin(user) || user.getId().equals(loginUser.getId())){
            long id = user.getId();
            if(id <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            User oldUser = this.getById(id);

            if(oldUser == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改的对象不存在");
            }

            boolean result = new LambdaUpdateChainWrapper<>(userMapper)
                    .eq(User::getId, user.getId())
                    .set(user.getUsername() != null, User::getUsername, user.getUsername())
                    .set(user.getUserAccount() != null , User::getUserAccount, user.getUserAccount())
                    .set(user.getAvatarUrl() != null , User::getAvatarUrl, user.getAvatarUrl())
                    .set(user.getGender() != null, User::getGender, user.getGender())
                    .set(user.getUserPassword() != null , User::getUserPassword, user.getUserPassword())
                    .set(user.getQq() != null , User::getQq, user.getQq())
                    .set(user.getPhone() != null , User::getPhone, user.getPhone())
                    .set(user.getEmail() != null, User::getEmail, user.getEmail())
                    .set(user.getUserStatus() != null, User::getUserStatus, user.getUserStatus())
                    .set(user.getCreateTime() != null, User::getCreateTime, user.getCreateTime())
                    .set(user.getUpdateTime() != null, User::getUpdateTime, user.getUpdateTime())
                    .set(user.getIsDelete() != null, User::getIsDelete, user.getIsDelete())
                    .set(user.getUserRole() != null, User::getUserRole, user.getUserRole())
                    .set(user.getTags() != null , User::getTags, user.getTags())
                    .set(user.getUserDescription() != null , User::getUserDescription, user.getUserDescription())
                    .update();
            return result;

        }

        throw new BusinessException(ErrorCode.NO_AUTH);
    }

    @Override
    public List<UserVO> recommendUsers(int num, User loginUser) {

        // 未登录时
        if(loginUser == null){
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.last(String.format("limit %s", num));
            List<User> userList = this.list(wrapper);
            ArrayList<UserVO> userVOList = new ArrayList<>();
            for (User user: userList) {
                UserVO safetyUser = getSafetyUser(user);
                userVOList.add(safetyUser);

            }

            return userVOList;
        }


        // 登录时
        Gson gson = new Gson();
        Type setType = new TypeToken<Set<String>>(){}.getType();
        Set<String> tagSet = gson.fromJson(loginUser.getTags(), setType);

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String redisKey = String.format("friend:user:recommend:%s", loginUser.getId());

        // 查看 redis 缓存中有没有数据
        if(redisTemplate != null && redisTemplate.hasKey(redisKey)){
            return (List<UserVO>) valueOperations.get(redisKey);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        List<User> userList = this.list(queryWrapper);
        PriorityQueue<UserWithScore> pq = new PriorityQueue<>(new UserWithScoreComparator());


        for (User user : userList) {
            if(user.getId().equals(loginUser.getId())){
                continue;
            }
            UserWithScore userWithScore = new UserWithScore();
            userWithScore.setId(user.getId());
            Set<String> tmptagSet = gson.fromJson(user.getTags(), setType);
            userWithScore.setScore(getSimilarity(tagSet, tmptagSet));

            if(pq.size() < num){
                pq.add(userWithScore);
            }
            else if(userWithScore.getScore() > pq.peek().getScore()){
                pq.add(userWithScore);
                pq.poll();
            }
        }

        List<Long> ids = new ArrayList<>();
        while (!pq.isEmpty()) {
//            UserWithScore u = pq.poll();
//            System.out.println(u.getId() + ":" + u.getScore());

            ids.add(0, pq.poll().getId());
        }

        ArrayList<UserVO> userVOList = new ArrayList<>();
        for (Long id : ids) {
            User user = this.getById(id);
            UserVO userVO = getSafetyUser(user);
            userVOList.add(userVO);

        }

        valueOperations.set(redisKey, userVOList, 20, TimeUnit.MINUTES);
        return userVOList;


    }


    private int getSimilarity(Set<String> tag1, Set<String> tag2){
        int score = 0;
        if(tag1 == null || tag2 == null){
            return 0;
        }
        for (String s : tag2) {
            if(tag1.contains(s)){
                score++;
            }
        }
        return score;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            throw  new BusinessException(ErrorCode.NOT_LOGIN);
        }

        String token = request.getHeader("Authorization");
        Map<String, Object> userInfo = JWTUtils.verifyToken(token);

        if(userInfo == null){
            return null;
        }



        return this.getById((Integer)userInfo.get("id"));
    }

    @Override
    public boolean uploadAvatar(MultipartFile avatar, User loginUser) {
        if(avatar == null || loginUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long userId = loginUser.getId();
        User originUser = this.getById(userId);
        if(originUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录用户不存在");
        }


        String url = AliOSSUtils.uploadFile(avatar);


        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAvatarUrl(url);

        this.updateById(updateUser);

        String originUrl = originUser.getAvatarUrl();
        AliOSSUtils.deleteFile(originUrl);

        return true;

    }

    @Override
    public List<UserVO> queryUsersbyTeamId(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id不正确");
        }

        Team team = teamMapper.selectById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍不存在");
        }

        Long createUserId = team.getUserId();

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId).select("userId");
        List<Map<String, Object>> mapList = userTeamService.listMaps(userTeamQueryWrapper);
        List<Long> userId = mapList.stream().map(map -> (Long) map.get("userId")).collect(Collectors.toList());

        List<User> userList = this.query().in("id", userId).list();


        int j = 0; // 队长的下标
        for (int i = 0; i < userList.size(); i++) {
            if(userList.get(i).getId().equals(createUserId)){
                j = i;
                break;
            }
        }

        if(j != 0){
            User tmp = userList.get(0);
            userList.set(0, userList.get(j));
            userList.set(j, tmp);
        }


        List<UserVO> userVOList = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        return userVOList;
    }

    @Override
    public List<UserVO> queryUsers(UserQueryDTO userQueryDTO, User loginUser) {
        String searchText = userQueryDTO.getSearchText();
        Long id = userQueryDTO.getId();
        String username = userQueryDTO.getUsername();
        String userDescription = userQueryDTO.getUserDescription();
        String userAccount = userQueryDTO.getUserAccount();
        Integer gender = userQueryDTO.getGender();
        String qq = userQueryDTO.getQq();
        String phone = userQueryDTO.getPhone();
        String email = userQueryDTO.getEmail();
        Integer userStatus = userQueryDTO.getUserStatus();
//        Date createTime = userQueryDTO.getCreateTime();
//        Date updateTime = userQueryDTO.getUpdateTime();
        Integer userRole = userQueryDTO.getUserRole();
        String[] tagList = userQueryDTO.getTagList();



        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if(loginUser != null){
            wrapper.ne("id", loginUser.getId());
        }

        wrapper.eq(id != null, "id", id).eq(StringUtils.isNotBlank(userAccount), "userAccount", userAccount).eq(gender != null, "gender", gender)
                .eq(userStatus != null, "userStatus", userStatus).eq(userRole != null, "userRole", userRole);

        wrapper.like(StringUtils.isNotBlank(username), "username", username).like(StringUtils.isNotBlank(userDescription), "userDescription", userDescription).like(StringUtils.isNotBlank(qq), "qq", qq)
                .like(StringUtils.isNotBlank(phone), "phone", phone).like(StringUtils.isNotBlank(email), "email", email);

        wrapper.and(StringUtils.isNotBlank(searchText), (qw) -> qw.like("username", searchText).or().like("userDescription", searchText).or().like("tags", searchText));

        List<User> userList = this.list(wrapper);


        if(tagList == null){
            return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
        }

        // 用户必须拥有查询的所有标签
        Gson gson = new Gson();
        Type setType = new TypeToken<Set<String>>(){}.getType();

        return userList.stream().filter( user -> {
            String tags = user.getTags();
            Set<String> tagSet = gson.fromJson(tags, setType);
            tagSet = Optional.ofNullable(tagSet).orElse(new HashSet<>());
            for(String tagName : tagList){
                if(!tagSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

}

