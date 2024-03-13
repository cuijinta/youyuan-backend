package com.qianye.youyuan.once;

/**
 * @Author 浅夜
 * @Description 导入Excel
 * @DateTime 2024/3/5 17:15
 **/

import com.alibaba.excel.EasyExcel;

import java.util.List;

/**
 * 导入Excel 读取数据
 */
public class ImportExcel {

    /**
     * 读取数据
     */
    public static void main(String[] args) {
        String fileName = "D:\\Desktop\\SourceMaterial\\testExcel.xlsx";
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        readByListener(fileName);
//        synchronousRead(fileName);
    }

    /**
     * 监听器读取
     *
     * @param fileName
     */
    public static void readByListener(String fileName) {
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     *  同步的返回，不推荐使用，如果数据量大会把数据放到内存里面造成卡顿
     * @param fileName
     */
    public static void synchronousRead(String fileName) {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<XingQiuTableUserInfo> totalDataList =
                EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuTableUserInfo : totalDataList) {
            System.out.println(xingQiuTableUserInfo);
        }
    }
}
