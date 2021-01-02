package com.alien.crack_wechat_robot.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.alien.crack_wechat_robot.WechatHook;
import com.alien.crack_wechat_robot.util.MD5Util;
import com.alien.crack_wechat_robot.util.WxPreferenceUtils;
import com.camel.api.SharedObject;
import com.camel.api.rposed.RC_MethodHook;
import com.camel.api.rposed.RposedHelpers;

import java.util.Arrays;

import static android.database.Cursor.FIELD_TYPE_BLOB;
import static android.database.Cursor.FIELD_TYPE_FLOAT;
import static android.database.Cursor.FIELD_TYPE_INTEGER;
import static android.database.Cursor.FIELD_TYPE_NULL;
import static android.database.Cursor.FIELD_TYPE_STRING;


public class DbHelper {

    private static final DbHelper dbHelper = new DbHelper();
    private volatile Object mDBConnection;
    private String dbPassword = "";

    public static DbHelper getInstance() {
        return dbHelper;
    }

    public static void init() {
        try {
            RposedHelpers.findAndHookMethod("com.tencent.mm.storagebase.f", SharedObject.loadPackageParam.classLoader, "E",
                    String.class, String.class, Boolean.TYPE, new RC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object conn = param.getResult();
                            if (conn != null) {
                                String path = (String) RposedHelpers.callMethod(conn, "getPath");
                                if (path.endsWith("EnMicroMsg.db")) {
                                    Log.i(WechatHook.TAG, "数据库密码: " + Arrays.toString(param.args));
                                    String password = (String) param.args[1];
                                    DbHelper.getInstance().setConnection(conn);
                                    DbHelper.getInstance().setDbPassword(password);
                                }
                            }
                        }
                    });
        } catch (Throwable e) {
            Log.e(WechatHook.TAG, "load WxContactService cinit method error!", e);
        }
    }

    private DbHelper() {
    }

    // 加synchronized原因是,将此方法改成线程安全的,防止openDB执行多次导致打开数据失败.
    synchronized Cursor rawQuery(String sql, String[] strArr, ClassLoader classLoader) {
        if (!isDBOpen()) {
            openDB(classLoader);
        }
        return (Cursor) RposedHelpers.callMethod(mDBConnection, "a", sql, strArr, 0);
    }

    long insert(String tableName, String strArr, ContentValues contentValues, ClassLoader classLoader) {
        if (!isDBOpen()) {
            openDB(classLoader);
        }
        return (long) RposedHelpers.callMethod(mDBConnection, "insert", tableName, strArr, contentValues);
    }

    public Object getConnection() {
        return mDBConnection;
    }


    public void setConnection(Object connection) {
        mDBConnection = connection;
    }

    /**
     * 打开数据库连接
     *
     * @param classLoader loader
     */
    private synchronized void openDB(ClassLoader classLoader) {
        Class<?> clazz = RposedHelpers.findClass("com.tencent.mm.cf.f", classLoader);
        if (!TextUtils.isEmpty(getDBPassword())) {
            mDBConnection = RposedHelpers.callStaticMethod(clazz, "F", getDBPath(), getDBPassword(), false);
        }
    }

    /**
     * 打开数据库连接
     */
    private synchronized boolean isDBOpen() {
        if (mDBConnection == null) {
            return false;
        }
        return (Boolean) RposedHelpers.callMethod(mDBConnection, "isOpen");
    }

    @Deprecated
    private void closeDB(ClassLoader classLoader) {
        if (mDBConnection != null) {
            RposedHelpers.callMethod(mDBConnection, "close");
        }
    }

    public void printCursor(Cursor cursor) {
        int position = cursor.getPosition();
        if (!cursor.moveToFirst()) {
            Log.d(WechatHook.TAG, "Cursor 数据为空\n");
            return;
        }
        Log.d(WechatHook.TAG, "************** DB Cursor start  **************\n");
        do {
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                switch (cursor.getType(i)) {
                    case FIELD_TYPE_INTEGER:
                        Log.d(WechatHook.TAG, cursor.getColumnName(i) + ": " + cursor.getLong(i));
                        break;
                    case FIELD_TYPE_FLOAT:
                        Log.d(WechatHook.TAG, cursor.getColumnName(i) + ": " + cursor.getDouble(i));
                        break;
                    case FIELD_TYPE_STRING:
                        Log.d(WechatHook.TAG, cursor.getColumnName(i) + ": " + cursor.getString(i));
                        break;
                    case FIELD_TYPE_BLOB:
                        Log.d(WechatHook.TAG, cursor.getColumnName(i) + ": " + Arrays.toString(cursor.getBlob(i)));
                        break;
                    case FIELD_TYPE_NULL:
                        Log.d(WechatHook.TAG, cursor.getColumnName(i) + ": NULL");
                        break;
                    default:
                        Log.d(WechatHook.TAG, cursor.getColumnName(i) + ": " + "请手动解析");
                        break;
                }
            }
            if (!cursor.isLast())
                Log.d(WechatHook.TAG, "---------------------------------------------\n");
        } while (cursor.moveToNext());
        Log.d(WechatHook.TAG, "************** DB Cursor end  **************\n");
        cursor.moveToPosition(position);
    }

    private String getDBPath() {
        String path = "/data/data/com.tencent.mm/MicroMsg/";
        String name = "/EnMicroMsg.db";
        String md5 = MD5Util.toMd5(("mm" + getUin()).getBytes(), false);
        return path.concat(md5).concat(name);
    }

    public String getDBPassword() {
//        Log.d(WechatHook.TAG,"数据库密码为： " + MD5Util.toMd5((DeviceUtil.getIMEI(mBaseContext) + getUin()).getBytes(), false).substring(0, 7));
        return TextUtils.isEmpty(dbPassword) ? WxPreferenceUtils.getDbPassword(SharedObject.context) : dbPassword;
    }

    public static int getUin() {
        SharedPreferences preferences = SharedObject.context.getSharedPreferences("system_config_prefs", Context.MODE_PRIVATE);
        return preferences.getInt("default_uin", 0);
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
        WxPreferenceUtils.setDatabasePassword(SharedObject.context, dbPassword);
    }
}
