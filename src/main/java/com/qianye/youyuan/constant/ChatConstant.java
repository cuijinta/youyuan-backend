package com.qianye.youyuan.constant;

/**
 * @Author 浅夜
 * @Description TODO
 * @DateTime 2024/4/11 16:47
 **/
public interface ChatConstant {
    /**
     * 私聊
     */
    int PRIVATE_CHAT = 1;
    /**
     * 队伍群聊
     */
    int TEAM_CHAT = 2;
    /**
     * 大厅聊天
     */
    int HALL_CHAT = 3;

    String CACHE_CHAT_HALL = "youyuan:c1hat:chat_records:chat_hall";

    String CACHE_CHAT_PRIVATE = "youyuan:chat:chat_records:chat_private";

    String CACHE_CHAT_TEAM = "youyuan:chat:chat_records:chat_team";


}
