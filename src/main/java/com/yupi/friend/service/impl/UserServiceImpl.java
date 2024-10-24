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
import com.yupi.friend.model.message.CacheUpdateMessage;
import com.yupi.friend.model.vo.UserVO;
import com.yupi.friend.mq.CacheUpdateProducer;
import com.yupi.friend.service.UserService;
import com.yupi.friend.service.UserTeamService;
import com.yupi.friend.utils.AliOSSUtils;
import com.yupi.friend.utils.HashUtils;
import com.yupi.friend.utils.JWTUtils;
import com.yupi.friend.utils.RedisCacheClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yupi.friend.constant.RabbitConstant.USER_CACHE_QUEUE;
import static com.yupi.friend.constant.RedisConstant.*;
import static com.yupi.friend.constant.UserConstant.ADMIN_ROLE;
import static com.yupi.friend.constant.UserConstant.DEFAULT_RECOMMEND_NUM;

/**
 * 用户服务实现类
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private RedisCacheClient redisCacheClient;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Resource
    private TeamMapper teamMapper;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private CacheUpdateProducer cacheUpdateProducer;

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
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        byte[] skey = new byte[0];
        byte[] encryptPassword = new byte[0];
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacMD5");
            SecretKey key = keyGen.generateKey();
            skey = key.getEncoded();
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);
            mac.update(userPassword.getBytes("UTF-8"));
            encryptPassword = mac.doFinal();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }


        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(HashUtils.hexToStr(encryptPassword));
        user.setSecretKey(HashUtils.hexToStr(skey));

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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号太短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码太短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "userRole", "secretKey", "userPassword");
        queryWrapper.eq("userAccount", userAccount);
        Map<String, Object> map = this.getMap(queryWrapper);
        // 用户不存在
        if (map == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }


        // 3. 检验密码是否正确
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        byte[] result;
        try {
            byte[] hkey = HashUtils.strToHex((String)map.get("secretKey"));
            SecretKey key = new SecretKeySpec(hkey, "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(key);
            mac.update(userPassword.getBytes("UTF-8"));
            result = mac.doFinal();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if(!HashUtils.hexToStr(result).equals(map.get("userPassword"))){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        Map<String, Object> jwtMap = new HashMap<>();
        jwtMap.put("id", map.get("id"));
        jwtMap.put("userRole", map.get("userRole"));

        String token = JWTUtils.getToken(jwtMap);
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

    /**
     *
     * @param user 要修改的用户信息
     * @param loginUser 登录用户信息
     * @return 操作成功或失败
     */
    @Override
    public boolean updateUser(User user, User loginUser) {
        if(isAdmin(loginUser) || user.getId().equals(loginUser.getId())){
            long id = user.getId();
            if(id <= 0){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }

            UserVO oldUser = this.queryById(id);

            if(oldUser == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改的对象不存在");
            }

            boolean result = new LambdaUpdateChainWrapper<>(userMapper)
                    .eq(User::getId, id)
                    .set(user.getUsername() != null, User::getUsername, user.getUsername())
                    .set(user.getAvatarUrl() != null , User::getAvatarUrl, user.getAvatarUrl())
                    .set(user.getGender() != null, User::getGender, user.getGender())
                    .set(user.getQq() != null , User::getQq, user.getQq())
                    .set(user.getPhone() != null , User::getPhone, user.getPhone())
                    .set(user.getEmail() != null, User::getEmail, user.getEmail())
                    .set(user.getUserStatus() != null, User::getUserStatus, user.getUserStatus())
                    .set(user.getUserRole() != null, User::getUserRole, user.getUserRole())
                    .set(user.getTags() != null , User::getTags, user.getTags())
                    .set(user.getUserDescription() != null , User::getUserDescription, user.getUserDescription())
                    .update();
            if(result){
                // 缓存同步
                user = query().eq("id", id).one();
                UserVO safetyUser = getSafetyUser(user);
//                if(user == null){
//                    return false;
//                }
                String key = USER_ID_KEY + id;
                redisCacheClient.setHashObject(key, safetyUser, USER_TTL, TimeUnit.MINUTES);

                CacheUpdateMessage msg = new CacheUpdateMessage();
                msg.setKey("recommend");
                msg.setValue(id);
                cacheUpdateProducer.sendCacheUpdateMessage(msg, USER_CACHE_QUEUE);
            }



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

        String recommendKey = USER_RECOMMEND_KEY + loginUser.getId();
         // 查看 redis 缓存中有没有数据
        if(redisTemplate.hasKey(recommendKey)){
            stringRedisTemplate.expire(recommendKey, USER_RECOMMEND_TTL, TimeUnit.MINUTES);

            List<String> range = stringRedisTemplate.opsForList().range(recommendKey, 0, num - 1);
            List<Long> ids = range.stream().map(Long::parseLong).collect(Collectors.toList());
            List<String> keyList = ids.stream().map(id -> USER_ID_KEY + id).collect(Collectors.toList());
            List<UserVO> userVOList = redisCacheClient.multiGetHashObject(keyList, UserVO.class, USER_TTL, TimeUnit.MINUTES);

            if(userVOList.size() >= num){
                return userVOList;
            }

            // 缓存数据不够，查询数据库
            // 批量查询
            String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
            List<User> userList = query().in("id", ids).last("ORDER BY FIELD(id, " + idStr + ")").list();
            userVOList = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
            // 写入缓存
            redisCacheClient.multiSetHashObject(keyList, userVOList, USER_TTL, TimeUnit.MINUTES);
            return userVOList;


        }

        // 寻找相似度最高的前 n 个人
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

        List<Long> ids = new ArrayList<>(num);
        while (!pq.isEmpty()) {
            ids.add(0, pq.poll().getId());
        }

        // 批量查询
        String idStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        userList = query().in("id", ids).last("ORDER BY FIELD(id, " + idStr + ")").list();
        List<UserVO> userVOList = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

        // 写入缓存
        // 1. 缓存推荐用户的 ids
        stringRedisTemplate.opsForList().rightPushAll(recommendKey, ids.stream().map(String::valueOf).collect(Collectors.toList()));
        stringRedisTemplate.expire(recommendKey, USER_RECOMMEND_TTL, TimeUnit.MINUTES);
        List<String> keyList = new ArrayList<>(userVOList.size());
        for (UserVO userVO : userVOList) {
            keyList.add(USER_ID_KEY + userVO.getId());
        }

        // 2. 将相应 id 的用户存入缓存
        redisCacheClient.multiSetHashObject(keyList, userVOList, USER_TTL, TimeUnit.MINUTES);

        return userVOList;


    }

    public List<Long> getRecommendUserIds(Long userId){
        User loginUser = getById(userId);
        Gson gson = new Gson();
        Type setType = new TypeToken<Set<String>>(){}.getType();
        Set<String> tagSet = gson.fromJson(loginUser.getTags(), setType);

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

            if(pq.size() < DEFAULT_RECOMMEND_NUM){
                pq.add(userWithScore);
            }
            else if(userWithScore.getScore() > pq.peek().getScore()){
                pq.add(userWithScore);
                pq.poll();
            }
        }

        List<Long> ids = new ArrayList<>(DEFAULT_RECOMMEND_NUM);
        while (!pq.isEmpty()) {
            ids.add(0, pq.poll().getId());
        }
        return ids;
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
        UserVO originUser = this.queryById(userId);
        if(originUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录用户不存在");
        }

        String url = AliOSSUtils.uploadFile(avatar);

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAvatarUrl(url);

        this.updateUser(updateUser, loginUser);

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

    @Override
    public UserVO queryById(Long id) {
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询的用户id异常");
        }

        String key = USER_ID_KEY + id;
        UserVO userVO = redisCacheClient.getHashObject(key, UserVO.class);

        if(userVO != null){
            // 缓存存在，刷新缓存
            redisCacheClient.expireKey(key, USER_TTL, TimeUnit.MINUTES);
            return userVO;
        }

        User user = getById(id);
        UserVO safetyUser = getSafetyUser(user);
        if(safetyUser != null){
            redisCacheClient.setHashObject(key, safetyUser, USER_TTL, TimeUnit.MINUTES);
            return safetyUser;
        }

        return null;
    }

}

