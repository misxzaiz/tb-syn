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

    public static String dateTimeAfterSecond(String time, int second) {
        LocalDateTime lastSynTime = TbDateUtil.dateTimeParse(time);
        LocalDateTime newSynTime = lastSynTime.plus(second, ChronoUnit.SECONDS);
        return TbDateUtil.dateTimeFormat(newSynTime);
    }

    public static LocalDateTime dateTimeParse(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 将 LocalDateTime 对象格式化为日期时间字符串
     * @param dateTime LocalDateTime 对象
     * @return 格式化后的日期时间字符串，格式为 "yyyy-MM-dd HH:mm:ss"
     */
    public static String dateTimeFormat(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    public static boolean isAfter(String time1, String time2) {
        LocalDateTime t1 = TbDateUtil.dateTimeParse(time1);
        LocalDateTime t2 = TbDateUtil.dateTimeParse(time2);
        return t1.isAfter(t2);
    }

    public static boolean isAfterNow(String time) {
        LocalDateTime t1 = TbDateUtil.dateTimeParse(time);
        LocalDateTime now = LocalDateTime.now();
        return t1.isAfter(now);
    }
}
