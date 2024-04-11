package com.qianye.youyuan.service;

import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.QQLoginRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @Author 浅夜
 * @Description 第三方登录
 * @DateTime 2024/4/11 13:41
 **/
public interface ThirdPartyLoginService {
    /**
     * 获取用户信息
     *
     * @param qqLoginRequest
     * @param request
     * @return
     * @throws
     */
    User getLoginInfo(QQLoginRequest qqLoginRequest, HttpServletRequest request) throws IOException;

    /**
     * 获取QQ登录地址
     *
     * @return
     * @throws IOException
     */
    String qqLogin() throws IOException;
}
