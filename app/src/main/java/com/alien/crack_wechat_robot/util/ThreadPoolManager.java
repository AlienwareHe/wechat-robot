package com.alien.crack_wechat_robot.util;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ThreadPoolManager {
    private final long keepAliveTime = 1;// 非核心线程存活时间为1min
    private TimeUnit unit = TimeUnit.MINUTES;
    private final int maxQueue = 128;
    private ThreadPoolExecutor mThreadPoolExecutor;
    private static ThreadPoolManager instance;
    // 因为在微信这个进程中不只是插件中有线程耗资源,微信本身也有许多耗资源的线程,所以将核心线程数设置为4,最大线程数设置为8.
    private final int corePoolSize = 4;
    private final int maxmunPoolSize = 8;

    private ThreadPoolManager() {
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxmunPoolSize,
                    keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(maxQueue),
                    Executors.defaultThreadFactory());
        }
    }

    public static ThreadPoolManager getInstace() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        if (runnable == null) return;
        mThreadPoolExecutor.execute(runnable);
    }

    public void remove(Runnable runnable) {
        if (runnable == null) return;
        mThreadPoolExecutor.remove(runnable);
    }

    public void shutDown(Runnable runnable) {
        if (mThreadPoolExecutor.isShutdown()) return;
        mThreadPoolExecutor.shutdown();
    }
}
