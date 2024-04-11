package com.qianye.youyuan.model.vo;

import lombok.Data;

/**
 * @Author 浅夜
 * @Description qq登录请求对象
 * @DateTime 2024/4/11 14:48
 **/
@Data
public class QQLoginVo {
    private Integer code;
    private String msg;
    private String type;
    private String url;
}
