package com.alien.crack_wechat_robot.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5Util {
    private MD5Util() {
    }

    public static String toMd5(byte[] var0, boolean var1) {
        try {
            MessageDigest var2 = MessageDigest.getInstance("MD5");
            var2.reset();
            var2.update(var0);
            return toHexString(var2.digest(), "", var1);
        } catch (NoSuchAlgorithmException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static String toHexString(byte[] var0, String var1, boolean var2) {
        StringBuilder var3 = new StringBuilder();
        byte[] var4 = var0;
        int var5 = var0.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            byte var7 = var4[var6];
            String var8 = Integer.toHexString(255 & var7);
            if (var2) {
                var8 = var8.toUpperCase();
            }

            if (var8.length() == 1) {
                var3.append("0");
            }

            var3.append(var8).append(var1);
        }

        return var3.toString();
    }
}
