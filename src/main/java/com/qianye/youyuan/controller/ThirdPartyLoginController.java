package com.qianye.youyuan.controller;


import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.QQLoginRequest;
import com.qianye.youyuan.service.ThirdPartyLoginService;
import com.qianye.youyuan.utils.ResultUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.qianye.youyuan.constant.UserConstant.USER_LOGIN_STATUS;

/**
 * @Author 浅夜
 * @Description
 * @DateTime 2024/4/11 13:40
 **/
@RestController
@RequestMapping("login")
public class ThirdPartyLoginController {
    @Resource
    private ThirdPartyLoginService thirdPartyLoginService;

    @GetMapping("qq")
    public Result<String> qqLogin() throws IOException {
        String url = thirdPartyLoginService.qqLogin();
        return ResultUtils.success(url);
    }

    @PostMapping("loginInfo")
    public Result<User> saveLoginInfo(@RequestBody QQLoginRequest qqLoginRequest, HttpServletRequest request) throws IOException {
        if (qqLoginRequest == null || StringUtils.isBlank(qqLoginRequest.getCode())) {
            throw new GlobalException(ErrorCode.NOT_LOGIN, "请重新登录");
        }
        User user = thirdPartyLoginService.getLoginInfo(qqLoginRequest, request);
        request.getSession().setAttribute(USER_LOGIN_STATUS, user);
        return ResultUtils.success(user);
    }
}
