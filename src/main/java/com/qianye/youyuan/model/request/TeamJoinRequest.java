package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description todo
 * @Author qianye
 * @Date 2024/3/20 15:11
 * @Version 1.0
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -1663024892135656746L;

    private Long teamId;
    private String password;
}
