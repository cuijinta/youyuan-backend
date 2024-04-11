package com.qianye.youyuan.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @Author 浅夜
 * @Description
 * @DateTime 2024/4/11 22:51
 **/
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = -1143142100964539206L;

    private Set<TeamVO> teamSet;
}
