package com.alien.crack_wechat_robot.hook;

import android.content.ContentValues;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.action.MessageAction;
import com.alien.crack_wechat_robot.model.brainserver.InteractionResult;
import com.alien.crack_wechat_robot.util.OkHttpUtil;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedHelpers;

import java.io.IOException;

import okhttp3.Response;

/**
 * 消息拦截
 */
public class MessageReceiveHook {

    private static final String serverUrl = "http://192.168.50.223:233/interaction/interact?talkContent=";

    private static OkHttpUtil client = new OkHttpUtil();

    static  {
        client = new OkHttpUtil();
    }

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
                Log.i(WechatHook.TAG, "contentValues: " + JSON.toJSONString(contentValues));
                Log.i(WechatHook.TAG, talker + " send message:" + content);
                try {
                    interact(content, talker);
                } catch (Exception e) {
                    Log.i(WechatHook.TAG, "all出现异常: " + e.getMessage(), e);
                }
//                Log.i(WechatHook.TAG, talker + " send message:" + content + " = = 让我康康");

            }
        });
    }

    private static void interact(String talkContent, String talker) throws Exception{
        Log.i(WechatHook.TAG, "开始处理对话");
        Response response = client.httpResponeBody(serverUrl + talkContent, OkHttpUtil.HttpMethod.GET);
        if (response.body() == null) {
            MessageAction.postMessage("请求结果为空", talker);
            return;
        }
        String resp = null;
        try {
            resp = response.body().string();
        } catch (IOException e) {
            Log.i(WechatHook.TAG, "出现异常: " + e.getMessage(), e);
            MessageAction.postMessage("获取body内容失败", talker);
            return;
        }
        Log.i(WechatHook.TAG, "请求小完大脑返回数据: " + resp);
        InteractionResult<String> result = JSON.parseObject(resp, new TypeReference<InteractionResult<String>>() {
        });
        if (result == null) {
            MessageAction.postMessage("解析结果为空", talker);
            return;
        }
        if (result.getCode() == 1000) {
            return;
        }
        if (result.getCode() == 1001) {
            MessageAction.postMessage(result.getData(), talker);
            return;
        }
        if (result.getCode() == 9999) {
            MessageAction.postMessage("处理出错, 错误: " + result.getMsg(), talker);
        }
    }


}
