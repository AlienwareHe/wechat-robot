package com.alien.crack_wechat_robot;

import android.util.Log;

import com.camel.api.SharedObject;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedBridge;
import com.camel.api.rposed.RposedHelpers;


public class ValidateHook {

//    public static Object chatFooterEventListener;

    public static void hook() {
        try {
            internalHook();
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "validate hook error", e);
            throw e;
        }
    }

    public static void internalHook() {

//        hookLog();

//        CamelToolKit.virtualEnv.disableFakeFingerprint();

        RposedBridge.hookAllMethods(RposedHelpers.findClass("com.tencent.mm.sdk.platformtools.ab$1", SharedObject.context.getClassLoader()), "getLogLevel", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(0);
            }
        });

//        RposedBridge.hookAllConstructors(StatFs.class, new RC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                Log.i(WechatHook.TAG, "statfs:" + JSON.toJSONString(param.thisObject));
//            }
//        });

        /**
         * 发送消息方法
         * trace from com.tencent.mm.pluginsdk.ui.chat.ChatFooter$4.onclick
         */
        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.mm.ui.chatting.p", SharedObject.loadPackageParam.classLoader), "TT", "java.lang.String", new RC_MethodHook() {

            @Override
            protected void beforeHookedMethod(RC_MethodHook.MethodHookParam param) throws Throwable {
                try {
                    String content = (String) param.args[0];
//                    Log.i(WechatHook.TAG, "before com.tencent.mm.ui.chatting.p:" + content);
//                    Log.i(WechatHook.TAG, "before com.tencent.mm.ui.chatting.p unicode:" + UnicodeUtil.string2Unicode(content));
                } catch (Throwable e) {
                    Log.e(WechatHook.TAG, "before com.tencent.mm.ui.chatting.p", e);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
//                    Log.i(WechatHook.TAG, "after com.tencent.mm.ui.chatting.p:" + param.args[0] + "|" + param.getResult());
                } catch (Throwable e) {
                    Log.e(WechatHook.TAG, "after com.tencent.mm.ui.chatting.p", e);
                }
            }
        });

        /**
         * 获取微信Id
         */
//        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.mm.ui.chatting.d.a", SharedObject.loadPackageParam.classLoader), "getTalkerUserName", new RC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                try {
//                    Log.i(WechatHook.TAG, "after com.tencent.mm.ui.chatting.d.a getTalkUserName:" + param.getResult());
//                } catch (Throwable e) {
//                    Log.e(WechatHook.TAG, "after com.tencent.mm.ui.chatting.d.a getTalkUserName:", e);
//                }
//            }
//        });


        /**
         * message sender
         * every user mapping to one instance
         */
//        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.mm.pluginsdk.ui.chat.ChatFooter", SharedObject.loadPackageParam.classLoader), "setFooterEventListener", "com.tencent.mm.pluginsdk.ui.chat.b", new RC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                chatFooterEventListener = param.args[0];
//                Log.i(WechatHook.TAG, "=== chatFooterEventListener seted:" + (chatFooterEventListener != null), new Throwable());
//            }
//        });

        /**
         * 查询用户微信Id等信息
         */
        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.mm.storage.aj", SharedObject.loadPackageParam.classLoader), "aub", "java.lang.String", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    //Log.i(WechatHook.TAG, "select user info where username= " + param.args[0] + " result=" + JSON.toJSONString(param.getResult()));
                } catch (Throwable e) {
                    Log.e(WechatHook.TAG, "select user info hook exception", e);
                }
            }
        });

    }


    private static void hookLog() {
        RposedHelpers.findAndHookMethod(RposedHelpers.findClass("com.tencent.mm.sdk.platformtools.ab", SharedObject.loadPackageParam.classLoader), "i", "java.lang.String", "java.lang.String", new RC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!"MicroMsg.ChatFooter".equals(param.args[0])) {
                    return;
                }
                Log.i(WechatHook.TAG, "tencent log:" + param.args[0] + " | " + param.args[1]);
            }
        });
    }

}
