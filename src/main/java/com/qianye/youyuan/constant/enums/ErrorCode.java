package com.qianye.youyuan.constant.enums;

import lombok.Getter;

/**
 * @Author 浅夜
 * @Description 状态码
 * @DateTime 2023/12/21 23:36
 **/
@Getter
public enum ErrorCode {

    SUCCESS(20000, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),
    FILE_TYPE_ERROR(50005, "文件类型异常", "类型不允许"),
    FILE_UPLOAD_ERROR(50006, "文件上传失败", "上传失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
