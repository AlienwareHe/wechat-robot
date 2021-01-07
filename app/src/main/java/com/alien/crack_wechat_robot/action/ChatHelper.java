package com.alien.crack_wechat_robot.action;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.util.DownloadUtil;
import com.alien.crack_wechat_robot.util.FileUtils;
import com.alien.crack_wechat_robot.util.MD5Util;
import com.alien.crack_wechat_robot.util.ThreadPoolManager;
import com.alien.crack_wechat_robot.util.WxPreferenceUtils;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RposedHelpers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class ChatHelper {

    public static LruCache<String, String> imageCache = new LruCache<>(30);

    /**
     * 下载图片文件
     *
     * @param imageUrl 图片的url
     * @param talker   聊天的对象
     *                 todo 目录根据md5分层
     */
    public static void downloadAndSendImageFile(final String imageUrl, final String talker, String md5) {
        if (md5 == null) {
            md5 = "";
        }
        final File downloadFile = getDownloadFile(imageUrl, "", FolderSettings.IMAGE_DIR, md5);
        if (downloadFile == null) {// 读写SD卡出错,回调发送文件失败,携带requestId
            return;
        }
        // 如果之前下载过，那么就不再重复下载这个文件了。根据md5判断文件的一致性
        Log.e(WechatHook.TAG, "down image:" + md5);
        if (downloadFile.exists() && (md5.equalsIgnoreCase(MD5Util.getFileMD5(downloadFile)) || md5.equals(""))) {
            sendImage(imageUrl, downloadFile.getAbsolutePath(), talker);
        } else {
            downImage(downloadFile, imageUrl, talker, md5);
        }
    }

    private static File getDownloadFile(String url, String fileName, String folder, String md5) {
        //检查SD卡状态是否mounted
        if (!Environment.getExternalStorageDirectory().canRead() || !Environment.getExternalStorageDirectory().canWrite()) {
            Log.e(WechatHook.TAG, "读写SD卡出错，请检查SD卡");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        File dir;
        if (TextUtils.isEmpty(md5)) {
            dir = new File(folder);
        } else {
            dir = new File(folder, sb.append(md5.substring(0, 2)).append(File.separator).append(md5.substring(2, 5)).toString());
        }

        if (TextUtils.isEmpty(fileName)) {
            String[] urlSplit = url.split("/");
            fileName = urlSplit[urlSplit.length - 1];
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return new File(dir, fileName);
    }

    /**
     * 发送图片
     * Hook思路，ChattingUI$a类，有发送图片的方法
     * pushsendimage, param.compressImg:%b, compresstype:%d 谁调用这个方法了
     *
     * @param imageUrl 图片的地址
     * @param path     图片的路径
     * @param weixinId 接收图片人的微信id
     */
    private static void sendImage(String imageUrl, final String path, final String weixinId) {

        if (!TextUtils.isEmpty(imageUrl)) {
            String key = path + weixinId;
            imageCache.put(key, imageUrl);
        }

        ThreadPoolManager.getInstace().execute(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.checkFileExist(path)) {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(path);
                    Object oAlT = RposedHelpers.callStaticMethod(RposedHelpers.findClass("com.tencent.mm.au.q", SharedObject.loadPackageParam.classLoader), "aMl");
                    RposedHelpers.callMethod(oAlT, "a", WxPreferenceUtils.getWxId(), weixinId, arrayList);
                    LinkedList list = (LinkedList) RposedHelpers.getObjectField(oAlT, "ihf");
                    //发送图片前先获取一下本地发送图片的队列数量,如果超出一定阈值认为发图片loading了,使用frog打点记录一下,不影响业务的正常执行
                    if (list != null && list.size() > 10) {
                        Log.d(WechatHook.TAG, "发图片loading了,本地队列数量是" + list.size());
                    }
                }
            }
        });
    }

    private static int loopTime = 0;

    private static void downImage(final File downloadFile, final String imageUrl, final String talker, final String md5) {
        DownloadUtil.downloadSequential(downloadFile, imageUrl,
                "下载中", "", new DownloadUtil.Callback<Boolean>() {
                    @Override
                    public void callback(Boolean data) {
                        if (data) {
                            if (!TextUtils.isEmpty(md5)) {
                                String myMd5 = MD5Util.getFileMD5(new File(downloadFile.getAbsolutePath()));
                                if (!md5.equalsIgnoreCase(myMd5)) {
                                    FileUtils.delFile(downloadFile.getAbsolutePath());
                                    return;
                                }
                            }
                            String filePath = downloadFile.getAbsolutePath();
                            Log.i(WechatHook.TAG, "图片下载成功,开始发送" + filePath);
                            sendImage(imageUrl, filePath, talker);
                            loopTime = 0;
                        } else {
                            if (loopTime < 4) {
                                loopTime++;
                                downImage(downloadFile, imageUrl, talker, md5);
                            } else {// 图片下载失败了,需要回调
                                loopTime = 0;
                            }
                        }
                    }
                });
    }
}
