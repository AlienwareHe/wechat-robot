package com.alien.crack_wechat_robot.util;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.alien.crack_wechat_robot.WechatHook;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.OkDownload;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener2;

import java.io.File;


/**
 * Created by sn on 14-1-21.
 */
public class DownloadUtil {

    public interface Callback<ResultType> {
        /**
         * 在调用时所在的线程执行回调处理（比如事件发起的调用是在ui线程，回调就可以直接操作ui元素）
         * @param data 经过自动数据转换后的对象， 如，我们要求一个JavaBean，他会自动构造该对象， 并且递归初始化他的属性（从json属性值自动转换类型并赋值），支持范型
         */
        public void callback(ResultType data);
    }


    public static void downloadSequential(File file, String url, String tickerText, String title, final Callback<Boolean> callback) {
        cancelAllTask();
        downloadSingleMode(file, url, callback);
    }

    public static void downloadParallel(File file, String url, final Callback<Boolean> callback) {
        DownloadTask downloadTask = new DownloadTask.Builder(url, file).build();
        downloadTask.enqueue(new DownloadListener2() {
            @Override
            public void taskStart(DownloadTask task) {
                String filename = task.getFilename();
                Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil downloadParallel taskStart: %s", filename));
            }

            @Override
            public void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
                if (realCause != null) {
                    Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil downloadParallel taskEnd, realCause: %s", realCause.getMessage()));
                }
                Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil downloadParallel taskEnd, cause: %s ", cause));

                if (cause == EndCause.COMPLETED) {
                    callback.callback(true);
                } else {
                    callback.callback(false);
                }
            }

            @Override
            public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
                BreakpointInfo taskInfo = task.getInfo();
                if (taskInfo != null) {
                    int blockCount = taskInfo.getBlockCount();
                    Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil downloadParallel progress: %s, blockIndex: %s, blockCount: %s",
                            (blockIndex * 1.0f / blockCount), blockIndex, blockCount));
                }
            }
        });
    }

    /**
     * 单任务模式，同一时间只能有一个下载任务执行， 每次收到新的下载，会cancel掉其他正在执行中的任务。
     */
    public static void downloadSingleMode(File file, String url, final Callback<Boolean> callback) {
        DownloadTask downloadTask = new DownloadTask.Builder(url, file)
                .setPassIfAlreadyCompleted(false).build();

        downloadTask.enqueue(new DownloadListener2() {
            @Override
            public void taskStart(DownloadTask task) {
                BreakpointInfo info = task.getInfo();
                Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil taskStart info: %s", info));
            }

            @Override
            public void taskEnd(DownloadTask task, EndCause cause, Exception realCause) {
                BreakpointInfo info = task.getInfo();
                if (realCause != null) {
                    Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil taskEnd, realCause: %s", realCause.getMessage()));
                }
                Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil taskEnd, cause: %s , info: ", cause, info));

                if (cause == EndCause.COMPLETED) {
                    callback.callback(true);
                } else {
                    callback.callback(false);
                }
            }

            @Override
            public void fetchProgress(DownloadTask task, int blockIndex, long increaseBytes) {
                BreakpointInfo taskInfo = task.getInfo();
                if (taskInfo != null) {
                    int blockCount = taskInfo.getBlockCount();
                    Log.i(WechatHook.TAG,String.format("gong>> DownloadUtil downloadSingleMode progress: %s, blockIndex: %s, blockCount: %s, info:",
                            (blockIndex * 1.0f / blockCount), blockIndex, blockCount, taskInfo));
                }
            }
        });
    }

    /**
     * cancel all running tasks
     */
    public static void cancelAllTask() {
        OkDownload.with().downloadDispatcher().cancelAll();
    }


    public static boolean isDownloading(Context context, String url) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING | DownloadManager.STATUS_PENDING);
        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor.moveToFirst()) {
                if (cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)).equals(url)) {
                    cursor.close();
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * 调用系统下载完成一个下载任务，配合receiver处理下载完成后的操作
     *
     * @param context   上下文
     * @param url       要下载的文件地址
     * @param publicDir 存储的公开目录，参见{@link android.os.Environment#DIRECTORY_PICTURES},{@link android.os.Environment#DIRECTORY_DOWNLOADS}等
     * @return 系统下载的id，如果下载失败返回-1
     */
    public static long systemDownload(Context context, String filename, String url, String publicDir) {
        try {
            String fileName = filename != null ? filename : URLUtil.guessFileName(url, null, null);
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri);


            request.setTitle(fileName);
            request.setDestinationInExternalPublicDir(publicDir, fileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            long id = downloadManager.enqueue(request);
            return id;
        } catch (Exception e) {
            Log.i(WechatHook.TAG,"下载异常",e);
        }
        return -1;
    }

    public static long systemDownload(Context context, String url, String publicDir) {
        return systemDownload(context, null, url, publicDir);
    }

    /**
     * 通过系统下载的id获得下载文件的路径
     *
     * @param context 上下文
     * @param id      {@link DownloadManager#enqueue(DownloadManager.Request)} 方法返回的下载id
     * @return 下载文件的路径
     */
    public static String getPathByDownloadId(Context context, long id) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        if (downloadManager == null) {
            return null;
        }
        query.setFilterById(id);
        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
        Cursor cursor = downloadManager.query(query);

        String path = null;
        String uri = null;
        try {
            if (cursor != null && cursor.moveToNext()) {
                path = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                if (TextUtils.isEmpty(path) && !TextUtils.isEmpty(uri)) {
                    path = Uri.parse(uri).getPath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (TextUtils.isEmpty(path) && uri != null && uri.startsWith("content:")) {
            try {
                cursor = context.getContentResolver().query(Uri.parse(uri), null, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    path = cursor.getString(cursor.getColumnIndex("_data"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return path;
    }
}
