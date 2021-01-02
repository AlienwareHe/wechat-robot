package com.alien.crack_wechat_robot.db;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * <p>
 * 用户关系表，存储用户的好友关系，删除的好友也可能存在这个表里面。
 * 区分好友是否已经删除：encryptUsername ==null  好友关系存在;如果不为空,好友已经删除;
 * <p>
 * 1. rcontact.type==0     添加对方为好友，对方未通过；
 * 2. rcontact.type==1    好友；
 */
//rcontact 用户表
public class UserTable {

    public static RContactModel findUserInfo(String sql, String[] strArr, ClassLoader classLoader) {
        DbHelper dbHelper = DbHelper.getInstance();
        Cursor cursor = null;
        RContactModel model = null;
        try {
            cursor = dbHelper.rawQuery(sql, strArr, classLoader);
            if (cursor == null) {
                return null;
            }
//        dbHelper.printCursor(cursor);
            model = new RContactModel();
            if (!cursor.moveToFirst()) {
                cursor.close();
                return null;
            }
            String wxid = cursor.getString(cursor.getColumnIndex("username"));
            model.setUsername(wxid);//username
            model.setAlias(cursor.getString(cursor.getColumnIndex("alias")));//alias
            model.setConRemark(cursor.getString(cursor.getColumnIndex("conRemark")));
            model.setDomainList(cursor.getString(cursor.getColumnIndex("domainList")));
            model.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));//nikename
            model.setQuanPin(cursor.getString(cursor.getColumnIndex("quanPin")));
            model.setConRemarkPYFull(cursor.getString(cursor.getColumnIndex("conRemarkPYFull")));
            model.setShowHead(Integer.valueOf(cursor.getString(cursor.getColumnIndex("showHead"))));
            model.setType(Integer.valueOf(cursor.getString(cursor.getColumnIndex("type"))));
            model.setEncryptUsername(cursor.getString(cursor.getColumnIndex("encryptUsername")));//strangerID
            model.setChatroomFlag(Integer.valueOf(cursor.getString(cursor.getColumnIndex("chatroomFlag"))));
            model.setVerifyFlag(Integer.valueOf(cursor.getString(cursor.getColumnIndex("verifyFlag"))));
            model.setContactLabelIds(cursor.getString(cursor.getColumnIndex("contactLabelIds")));
            model.setLvbuff(cursor.getBlob(cursor.getColumnIndex("lvbuff")));// 这个是微信加密的数据
            // todo 暂时用不上这些字段
            model.setPhone(getPhone(classLoader, model.getLvbuff(), wxid));
            model.setAvatar("");
            model.setAddWay(0);
            model.setSex(getSex(model.getLvbuff()));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return model;
    }

    public static RContactModel findUserInfo(ClassLoader classLoader, String wx_id) {
        String sql = "select *,rowid from rcontact  where username = ? or encryptUsername = ?";
        return findUserInfo(sql, new String[]{wx_id, wx_id}, classLoader);
    }

    /**
     * 通讯录好友的个数
     * 默认包含. “文件传输助手” --> username: filehelper,nickname: 文件传输助手
     */
    public static long findContactSize(ClassLoader classLoader) {
        DbHelper dbHelper = DbHelper.getInstance();
        //群聊的 id 可能存在 @chatroom 和 @im.chatroom
        String sql = "select count(*) from rcontact  where (type & 1 != 0 and type & 8 == 0 and type & 32 == 0 and verifyFlag & 8 == 0 and username not like '%@%chatroom' and username not like '%@stranger')";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, null, classLoader);
            if (cursor != null && cursor.moveToFirst()) {
                int size = cursor.getInt(0);
                cursor.close();
                return size;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return 0;
    }


    public static String findUserWxid(ClassLoader classLoader, String strangeId) {
        DbHelper dbHelper = DbHelper.getInstance();
        String sql = "select * from rcontact  where encryptUsername = ?";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, new String[]{strangeId}, classLoader);
            if (cursor == null) {
                return "";
            } else if (!cursor.moveToFirst()) {
                cursor.close();
                return "";
            } else {
                String userName = cursor.getString(cursor.getColumnIndex("username"));
                cursor.close();
                return userName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static String findUserNickname(ClassLoader classLoader, String wxid) {
        DbHelper dbHelper = DbHelper.getInstance();
        String sql = "select * from rcontact  where username = ? ";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, new String[]{wxid}, classLoader);
            if (cursor == null) {
                return "";
            } else if (!cursor.moveToFirst()) {
                cursor.close();
                return "";
            } else {
                String userName = cursor.getString(cursor.getColumnIndex("nickname"));
                cursor.close();
                return userName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static ArrayList<SyncGroupMemberModel> findUserNickNames(ClassLoader classLoader, String wxids) {
        DbHelper dbHelper = DbHelper.getInstance();
        String sql = "select username,nickname from rcontact  where username in (" + wxids + ")";
        Cursor cursor = null;
        ArrayList<SyncGroupMemberModel> userinfo = new ArrayList<>();
        try {
            cursor = dbHelper.rawQuery(sql, null, classLoader);
            if (cursor == null) {
                return userinfo;
            } else if (cursor.moveToFirst()) {
                do {
                    userinfo.add(new SyncGroupMemberModel(cursor.getString(0), cursor.getString(1)));
                } while (cursor.moveToNext());
                cursor.close();
                return userinfo;
            } else {
                cursor.close();
                return userinfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userinfo;
    }

    public static String findUserByFieldName(ClassLoader classLoader, String wxId, @NonNull String fieldName) {
        DbHelper dbHelper = DbHelper.getInstance();
        String sql = "select * from rcontact  where username = ? or encryptUsername = ?";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, new String[]{wxId, wxId}, classLoader);
            if (cursor == null) {
                return "";
            } else if (!cursor.moveToFirst()) {
                cursor.close();
                return "";
            } else {
                String fieldValue = cursor.getString(cursor.getColumnIndex(fieldName));
                cursor.close();
                return fieldValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    public static boolean isFriendByAlias(ClassLoader classLoader, @NonNull String alias) {
        String sql = "select * from rcontact  where alias = ?";
        RContactModel model = findUserInfo(sql, new String[]{alias}, classLoader);
        return model != null && (model.getType() == 1 || model.getType() == 3 || model.getType() == 5 || model.getType() == 65);
    }

    public static boolean isFriend(RContactModel model) {
        return model != null
                && (model.getType() & 1) != 0
                && (model.getType() & 8) == 0
                && (model.getType() & 32) == 0
                && (model.getVerifyFlag() & 8) == 0;
    }

    public static long insertUserData(ClassLoader classLoader, ContentValues contentValues) {
        DbHelper dbHelper = DbHelper.getInstance();
        return dbHelper.insert("rcontact", "username", contentValues, classLoader);
    }

    public static ArrayList<RContactModel> findUserInfos(String sql, String[] strArr, ClassLoader classLoader) {
        DbHelper dbHelper = DbHelper.getInstance();
        ArrayList<RContactModel> userInfoModels = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, strArr, classLoader);
            RContactModel model = new RContactModel();
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            do {
                model.setUsername(cursor.getString(cursor.getColumnIndex("username")));//username
                model.setAlias(cursor.getString(cursor.getColumnIndex("alias")));//alias
                model.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));//nikename
                model.setEncryptUsername(cursor.getString(cursor.getColumnIndex("encryptUsername")));//strangerID
                model.setType(Integer.valueOf(cursor.getString(cursor.getColumnIndex("type"))));//type
                model.setAlias(cursor.getString(cursor.getColumnIndex("reserved1")));
                userInfoModels.add(model);
            } while (cursor.moveToNext());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userInfoModels;
    }

    public static RContactModel findQQList(String sql, String[] strArr, ClassLoader classLoader) {
        DbHelper dbHelper = DbHelper.getInstance();
        Cursor cursor = null;
        RContactModel model = null;
        try {
            cursor = dbHelper.rawQuery(sql, strArr, classLoader);
            model = new RContactModel();
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            model.setUsername(cursor.getString(cursor.getColumnIndex("qq")));//username
            model.setAlias(cursor.getString(cursor.getColumnIndex("username")));//alias
            model.setNickname(cursor.getString(cursor.getColumnIndex("nickname")));//nikename
            model.setEncryptUsername(cursor.getString(cursor.getColumnIndex("wexinstatus")));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return model;
    }

    public static ArrayList<RContactModel> getRemarkFriends(ClassLoader classLoader) {
        DbHelper dbHelper = DbHelper.getInstance();
        ArrayList<RContactModel> userInfoModels = new ArrayList<>();
        String sql = "select * from rcontact where conRemark <>''";
        Cursor cursor = null;
        try {
            cursor = dbHelper.rawQuery(sql, null, classLoader);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            do {
                RContactModel model = new RContactModel();
                model.setUsername(cursor.getString(cursor.getColumnIndex("username")));//username
                model.setConRemark(cursor.getString(cursor.getColumnIndex("conRemark")));
                model.setType(Integer.valueOf(cursor.getString(cursor.getColumnIndex("type"))));//type
                userInfoModels.add(model);
            } while (cursor.moveToNext());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userInfoModels;
    }

    private static ArrayList<String> getPhone(ClassLoader classLoader, byte[] bytes, String wx_id) {
        return new ArrayList<>();
    }

    private static String getUpload2Phone(ClassLoader classLoader, String wx_id) {
        String sql = "select * from addr_upload2 where username = ?";
        DbHelper dbHelper = DbHelper.getInstance();
        Cursor cursor = null;
        String upPhone;
        try {
            cursor = dbHelper.rawQuery(sql, new String[]{wx_id}, classLoader);
            if (cursor == null) {
                return "";
            }
            if (!cursor.moveToFirst()) {
                cursor.close();
                return "";
            }
            upPhone = cursor.getString(cursor.getColumnIndex("moblie"));
            cursor.close();
            return upPhone.replace(" ", "");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }


    private static int getSex(byte[] bytes) {
       return 0;
    }
}
