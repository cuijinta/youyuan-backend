package com.qianye.youyuan.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 浅夜
 * @Description 聊天消息响应对象
 * @DateTime 2024/4/11 16:32
 **/
@Data
public class MessageVo implements Serializable {
    private static final long serialVersionUID = 574796068010177193L;
    private WebSocketVo formUser;
    private WebSocketVo toUser;
    private Long teamId;
    private String text;
    private Boolean isMy = false;
    private Integer chatType;
    private Boolean isAdmin = false;
    private String createTime;
}
