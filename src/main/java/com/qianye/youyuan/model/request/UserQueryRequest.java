package com.qianye.youyuan.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserQueryRequest implements Serializable {

    private static final long serialVersionUID = 8245489534534574432L;
    /**
     * 查询用户
     */
    private String searchText;
}