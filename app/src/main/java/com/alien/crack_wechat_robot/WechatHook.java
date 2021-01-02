package com.alien.crack_wechat_robot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.alien.crack_wechat_robot.action.ContactInfoAction;
import com.alien.crack_wechat_robot.action.MessageAction;
import com.alien.crack_wechat_robot.db.DbHelper;
import com.alien.crack_wechat_robot.hook.MessageReceiveHook;
import com.alien.crack_wechat_robot.service.WxContactService;
import com.camel.api.CamelToolKit;
import com.camel.api.SharedObject;
import com.camel.api.extensions.superappium.PageTriggerManager;
import com.camel.api.extensions.superappium.sekiro.DumpTopActivityHandler;
import com.camel.api.extensions.superappium.sekiro.DumpTopFragmentHandler;
import com.camel.api.extensions.superappium.sekiro.ExecuteJsOnWebViewHandler;
import com.camel.api.extensions.superappium.sekiro.ScreenShotHandler;
import com.camel.api.rposed.IRposedHookLoadPackage;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedBridge;
import com.camel.api.rposed.RposedHelpers;
import com.camel.api.rposed.callbacks.RC_LoadPackage;
import com.virjar.sekiro.api.SekiroClient;

public class WechatHook implements IRposedHookLoadPackage {

    public static final String TAG = "WX_HOOK";

    /**
     * FIXME 具体替换为对应的Sekiro服务端域名
     */
    private static final String SEKIRO_HOST = "sz.clickdream.top";
    /**
     * FIXME 具体替换为对应的Sekiro服务端长连接端口
     */
    private static final int SEKIRO_PORT = 9600;

    public static SekiroClient sekiroClient1;
    public static String clientId;

    @Override
    public void handleLoadPackage(RC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        SharedObject.loadPackageParam = lpparam;
        SharedObject.context = CamelToolKit.sContext;
        testHook();
        Log.i(WechatHook.TAG, "wechat hook success: " + SharedObject.loadPackageParam.processName);
        if (!SharedObject.loadPackageParam.processName.equals(SharedObject.context.getPackageName())) {
            return;
        }
        Log.i(WechatHook.TAG, "wechat Main process hook start");
        try {
            clientId = genDeviceId(SharedObject.context);
            RposedHelpers.findAndHookMethod("com.tencent.tinker.loader.app.TinkerApplication", lpparam.classLoader, "onCreate", new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    logicHook();
                }
            });
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "logic hook exception", e);
        }
    }

    private static void logicHook() {
        DbHelper.init();
        registerSekiroService();
        MessageReceiveHook.hook();
        Log.i(WechatHook.TAG, "logic hook end");
    }

    private static void testHook() {
        try {

        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "hook websocket error", e);
        }
    }

    private static void registerSekiroService() {
        PageTriggerManager.setDisable(false);
        // 注册长连接
        sekiroClient1 = SekiroClient.start(SEKIRO_HOST, SEKIRO_PORT, clientId, "WECHAT_ROBOT");
        sekiroClient1.registerHandler("sendMessage", new MessageAction());
        sekiroClient1.registerHandler("findContactInfo", new ContactInfoAction());
        sekiroClient1.registerHandler("screenShot", new ScreenShotHandler());

        Log.i(WechatHook.TAG, "本地HTTP服务已启动");
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    private static String genDeviceId(Context context) {
        StringBuilder sb = new StringBuilder();
        String serial = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                serial = Build.getSerial();
            } catch (Exception e) {
                //ignore
            }
        }
        if (serial == null) {
            try {
                serial = Build.SERIAL;
            } catch (Exception e) {
                //ignore
            }
        }
        if (serial != null) {
            sb.append("_serial_").append(serial);
        }

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            try {
                String imei = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    imei = telephonyManager.getImei();
                } else {
                    imei = telephonyManager.getDeviceId();
                }
                if (imei != null) {
                    sb.append("_imei_").append(imei);
                }
            } catch (Exception e) {
                //ignore
            }
        }
        String m_szAndroidID = Settings.Secure.getString(SharedObject.context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        sb.append("_androidId_").append(m_szAndroidID).append("_version_5");

        return sb.toString();
    }
}
