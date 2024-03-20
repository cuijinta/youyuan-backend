package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description todo
 * @Author qianye
 * @Date 2024/3/20 16:26
 * @Version 1.0
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -4906077805155147783L;

    private Long teamId;
}
