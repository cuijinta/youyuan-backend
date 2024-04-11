package com.qianye.youyuan.controller;

import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.service.UploadService;
import com.qianye.youyuan.utils.ResultUtils;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @Description 文件上传控制器
 * @Author qianye
 * @Date 2024/2/20 13:41
 * @Version 1.0
 */
@RestController
@RequestMapping("/file")
@Api(tags = "文件上传相关")
public class UploadController {

    @Resource
    private UploadService uploadService;

    /**
     * 图片上传
     * @param img 图片实体
     * @return 上传结果
     */
//    @PostMapping("/upload")
//    public Result<?> uploadImg(@RequestParam("img") MultipartFile img) {
//        if(ObjectUtils.isEmpty(img)) return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数不能为空");
//        return uploadService.uploadImg(img);
//    }

    @PostMapping("/upload")
    public Result<?> uploadImg(@RequestParam("file") MultipartFile img) {
        if(ObjectUtils.isEmpty(img)) return ResultUtils.error(ErrorCode.PARAMS_ERROR, "参数不能为空");
        return uploadService.uploadImg(img);
    }
}
