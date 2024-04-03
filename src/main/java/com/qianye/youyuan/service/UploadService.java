package com.qianye.youyuan.service;

import com.qianye.youyuan.common.Result;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * 图片上传
     * @param img 图片实体
     * @return 上传结果
     */
    Result uploadImg(MultipartFile img);
}
