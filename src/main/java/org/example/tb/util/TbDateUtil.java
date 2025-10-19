package org.example.tb.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TbDateUtil {
    public static String dateTimeNowStr() {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化当前时间
        return now.format(formatter);
    }

    public static String dateTimeBeforeDayStr(int day) {
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算七天前的时间
        LocalDateTime sevenDaysAgo = now.minus(day, ChronoUnit.DAYS);

        // 定义日期时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 格式化七天前的时间
        return sevenDaysAgo.format(formatter);
    }
}
