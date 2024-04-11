package com.qianye.youyuan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.youyuan.model.domain.Chat;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.ChatRequest;
import com.qianye.youyuan.model.vo.MessageVo;

import java.util.Date;
import java.util.List;


/**
 * 聊天消息表(Chat)表服务接口
 *
 * @author makejava
 * @since 2024-04-11 16:34:32
 */
public interface ChatService extends IService<Chat> {
    /**
     * 保存缓存
     *
     * @param redisKey
     * @param id
     * @param messageVos
     */
    void saveCache(String redisKey, String id, List<MessageVo> messageVos);

    /**
     * 获取缓存
     *
     * @param redisKey
     * @param id
     * @return
     */
    List<MessageVo> getCache(String redisKey, String id);

    /**
     * 获取私聊聊天内容
     *
     * @param chatRequest
     * @param chatType
     * @param loginUser
     * @return
     */
    List<MessageVo> getPrivateChat(ChatRequest chatRequest, int chatType, User loginUser);

    /**
     * 获取大厅聊天纪录
     *
     * @param chatType
     * @param loginUser
     * @return
     */
    List<MessageVo> getHallChat(int chatType, User loginUser);

    /**
     * 聊天记录映射
     *
     * @param fromId
     * @param toId
     * @param text
     * @param chatType
     * @param createTime
     * @return
     */
    MessageVo chatResult(Long fromId, Long toId, String text, Integer chatType, Date createTime);

    /**
     * 队伍聊天室
     *
     * @param chatRequest
     * @param chatType
     * @param loginUser
     * @return
     */
    List<MessageVo> getTeamChat(ChatRequest chatRequest, int chatType, User loginUser);


    /**
     * 删除key
     *
     * @param key
     * @param id
     */
    void deleteKey(String key, String id);
}
