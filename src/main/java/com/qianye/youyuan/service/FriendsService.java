package com.qianye.youyuan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qianye.youyuan.model.domain.Friends;
import com.qianye.youyuan.model.domain.User;
import com.qianye.youyuan.model.request.FriendAddRequest;
import com.qianye.youyuan.model.vo.FriendsRecordVO;

import java.util.List;
import java.util.Set;

/**
 * 好友申请管理表(Friends)表服务接口
 *
 * @author makejava
 * @since 2024-04-03 10:26:13
 */
public interface FriendsService extends IService<Friends> {

    /**
     * 发起申请
     * @param loginUser 请求用户
     * @param friendAddRequest 请求体
     * @return
     */
    boolean addFriendRecords(User loginUser, FriendAddRequest friendAddRequest);

    /**
     * 获取请求记录
     * @param loginUser 请求用户
     * @return
     */
    List<FriendsRecordVO> obtainFriendApplicationRecords(User loginUser);

    /**
     * 获取申请数
     * @param loginUser 请求用户
     * @return
     */
    int getRecordCount(User loginUser);

    /**
     * 查看我发起的申请
     * @param loginUser 请求用户
     * @return
     */
    List<FriendsRecordVO> getMyRecords(User loginUser);

    /**
     * 取消申请
     * @param id 申请id
     * @param loginUser 请求用户
     * @return
     */
    boolean canceledApply(Long id, User loginUser);

    boolean toRead(User loginUser, Set<Long> ids);

    /**
     * 同意申请
     *
     * @param loginUser 当前用户
     * @param fromId 请求用户
     * @return
     */
    boolean agreeToApply(User loginUser, Long fromId);
}

