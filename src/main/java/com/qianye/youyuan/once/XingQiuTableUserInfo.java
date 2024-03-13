package com.qianye.youyuan.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author: qianye
 * @date: 2024/3/2
 * @ClassName: yupao-backend01
 * @Description:    星球表格用户信息
 */
@Data
public class XingQiuTableUserInfo {
    /**
     * id
     */
    @ExcelProperty("成员编号")
    private String planetCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("成员昵称")
    private String username;

}