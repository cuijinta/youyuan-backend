package com.qianye.youyuan.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @Description 拼接上传文件的路径
 * @Author qianye
 * @Date 2024/2/20 13:46
 * @Version 1.0
 */
public class PathUtils {
    public static String generateFilePath(String fileName){
        //根据日期生成路径   2024/2/20/ 形式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMdd/");
        String datePath = sdf.format(new Date());
        //uuid作为文件名
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //后缀和文件后缀一致
        int index = fileName.lastIndexOf(".");
        // test.jpg -> .jpg
        String fileType = fileName.substring(index);
        return new StringBuilder().append("youyuan/").append(datePath).append(uuid).append(fileType).toString();
    }
}
