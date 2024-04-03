package com.qianye.youyuan.service.impl;

import com.qianye.youyuan.common.Result;
import com.qianye.youyuan.constant.enums.ErrorCode;
import com.qianye.youyuan.exception.GlobalException;
import com.qianye.youyuan.service.UploadService;
import com.qianye.youyuan.utils.AliOssUtils;
import com.qianye.youyuan.utils.PathUtils;
import com.qianye.youyuan.utils.ResultUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description 阿里oss文件上传业务层
 * @Author qianye
 * @Date 2024/3/20 23:43
 * @Version 1.0
 */
@Service
@Data
public class UploadServiceImpl implements UploadService {

    @Autowired
    private AliOssUtils aliOssUtils;

    /**
     * 图片上传
     *
     * @param img 图片实体
     * @return 上传结果
     */
    public Result<?> uploadImg(MultipartFile img) {
        //判断文件类型
        //获取原始文件名
        try {
            String originalFilename = img.getOriginalFilename();
            if(!StringUtils.hasText(originalFilename))
                return ResultUtils.error(ErrorCode.PARAMS_ERROR, "文件名不能为空！");
            //对原始文件名进行判断
            if ((!originalFilename.endsWith(".png") && !originalFilename.endsWith(".jpg"))
                    || !StringUtils.hasText(originalFilename)) {
                throw new GlobalException(ErrorCode.FILE_TYPE_ERROR);
            }

            //如果判断通过上传文件到OSS
            String filePath = PathUtils.generateFilePath(originalFilename);
            System.out.println(filePath);
            String url = aliOssUtils.upload(img.getBytes(), filePath);
            return ResultUtils.success(url);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}
