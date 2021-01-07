
package com.alien.crack_wechat_robot.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: chenjishi
 * Date: 13-10-12
 * Time: AM11:45
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {

    private static final int BUFFER_SIZE = 8 * 1024; // 8 KB

    /**
     * 通过文件路劲获取保存到本地的的Model
     *
     * @param path
     * @return
     */
    public static Object unserializeObject(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        ObjectInputStream ois = null;
        Object o = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path));
            o = ois.readObject();
        } catch (Exception e) {
            file.delete();
            return null;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
        }
        return o;
    }

    /**
     * 将文件序列化并且保存到本地
     *
     * @param path
     * @param o
     * @return
     */
    public static boolean serializeObject(String path, Object o) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(o);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                file.delete();
            }
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否用写sd卡目录的权限
     *
     * @return
     */
    public static boolean isExternalStorageCanWrite() {
        return Environment.getExternalStorageDirectory().canWrite();
    }

    /**
     * check whether the sd card is mounted on the phone
     *
     * @return whether the sd card is mounted on the phone
     * @author jiayuan
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * get the free space of a specified file path
     *
     * @param path: the specified file path
     * @return the free space size or 0 if the path is not exist or is not a
     * real file path
     */
    public static long getFreeSpace(File path) {
        long size = 0;
        try {
            if (path.exists() && path.isDirectory()) {
                StatFs stat = new StatFs(path.getAbsolutePath());
                size = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
            }
        } catch (Throwable e) {
            /**
             * 本来此处有可能出现一个exception，后来想捕获所有异常算了，检查下可用空间不能就崩掉
             * java.lang.IllegalArgumentException: Invalid path: /storage/sdcard0
             at android.os.StatFs.doStat(StatFs.java:46)
             at android.os.StatFs.<init>(StatFs.java:39)
             at com.baidu.homework.common.utils.FileUtils.getFreeSpace(Unknown Source)
             */
        }
        return size;
    }

    public static boolean writeFile(String path, byte[] data) {
        if (data == null) {
            return false;
        } else {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(path));
                bos.write(data, 0, data.length);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 保存下载的文件到本地
     *
     * @param savePath 保存路径
     * @param inFile   要保存的文件
     * @return 是否保存成功
     */
    public static boolean writeFile(String savePath, File inFile) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(inFile));
            bos = new BufferedOutputStream(new FileOutputStream(savePath));
            final byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeJsonRecord(String path, ArrayList<JSONObject> content) {
        if (null == content || content.isEmpty()) {
            return false;
        }
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        BufferedWriter writer = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            int len = content.size();
            for (int i = 0; i < len; i++) {
                JSONObject jsonObject = content.get(i);
                writer.write(jsonObject.toString() + "\n");
            }
            return true;
        } catch (IOException e) {
            //todo  Common Execption Handler;
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeUtfStrings(String path, List<String> content) {

        if (null == content || content.isEmpty()) {
            return false;
        }
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        System.err.println("log_..FileUtils.writeUtfStrings." + "path = [" + path + "], content = [" + content.size() + "]");
        DataOutputStream writer = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            writer = new DataOutputStream(fos);

            int len = content.size();
            writer.writeInt(len);
            for (int i = 0; i < len; i++) {
                writer.writeUTF(TextUtils.isEmpty(content.get(i)) ? "" : content.get(i));
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            //todo  Common Execption Handler;
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeFile(String path, String content) {
        if (null == content) {
            return false;
        }
        BufferedWriter writer = null;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(content);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            //todo  Common Execption Handler;
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] readFile(File file) {
        byte[] result = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            int fileSize = (int) file.length();
            result = new byte[fileSize];
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bis.read(result, 0, fileSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static boolean delFile(File file) {
        if (null == file) {
            return false;
        }
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static boolean delFile(String path) {
        return delFile(new File(path));
    }

    /**
     * 清空目录的内容（不包括目录本身)
     *
     * @param dir
     */
    public static void clearDir(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDir(dir);
                    } else {
                        delFile(file);
                    }
                }
            }
        }
    }

    /**
     * 清空整个目录（包括目录本身)
     *
     * @param dir
     */
    public static void deleteDir(File dir) {
        if (!dir.exists()) return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                dir.delete();
                return;
            }
            for (int i = 0; i < files.length; i++) {
                deleteDir(files[i]);
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    public static byte[] readInputStream(InputStream input) {
        byte[] data = null;
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            data = output.toByteArray();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return data;
    }

    /**
     * 复制IO流
     *
     * @param is
     * @param os
     * @throws IOException
     */
    public static void copyStream(InputStream is, OutputStream os) throws IOException {

        if (is == null || os == null) {
            throw new IOException("Argument is null.");
        }

        byte[] bytes = new byte[BUFFER_SIZE];
        while (true) {
            int count = is.read(bytes, 0, BUFFER_SIZE);
            if (count == -1) {
                break;
            }
            os.write(bytes, 0, count);
        }
    }

    public static byte[] gunzip(byte[] input) {
        GZIPInputStream inputStream = null;
        ByteArrayOutputStream bos = null;
        try {
            inputStream = new GZIPInputStream(new ByteArrayInputStream(input));
            bos = new ByteArrayOutputStream();
            copyStream(inputStream, bos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (bos != null) {
            return bos.toByteArray();
        } else {
            return null;
        }
    }

    /**
     * 复制文件
     *
     * @param src 源文件
     * @param des 目的文件
     * @throws IOException
     */
    public static void copy(File src, File des) throws IOException {
        if (des.exists()) {
            des.delete();
        }
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(des);
        try {
            FileUtils.copyStream(fis, fos);
        } catch (IOException e) {
            throw e;
        } finally {
            fis.close();
            fos.close();
        }
    }

    /**
     * 剪切文件
     *
     * @param src 源文件
     * @param des 目的文件
     * @throws IOException
     */
    public static void cut(File src, File des) throws IOException {
        copy(src, des);
        delFile(src);
    }

    /**
     * 从Raw资源中读取一张图片到外部存储卡的一个文件
     *
     * @param context
     * @param rawResId
     * @param file
     * @return 读取失败或者外部存储卡不存在返回false
     */
    public static boolean readRawIntoFile(Context context, int rawResId, File file) {
        OutputStream os = null;
        InputStream is = null;
        if (file.exists()) {
            FileUtils.delFile(file);
        }
        try {
            os = new FileOutputStream(file);
            is = context.getResources().openRawResource(rawResId);
            FileUtils.copyStream(is, os);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean unzip(String filePath, String outPath, boolean delSrc, int bufSize) {
        ZipInputStream inputStream = null;
        BufferedOutputStream out = null;
        InputStream in = null;
        try {
            inputStream = new ZipInputStream(new FileInputStream(filePath));
            File outDirectory = new File(outPath);
            if (!outDirectory.isDirectory()) {
                if (!outDirectory.mkdirs()) {
                    throw new FileNotFoundException();
                }
            }
            in = new BufferedInputStream(inputStream);

            bufSize = Math.max(bufSize, 1024);
            ZipEntry entry;
            byte[] buffer = new byte[bufSize];
            while ((entry = inputStream.getNextEntry()) != null) {
                String name = entry.getName();
                if (!entry.isDirectory()) {
                    File file = new File(outPath, name);
                    File folder = file.getParentFile();
                    if (folder == null || (!folder.exists() && !folder.mkdirs())) {
                        throw new FileNotFoundException();
                    }
                    out = new BufferedOutputStream(new FileOutputStream(file));
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    out.flush();
                }
            }
            return true;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (delSrc) {
                FileUtils.delFile(filePath);
            }
        }
        return false;
    }

    public static boolean isApk(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry ze;
            while ((ze = zipInputStream.getNextEntry()) != null) {
                if (ze.getName().equals("AndroidManifest.xml")) {
                    return true;
                }
            }
        } catch (Exception e) {

        } finally {
            try {
                if (zipInputStream != null) {
                    zipInputStream.close();
                }
            } catch (IOException e) {
            }
        }
        return false;
    }

    public static String md5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    public static boolean checkFileExist(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        } else {
            return new File(path).exists();
        }
    }

    /**
     * 复制文件
     * @param oldPath
     * @param newPath
     */
    public static void copyFile(String oldPath, String newPath) {
        try (FileInputStream in = new FileInputStream(oldPath);
             FileOutputStream out = new FileOutputStream(newPath)) {
            byte[] buff = new byte[10240];
            int n;
            while ((n = in.read(buff)) != -1) {
                out.write(buff, 0, n);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
