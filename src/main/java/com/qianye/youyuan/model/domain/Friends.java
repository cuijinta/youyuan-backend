package com.qianye.youyuan.model.domain;

import java.util.Date;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 好友申请管理表(Friends)表实体类
 *
 * @author makejava
 * @since 2024-04-03 10:19:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("friends")
public class Friends implements Serializable {
    private static final long serialVersionUID = 7320238049147676905L;
    //好友申请id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //发送申请的用户id
    private Long fromId;
    //接收申请的用户id
    private Long receiveId;
    //是否已读(0-未读 1-已读)
    private Integer isRead;
    //申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）
    private Integer status;
    //创建时间
    private Date createTime;

    private Date updateTime;
    //是否删除
    private Integer isDelete;
    //好友申请备注信息
    private String remark;
}

