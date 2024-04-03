package com.qianye.youyuan.model.vo;

import com.qianye.youyuan.model.domain.User;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description 好友申请响应对象
 * @Author qianye
 * @Date 2024/4/3 11:08
 * @Version 1.0
 */
@Data
public class FriendsRecordVO implements Serializable {
    private static final long serialVersionUID = 5983383290260035611L;

    private Long id;

    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-已过期）
     */
    private Integer status;

    /**
     * 好友申请备注信息
     */
    private String remark;

    /**
     * 申请用户
     */
    private User applyUser;
}
