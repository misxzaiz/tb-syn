package org.example.tb.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个简单的调用频率限制器
 * 用于测试：如果1秒内调用超过3次，会提示调用频繁
 */
public class SimpleRateLimiter {

    // 用来存放最近调用的时间戳（毫秒）
    private final List<Long> requestTimestamps = new ArrayList<>();

    // 限制条件：在指定的时间窗口内（毫秒）
    private final long timeWindowInMillis;
    // 限制条件：允许的最大调用次数
    private final int maxRequests;

    /**
     * 构造函数
     * @param timeWindowInMillis 时间窗口，例如 1000 代表1秒
     * @param maxRequests 在时间窗口内允许的最大请求次数
     */
    public SimpleRateLimiter(long timeWindowInMillis, int maxRequests) {
        this.timeWindowInMillis = timeWindowInMillis;
        this.maxRequests = maxRequests;
    }

    /**
     * 检查是否允许调用
     * @return true: 允许调用; false: 调用过于频繁
     */
    public synchronized boolean allowRequest() {
        // 1. 获取当前时间
        long currentTime = System.currentTimeMillis();

        // 2. 清理掉所有超出时间窗口的旧记录
        //    从列表开头遍历，如果记录的时间早于窗口开始时间，就移除它
        requestTimestamps.removeIf(timestamp -> currentTime - timestamp > timeWindowInMillis);

        // 3. 检查当前窗口内的请求数量是否已达到上限
        if (requestTimestamps.size() >= maxRequests) {
            // 超过限制，拒绝请求
            return false;
        }

        // 4. 未超过限制，记录本次调用时间，并允许请求
        requestTimestamps.add(currentTime);
        return true;
    }

    // --- 以下是测试代码 ---
    public static void main(String[] args) throws InterruptedException {
        // 创建一个限流器实例：1秒内最多允许3次调用
        SimpleRateLimiter limiter = new SimpleRateLimiter(1000, 3);

        System.out.println("--- 开始测试 ---");
        System.out.println("模拟快速连续调用5次：");

        // 模拟在1秒内快速调用5次
        for (int i = 1; i <= 5; i++) {
            boolean allowed = limiter.allowRequest();
            if (allowed) {
                System.out.println("调用 " + i + ": 成功！");
            } else {
                System.out.println("调用 " + i + ": 失败！调用过于频繁。");
            }
            // 每次调用间隔200毫秒
            Thread.sleep(200);
        }

        System.out.println("\n等待1.2秒，让时间窗口重置...");
        Thread.sleep(1200);

        System.out.println("\n再次调用2次：");
        for (int i = 6; i <= 7; i++) {
            boolean allowed = limiter.allowRequest();
            if (allowed) {
                System.out.println("调用 " + i + ": 成功！");
            } else {
                System.out.println("调用 " + i + ": 失败！调用过于频繁。");
            }
        }
    }
}

