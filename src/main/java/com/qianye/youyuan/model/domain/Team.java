package com.qianye.youyuan.model.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 队伍(Team)表实体类
 *
 * @author makejava
 * @since 2024-03-20 10:24:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    // id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 队伍名称
    private String name;
    // 描述
    private String description;

    private String teamAvatarUrl;
    // 最大人数
    private Integer maxNum;
    // 过期时间
    private Date expireTime;
    // 创建人 id
    private Long userId;

    // 0 - 公开，1 - 私有，2 - 加密
    private Integer status;
    // 密码
    private String password;
    // 创建时间
    private Date createTime;

    private Date updateTime;
    // 是否删除
    @TableLogic
    private Integer isDelete;
    /**
     * 公告
     */
    private String announce;

}

