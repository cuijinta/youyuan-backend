package com.qianye.youyuan.model.domain;

import java.util.Date;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 聊天消息表(Chat)表实体类
 *
 * @author makejava
 * @since 2024-04-11 16:34:32
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("chat")
public class Chat implements Serializable{
    private static final long serialVersionUID = -350020095775751061L;
    //聊天记录id
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //发送消息id
    private Long fromId;
    //接收消息id
    private Long toId;

    private String text;
    //聊天类型 1-私聊 2-群聊
    private Integer chatType;
    //创建时间
    private Date createTime;

    private Date updateTime;

    private Long teamId;


}
