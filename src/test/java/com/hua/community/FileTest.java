package com.hua.community;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试删除一分钟前由wk生成的分享图片
 * @create 2022-05-22 13:25
 */
public class FileTest {

    public static void main(String[] args) {
        String path = "E:/work/data/wk-images";
        File file = new File(path);

        File[] files = file.listFiles();
        for(File f : files){
            long createTime = f.lastModified();
            long now = System.currentTimeMillis();

            System.out.println(now - createTime);
            System.out.println(f.getName());
            if(now - createTime > 60000){
                f.delete();
            }
        }

    }
}
