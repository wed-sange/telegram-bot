package com.sange.telegram.bot.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    /**
     * 将 dealTime 转换为北京时间
     *
     * @return 北京时间的字符串表示
     */
    public static String getBeijingTime(long dealTime) {
        Instant instant = Instant.ofEpochMilli(dealTime);
        ZoneId beijingZone = ZoneId.of("Asia/Shanghai");
        ZonedDateTime beijingTime = ZonedDateTime.ofInstant(instant, beijingZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return beijingTime.format(formatter);
    }

}
