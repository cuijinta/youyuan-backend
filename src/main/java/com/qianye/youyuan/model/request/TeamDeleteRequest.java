package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description 删除队伍请求对象
 * @Author qianye
 * @Date 2024/3/20 15:11
 * @Version 1.0
 */
@Data
public class TeamDeleteRequest implements Serializable {

    private static final long serialVersionUID = -156075716907849068L;
    private Long id;
}
