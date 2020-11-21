package com.alien.crack_wechat_robot.service;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import com.alien.crack_wechat_robot.WechatHook;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedHelpers;

import java.util.Arrays;

import camel.external.org.apache.commons.lang3.StringUtils;


/**
 * 微信联系人相关数据
 */
public class WxContactService {

    private static Object wxDbMapper;

    /**
     * 尽可能早的调用，防止错过aj构造的时机
     */
    public static void init() {
        try {
            RposedHelpers.findAndHookConstructor("com.tencent.mm.storage.aj", SharedObject.loadPackageParam.classLoader, "com.tencent.mm.cf.h", new RC_MethodHook() {
                @Override
                protected void afterHookedMethod(RC_MethodHook.MethodHookParam param) throws Throwable {
                    Log.i(WechatHook.TAG, "weixin db mapper hooked");
                    if (wxDbMapper == null) {
                        wxDbMapper = RposedHelpers.getObjectField(param.thisObject, "db");
                    }
                }
            });
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "load WxContactService cinit method error!", e);
        }
    }

    /**
     * 根据微信昵称查询微信号
     *
     * @param nickname 微信昵称
     * @return if exec error or parameter is illegal or not find, result may be null
     */
    public static String findWxIdByNickName(String nickname) {
        if (wxDbMapper == null) {
            Log.e(WechatHook.TAG, "wx db mapper is null");
            return null;
        }
        if (StringUtils.isBlank(nickname)) {
            return null;
        }
        String sql = "select * from rcontact where nickname = " + DatabaseUtils.sqlEscapeString(nickname);
        Cursor cursor = dbExec(sql);
        if (cursor == null) {
            return null;
        }
        if (cursor.moveToFirst()) {
            int usernameIndex = cursor.getColumnIndex("username");
            Log.i(WechatHook.TAG, "curosr columns:" + Arrays.toString(cursor.getColumnNames()) + ";usernameIndex:" + usernameIndex);
            String username = cursor.getString(usernameIndex);
            cursor.close();
            return username;
        } else {
            Log.i(WechatHook.TAG, "cursor move to first is false");
            cursor.close();
        }
        return null;
    }

    private static Cursor dbExec(String sql) {
        if (StringUtils.isBlank(sql)) {
            return null;
        }
        Log.i(WechatHook.TAG, "db exec:" + sql);
        // 第三个参数2尚不知具体意义
        try {
            return (Cursor) RposedHelpers.callMethod(wxDbMapper, "a", sql, null, 2);
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "wx db mapper exec error", e);
            return null;
        }
    }
}
