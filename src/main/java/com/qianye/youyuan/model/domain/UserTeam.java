package com.qianye.youyuan.model.domain;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户队伍关系(UserTeam)表实体类
 *
 * @author makejava
 * @since 2024-03-20 10:26:43
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTeam{
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 用户id
    private Long userId;
    // 队伍id
    private Long teamId;
    // 加入时间
    private Date joinTime;
    // 创建时间
    private Date createTime;

    private Date updateTime;
    // 是否删除
    @TableLogic
    private Integer isDelete;

}

