package com.bopinjia.customer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.DateFormat;

import com.bopinjia.customer.R;

/**
 * SD卡相关操作
 * 
 * @author yushen 2015/12/28
 */
public class StorageUtil {

    private static final String BOPINJIA_DIR = "bopinjia";

    /**
     * 保存图片文件
     * 
     * @param context
     *            上下文
     * @param bitmap
     *            图片数据
     * @param phone
     *            当前用户绑定的手机号
     * @param imgType
     *            图片类型（头像、证件照等）
     */
    public static String saveImage(Context context, Bitmap bitmap, String phone, String imgType) {
        // SD卡状态
        String sdStatus = Environment.getExternalStorageState();
        // 检测SD卡是否可用
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            StringUtils.showToast(context, R.string.msg_err_sdcard_invalid);
            return null;
        }

        // 当前用户的个人文件夹
        String folderPath = getAppFileDirectory() + phone;
        File folder = new File(folderPath);
        if (!folder.exists()) {
            // 创建文件夹
            folder.mkdirs();
        }

        // 头像文件的保存文件名
        String now = DateFormat.format("yyyyMMddHHmmss", Calendar.getInstance(Locale.CHINA)) + ".jpg";
        String fileName = folderPath + File.separator + imgType + now;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            // 把数据写入文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                StringUtils.showToast(context, R.string.msg_err_system);
            }
        }
		return fileName;
    }
    
  

    /**
     * 获取本应用的根目录。
     * 
     * @return 本应用的根目录。 【sdcard根目录/bopinjia/】
     */
    public static String getAppFileDirectory() {
        // SD卡根目录。
        String sdcardPath = Environment.getExternalStorageDirectory().getPath();
        // 本应用根目录。
        String rootDirPath = sdcardPath + File.separator + BOPINJIA_DIR + File.separator;
        return rootDirPath;
    }

    /**
     * 保存图片文件
     * 
     * @param context
     *            上下文
     * @param fileContent
     *            文件内容
     */
    public static void saveFile(Context context, String fileContent) {
        // SD卡状态
        String sdStatus = Environment.getExternalStorageState();
        // 检测SD卡是否可用
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            StringUtils.showToast(context, R.string.msg_err_sdcard_invalid);
            return;
        }

        // 当前用户的个人文件夹
        String folderPath = getAppFileDirectory() + "files";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            // 创建文件夹
            folder.mkdirs();
        }

        // 头像文件的保存文件名
        String now = DateFormat.format("yyyyMMddHHmmss", Calendar.getInstance(Locale.CHINA)) + ".txt";
        String fileName = folderPath + File.separator + now;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
            // 把数据写入文件
            fos.write(fileContent.getBytes());
        } catch (FileNotFoundException e) {
            StringUtils.showToast(context, R.string.msg_err_system);
        } catch (IOException e) {
            StringUtils.showToast(context, R.string.msg_err_system);
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                StringUtils.showToast(context, R.string.msg_err_system);
            }
        }
    }

    public static void saveErrFile(Context context, Exception e) {
        StackTraceElement[] stacks = e.getStackTrace();
        // SD卡状态
        String sdStatus = Environment.getExternalStorageState();
        // 检测SD卡是否可用
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            StringUtils.showToast(context, R.string.msg_err_sdcard_invalid);
            return;
        }

        // 当前用户的个人文件夹
        String folderPath = getAppFileDirectory() + "files";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            // 创建文件夹
            folder.mkdirs();
        }

        // 头像文件的保存文件名
        String now = DateFormat.format("yyyyMMdd", Calendar.getInstance(Locale.CHINA)) + ".txt";
        String fileName = folderPath + File.separator + "ERROR_" + now;
        FileOutputStream fos = null;
        try {

            File file = new File(fileName);

            fos = new FileOutputStream(fileName, file.exists());

            // 把数据写入文件
            for (StackTraceElement stack : stacks) {
                fos.write((DateFormat.format("yyyy-MM-dd HH:mm:ss ： ", Calendar.getInstance(Locale.CHINA)) + stack
                                .toString()).getBytes());
            }

        } catch (Exception ex) {
            StringUtils.showToast(context, R.string.msg_err_system);
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException ioe) {
                StringUtils.showToast(context, R.string.msg_err_system);
            }
        }
    }

    public static void saveDebugMsg(Context context, String msg) {
        // SD卡状态
        String sdStatus = Environment.getExternalStorageState();
        // 检测SD卡是否可用
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
            StringUtils.showToast(context, R.string.msg_err_sdcard_invalid);
            return;
        }

        // 当前用户的个人文件夹
        String folderPath = getAppFileDirectory() + "files";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            // 创建文件夹
            folder.mkdirs();
        }

        // 头像文件的保存文件名
        String now = DateFormat.format("yyyyMMdd", Calendar.getInstance(Locale.CHINA)) + ".txt";
        String fileName = folderPath + File.separator + "DEBUG_" + now;
        FileOutputStream fos = null;
        try {

            File file = new File(fileName);

            fos = new FileOutputStream(fileName, file.exists());

            // 把数据写入文件
            fos.write((DateFormat.format("yyyy-MM-dd HH:mm:ss ： ", Calendar.getInstance(Locale.CHINA)) + msg + "\n")
                            .getBytes());

        } catch (Exception ex) {
            StringUtils.showToast(context, R.string.msg_err_system);
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException ioe) {
                StringUtils.showToast(context, R.string.msg_err_system);
            }
        }
    }

    /**
     * 图片压缩
     * 
     * @param srcPath
     * @return
     */
    public static Bitmap compressImageFromFile(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;// 只读边,不读内容
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        float hh = 1280f;//
        float ww = 1024f;//
        int be = 1;
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置采样率

        newOpts.inPreferredConfig = Config.ARGB_8888;// 该模式是默认的,可不设
        newOpts.inPurgeable = true;// 同时设置才会有效
        newOpts.inInputShareable = true;// 。当系统内存不够时候图片自动被回收

        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        // return compressBmpFromBmp(bitmap);//原来的方法调用了这个方法企图进行二次压缩
        // 其实是无效的,大家尽管尝试
        return bitmap;
    }
}
