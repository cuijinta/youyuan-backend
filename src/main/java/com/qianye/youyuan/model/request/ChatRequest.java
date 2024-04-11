package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 浅夜
 * @Description 聊天请求对象
 * @DateTime 2024/4/11 16:40
 **/
@Data
public class ChatRequest implements Serializable {
    private static final long serialVersionUID = 7239881108063115954L;
    /**
     * 队伍聊天室id
     */
    private Long teamId;

    /**
     * 接收消息id
     */
    private Long toId;
}
