package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description todo
 * @Author qianye
 * @Date 2024/3/20 10:44
 * @Version 1.0
 */
@Data
public class PageRequest implements Serializable {

    /**
     * 每页条数
     */
    protected int pageSize = 10;
    /**
     * 当前页数
     */
    protected int pageNum = 1;
}
