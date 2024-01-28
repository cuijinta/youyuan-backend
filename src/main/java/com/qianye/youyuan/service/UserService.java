package com.qianye.youyuan.service;

import com.qianye.youyuan.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 浅夜光芒万丈
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-10-30 22:41:32
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param code
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String code);

    /**
     * 用户登录
     *
     * @param userAccount 账户名
     * @param userPassword 账户密码
     * @return 返回脱敏后的用户信息
     */
    User  doLogin(String userAccount, String userPassword, HttpServletRequest request);

    List<User> searchUserByTags(List<String> tagNameList);

    /**
     * 用户数据脱敏
     *
     * @param user
     * @return
     */
    public User getSafetyUser(User user);

    /**
     * 用户注销
     *
     * @return
     */
    Integer userLogout(HttpServletRequest request);
}
