package com.qianye.youyuan.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description
 * @Author qianye
 * @Date 2024/4/3 10:32
 * @Version 1.0
 */
@Data
@AllArgsConstructor
public class FriendAddRequest implements Serializable {

    private static final long serialVersionUID = -83871917924902945L;

    private Long id;
    /**
     * 接收申请的用户id
     */
    private Long receiveId;

    /**
     * 好友申请备注信息
     */
    private String remark;
}
