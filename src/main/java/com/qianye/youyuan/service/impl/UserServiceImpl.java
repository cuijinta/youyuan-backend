package com.qianye.youyuan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qianye.youyuan.constant.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qianye.youyuan.constant.UserConstant.ADMIN_ROLE;

/**
 * @author 浅夜光芒万丈
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2023-10-30 22:41:32
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    UserMapper userMapper;

    /**
     * 盐值 ，用于混淆密码
     */
    private static final String SALT = "qianye";

    /**
     * 用户登录状态键
     */
    private static final String USER_LOGIN_STATUS = "userLoginStatus";


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param code
     * @return 新用户id
     */
    public long userRegister(String userAccount, String userPassword, String checkPassword, String code) {
        //1. 校验(采用apache commons lang依赖中的方法来一次判断多个变量是否为空)
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, code)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户名长度不能小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码长度不小于8位");
        }
        if(code.length() > 5) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户编号不能超过5位");
        }

        //账户不能包含特殊字符
        String regEx = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (matcher.find()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户名不能包含特殊字符");
        }

        //保持两次密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }

        //账户不能重复（写在校验特殊字符逻辑之后，减小性能开销）
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }

        //用户编号不能重复
        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("code", code);
        count = userMapper.selectCount(queryWrapper1);
        if(count >0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "编号不能重复");
        }

        //2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setCode(code);
        boolean isSave = this.save(user);

        if (!isSave) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  账户名
     * @param userPassword 账户密码
     * @return 返回脱敏后的用户信息
     */
    @Override
    public User doLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1. 校验(采用apache commons lang依赖中的方法来一次判断多个变量是否为空)
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "账户名长度不能小于4位");
        }
        if (userPassword.length() < 8) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "密码长度不小于8位");
        }

        //账户不能包含特殊字符
        String regEx = "\\pP|\\pS|\\s+";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (matcher.find()) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户名不能包含特殊字符");
        }

        //1. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //2.查询是否存在响应的用户
        QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("userAccount", userAccount);
        queryWrapper1.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper1);
        if (user == null) { //用户不存在
            log.info("user login failed, userAccount cannot match userPassword");
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        //3.用户信息脱敏
        User safetyUser = getSafetyUser(user);

        //4.记录用户的用户态
        request.getSession().setAttribute(USER_LOGIN_STATUS, safetyUser);
        return safetyUser;
    }

    /**
     * 按标签名查找用户 (内存过滤)
     * @param tagNameList 标签列表
     * @return 包含所有标签的用户
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {

        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     *  用户信息修改
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user,User loginUser) {
        long userId = user.getId();
        if (userId <= 0){
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        //如果是管理员，允许更新任意用户
        //如果不是管理员，只允许更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        User user1 = userMapper.selectById(userId);
        if (user1 == null){
            throw new GlobalException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     *  获取当前用户信息
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (userObj == null){
            throw new GlobalException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 根据标签搜索用户（SQL 查询）
     * @param tagNameList 标签列表
     * @return 包含所有标签的用户
     */
    @Deprecated
    private List<User> searchUserBySQL(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }

        //方式1：sql查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接查询sql
        for(String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);

        return userList;
    }

    /**
     * 用户脱敏
     *
     * @param user
     * @return
     */
    @Override
    public User getSafetyUser(User user) {
        if(user == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setCode(user.getCode());
        safetyUser.setTags(user.getTags());
        safetyUser.setProfile(user.getProfile());

        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @return 1表示注销成功
     */
    @Override
    public Integer userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATUS);
        return 1;
    }
}




