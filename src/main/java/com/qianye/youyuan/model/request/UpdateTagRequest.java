package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @Author 浅夜
 * @Description 更新标签请求对象
 * @DateTime 2024/4/11 18:49
 **/
@Data
public class UpdateTagRequest implements Serializable {
    private static final long serialVersionUID = 3208537167994563404L;
    private long id;
    private Set<String> tagList;
}
