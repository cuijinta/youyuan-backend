package com.qianye.youyuan.model.vo;

import com.qianye.youyuan.model.domain.User;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * 队伍和用户信息封装类（脱敏）
 *
 * @author yupi
 */
@Data
public class TeamVO implements Serializable {

    private static final long serialVersionUID = 1899063007109226944L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    private String password;

    /**
     * 描述
     */
    private String description;

    private String teamAvatarUrl;
    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private Long userId;

    // 创建人用户信息
    private User User;

    // 已加入的用户数
    private Integer hasJoinNum;

    // 是否已加入队伍
    private boolean hasJoin = false;
    /**
     * 公告
     */
    private String announce;

    private Set<User> userSet;
}