package com.alien.crack_wechat_robot.hook;

import android.content.ContentValues;
import android.util.Log;

import com.alien.crack_wechat_robot.WechatHook;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedHelpers;

/**
 * 消息拦截
 */
public class MessageReceiveHook {
    public static void hook() {
        try {
            internalHook();
            Log.i(WechatHook.TAG, "message receive hook success");
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "message receive hook exception", e);
        }
    }

    private static void internalHook() {
        /**
         * 消息撤回时会修改message表中记录
         */
        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.wcdb.database.SQLiteDatabase", SharedObject.loadPackageParam.classLoader), "updateWithOnConflict", String.class, ContentValues.class, String.class, String[].class, int.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(RC_MethodHook.MethodHookParam param) throws Throwable {
                // message为消息表
                if (!"message".equals(param.args[0])) {
                    return;
                }
                ContentValues contentValues = (ContentValues) param.args[1];
                if (contentValues == null) {
                    Log.i(WechatHook.TAG, "message update with conflict content is null");
                    return;
                }
                for (String key : contentValues.keySet()) {
                    Log.i(WechatHook.TAG, "message update with conflict,key: " + key + ";value=" + contentValues.get(key));
                }
            }
        });

        /**
         * 消息入库
         */
        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.wcdb.database.SQLiteDatabase", SharedObject.loadPackageParam.classLoader), "insertWithOnConflict", String.class, String.class, ContentValues.class, int.class, new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // message为消息表
                if (!"message".equals(param.args[0])) {
                    return;
                }
                ContentValues contentValues = (ContentValues) param.args[2];
                if (contentValues == null) {
                    Log.i(WechatHook.TAG, "message insert with conflict content is null");
                    return;
                }
                if (contentValues.getAsInteger("type") != 1) {
                    Log.i(WechatHook.TAG, "消息类型不为1：" + contentValues.getAsInteger("type"));
                    return;
                }
                if (contentValues.getAsInteger("isSend") != 0) {
                    // 是否发送
                    return;
                }
                final String content = contentValues.getAsString("content");
                final String talker = contentValues.getAsString("talker");
                final String msgId = contentValues.getAsString("msgId");
                final String createTime = contentValues.getAsString("createTime");
                Log.i(WechatHook.TAG, talker + " send message:" + content);
            }
        });
    }


}
