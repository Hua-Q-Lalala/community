package com.hua.community;

import java.io.IOException;

/**
 * 测试生成长图的Wkhtmltopdf
 * @create 2022-05-19 23:14
 */
public class WkTests {

    public static void main(String[] args) {
        String cmd = "E:/Environment/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com e:/work/data/wk-images/5.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
