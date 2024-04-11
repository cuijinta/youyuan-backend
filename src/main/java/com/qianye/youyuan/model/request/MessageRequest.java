package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 浅夜
 * @Description 消息请求对象
 * @DateTime 2024/4/11 16:45
 **/
@Data
public class MessageRequest implements Serializable {
    private static final long serialVersionUID = -5819686817398955194L;
    private Long toId;
    private Long teamId;
    private String text;
    private Integer chatType;
    private boolean isAdmin;
}
