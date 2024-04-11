package com.qianye.youyuan.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.UpdateTagRequest;
import com.qianye.youyuan.model.request.UserLoginRequest;
import com.qianye.youyuan.model.request.UserQueryRequest;
import com.qianye.youyuan.model.request.UserRegisterRequest;
import com.qianye.youyuan.service.UserService;
import com.qianye.youyuan.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qianye.youyuan.constant.UserConstant.ADMIN_ROLE;
import static com.qianye.youyuan.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @Author 浅夜
 * @Description 控制层
 * @DateTime 2023/11/15 23:02
 **/
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"}) //配置跨域
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String code = userRegisterRequest.getCode();
        if (StringUtils.isAllBlank(userAccount, userPassword, checkPassword, code)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, code);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public Result<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAllBlank(userAccount, userPassword)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户用或密码错误");
        }
        User user = userService.doLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Result<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN);
        }
        Integer userLogout = userService.userLogout(request);
        return ResultUtils.success(userLogout);
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable("id") Integer id) {
        if (id == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getSafetyUser(this.userService.getById(id));
        return ResultUtils.success(user);
    }

    /**
     * 获取当前用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public Result<User> currentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new GlobalException(ErrorCode.NOT_LOGIN, "请先登录");
        }
        long userId = currentUser.getId();
        //todo: 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 管理员根据用户名搜索用户
     *
     * @param userQueryRequest 用户名
     * @param request  请求体
     * @return
     */
    @PostMapping("/search")
    public Result<List<User>> userQuery(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        List<User> users = userService.userQuery(userQueryRequest, request);
        return ResultUtils.success(users);
    }

    @GetMapping("/search")
    public Result<List<User>> searchList(HttpServletRequest request) {
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
        if (loginUser != null) {
            List<User> userList = (List<User>) valueOperations.get(userService.redisFormat(loginUser.getId()));
            if (userList != null) {
                // 打乱并固定第一个用户
                return ResultUtils.success(fixTheFirstUser(userList));
            }
        } else {
            List<User> userList = (List<User>) valueOperations.get("youyuan:user:notLogin");
            if (userList != null) {
                // 打乱并固定第一个用户
                return ResultUtils.success(fixTheFirstUser(userList));
            }
        }
        List<User> result = null;
        try {
            if (loginUser != null) {
                List<User> userList = userService.list();
                // 打乱并固定第一个用户
                result = fixTheFirstUser(userList).stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                redisTemplate.opsForValue().set(userService.redisFormat(loginUser.getId()), result, 1 + RandomUtil.randomInt(1, 2) / 10, TimeUnit.MINUTES);
            } else {
                // 未登录只能查看20条
                Page<User> userPage = new Page<>(1, 30);
                LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
                Page<User> page = userService.page(userPage, userLambdaQueryWrapper);
                result = fixTheFirstUser(page.getRecords()).stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
                redisTemplate.opsForValue().set("youyuan:user:notLogin", result, 10, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(result);
    }

    private List<User> fixTheFirstUser(List<User> userList) {
        // 取出第一个元素
        User firstUser = userList.get(0);
        // 将剩下的元素打乱顺序
        userList = userList.subList(1, userList.size());
        Collections.shuffle(userList);
        userList.add(0, firstUser);
        return userList;
    }

    /**
     * 根据id删除用户（逻辑删除）
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        //鉴权，不是管理员不能删除
        if (!isAdmin(request)) {
            throw new GlobalException(ErrorCode.NO_AUTH);
        }

        if (id <= 0) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }

        boolean isRemove = userService.removeById(id);
        return ResultUtils.success(isRemove);
    }

    /**
     * 根据标签列表获取用户列表
     *
     * @param tagNameList 标签列表
     * @return
     */
    @GetMapping("/search/tags")
    public Result<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 根据标签列表获取用户列表
     *
     * @param pageSize 每页条数
     * @param pageNum 当前页数
     * @return
     */
    @GetMapping("/recommend")
    public Result<Page<User>> recommendUsers(HttpServletRequest request, long pageNum, long pageSize) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userList = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(userList);
    }

    /**
     * 用户信息更新
     *
     * @param user
     * @param request
     * @return
     */
    @PostMapping("/update")
    public Result<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateId = userService.updateUser(user, currentUser);
        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
        return ResultUtils.success(updateId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        //鉴权，仅管理员可查
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATUS);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取最匹配用户列表
     * @param num 数量
     * @param request
     * @return
     */
    @GetMapping("/match")
    public Result<List<User>> matchUsers(long num, HttpServletRequest request) {
        if(num <= 0 || num > 20) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, user));
    }

    @GetMapping("/friends")
    public Result<List<User>> getFriends(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        List<User> getUser = userService.getFriendsById(currentUser);
        return ResultUtils.success(getUser);
    }

    @PostMapping("/deleteFriend/{id}")
    public Result<Boolean> deleteFriend(@PathVariable("id") Long id, HttpServletRequest request) {
        if (id == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "好友不存在");
        }
        User currentUser = userService.getLoginUser(request);
        boolean deleteFriend = userService.deleteFriend(currentUser, id);
        return ResultUtils.success(deleteFriend);
    }

    @PostMapping("/searchFriend")
    public Result<List<User>> searchFriend(@RequestBody UserQueryRequest userQueryRequest, HttpServletRequest request) {
        if (userQueryRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        User currentUser = userService.getLoginUser(request);
        List<User> searchFriend = userService.searchFriend(userQueryRequest, currentUser);
        return ResultUtils.success(searchFriend);
    }

    @PostMapping("/update/tags")
    public Result<Integer> updateTagById(@RequestBody UpdateTagRequest tagRequest, HttpServletRequest request) {
        if (tagRequest == null) {
            throw new GlobalException(ErrorCode.PARAMS_ERROR);
        }
        User currentUser = userService.getLoginUser(request);
        int updateTag = userService.updateTagById(tagRequest, currentUser);
        redisTemplate.delete(userService.redisFormat(currentUser.getId()));
        return ResultUtils.success(updateTag);
    }

}
