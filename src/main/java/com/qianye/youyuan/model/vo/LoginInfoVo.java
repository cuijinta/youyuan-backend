package com.qianye.youyuan.model.vo;

import lombok.Data;

/**
 * @Author 浅夜
 * @Description qq登录响应对象
 * @DateTime 2024/4/11 14:45
 **/
@Data
public class LoginInfoVo {
    private String social_uid;
    private String faceImg;
    private String nickname;
    private Integer code;
    private String gender;
}
