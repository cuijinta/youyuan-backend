package com.qianye.youyuan.model;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     *
     */
    @TableId
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 用户性别
     */
    private Integer gender;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     *  用户状态 0 - 正常 
     */
    private Integer userStatus;

    /**
     * 用户角色 0 - 普通  1 - 管理员
     */
    private Integer userRole;

    /**
     * 是否删除
     */
    @TableLogic //表示该字段是逻辑删除字段，对应配置文件中要有说明
    private Integer isDelete;

    /**
     * 邮箱
     */
    private String email;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户编号
     */
    private String code;

    /**
     * 标签 json 列表
     */
    private String tags;

    /**
     * 个人简介
     */
    private String profile;
}