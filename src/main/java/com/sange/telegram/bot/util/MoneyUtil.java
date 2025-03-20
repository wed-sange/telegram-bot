package com.sange.telegram.bot.util;

import java.text.DecimalFormat;

public class MoneyUtil {

    /**
     * 将分转换为元  12345 => 123.45
     * @param fen
     * @return
     */
    public static String convertToYuan(long fen) {
        double yuan = fen / 100.0; // 转换为元
        DecimalFormat df = new DecimalFormat("#.00"); // 保留两位小数
        return df.format(yuan);
    }
}
