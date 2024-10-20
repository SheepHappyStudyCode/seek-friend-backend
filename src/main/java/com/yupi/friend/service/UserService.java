package com.yupi.friend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.friend.model.dto.UserQueryDTO;
import com.yupi.friend.model.entity.User;
import com.yupi.friend.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    String userLogin(String userAccount, String userPassword);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    UserVO getSafetyUser(User originUser);

    List<UserVO> searchUsersByTags(List<String> tagNameList);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    boolean isAdmin(User loginUser);


    boolean updateUser(User user, User loginUser);

    List<UserVO> recommendUsers(int num, User loginUser);

    User getLoginUser(HttpServletRequest request);

    boolean uploadAvatar(MultipartFile avatar, User loginUser);

    List<UserVO> queryUsersbyTeamId(Long id);

    List<UserVO> queryUsers(UserQueryDTO userQueryDTO, User loginUser);

    UserVO queryById(Long id);

     List<Long> getRecommendUserIds(Long userId);
}
