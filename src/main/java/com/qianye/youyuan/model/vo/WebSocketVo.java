package com.qianye.youyuan.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 浅夜
 * @Description 用户聊天响应对象
 * @DateTime 2024/4/11 16:49
 **/
@Data
public class WebSocketVo implements Serializable {
    private static final long serialVersionUID = -832886173112068331L;

    private long id;
    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
