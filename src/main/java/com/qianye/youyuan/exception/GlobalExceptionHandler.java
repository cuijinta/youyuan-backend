package com.qianye.youyuan.exception;

import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author 浅夜
 * @Description 全局异常处理器
 * @DateTime 2023/12/23 0:11
 **/
@Slf4j
@RestControllerAdvice //spring 的切面功能
public class GlobalExceptionHandler {
    @ExceptionHandler(GlobalException.class) //指定方法要捕获的异常
    public Result<?> globalExceptionHandler(GlobalException exception) {
        log.error("globalException" + exception.getMessage (), exception);
        Result<?> result = ResultUtils.error(exception.getCode(), exception.getMessage(), exception.getDescription());
        return result;
}

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "未知异常，请联系管理员");
    }
}
