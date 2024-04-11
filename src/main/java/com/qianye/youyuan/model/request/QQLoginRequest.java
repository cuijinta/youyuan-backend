package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 浅夜
 * @Description qq登录请求对象
 * @DateTime 2024/4/11 13:45
 **/
@Data
public class QQLoginRequest implements Serializable {
    private static final long serialVersionUID = -958164653280463444L;
    private String code;
}