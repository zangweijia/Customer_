package com.bopinjia.customer.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bopinjia.customer.R;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * APK更新管理类
 */
public class UpdateManager implements android.view.View.OnClickListener {

    // 上下文对象
    private Context mContext;

    // 下载进度条
    private ProgressBar progressBar;
    // 是否终止下载
    private boolean isInterceptDownload = false;
    // 进度条显示数值
    private int progress = 0;

    private String mDownloadUrl;
    private int id;
    private OnUpdateCancelListener mListener;
    private Activity act;

    /**
     * 参数为Context(上下文activity)的构造函数
     *
     * @param context
     */
    public UpdateManager(Context context, String downloadUrl, OnUpdateCancelListener listener, int id, Activity act) {
        this.mContext = context;
        this.mDownloadUrl = downloadUrl;
        this.mListener = listener;
        this.id = id;
        this.act = act;
    }

    public void checkUpdate(int versionCode) throws NameNotFoundException {
        // 获取当前软件包信息
        PackageInfo pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
                PackageManager.GET_CONFIGURATIONS);
        if (versionCode > pi.versionCode) {
            // 如果当前版本号小于服务端版本号,则弹出提示更新对话框
            showUpdateDialog();
        }
    }

    UpDataDialog releasePopWindow;

    /**
     * 提示更新对话框
     * 版本信息对象
     */
    private void showUpdateDialog() {
        releasePopWindow = new UpDataDialog(act, itemsOnClick);
        releasePopWindow.showAtLocation(act.findViewById(id), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

    }

    /**
     * 更新对话框点击事件
     */
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bt_send:// 确定
                    releasePopWindow.dismiss();
                    showDownloadDialog();
                    break;
                case R.id.bt_cancel:// 取消
                    releasePopWindow.dismiss();
                    mListener.onUpdateCancel();
                    break;
            }
        }
    };

    /**
     * 弹出下载框
     */
    @SuppressLint("InflateParams")
    private void showDownloadDialog() {
        // 下载apk
        downloadApk();
    }

    @Override
    public void onClick(View v) {
        // 终止下载
        isInterceptDownload = true;
        mListener.onUpdateCancel();

    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        // 开启另一线程下载
        URL url = null;
        try {
            url = new URL(mDownloadUrl);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bopinjia/");
            if (!file.exists()) {
                // 如果文件夹不存在,则创建
                file.mkdir();
            }
            // 下载服务器中新版本软件（写文件）
            String apkFile = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/bopinjia/Customer.apk";
            File ApkFile = new File(apkFile);
            downloadFile(mDownloadUrl, apkFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }


    ProgressDialog progressDialog;

    private void downloadFile(final String url, String path) {
        progressDialog = new ProgressDialog(mContext);
        RequestParams requestParams = new RequestParams(url);
        requestParams.setSaveFilePath(path);
        x.http().get(requestParams, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {
            }

            @Override
            public void onStarted() {
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMessage("亲，努力下载中。。。");
                progressDialog.show();
                progressDialog.setMax((int) total / 1024  );
                progressDialog.setProgress((int) current / 1024 );
            }

            @Override
            public void onSuccess(File result) {
                Toast.makeText(mContext, "下载成功", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                mListener.onUpdateFinish();
                // 安装apk文件
                installApk();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
                Toast.makeText(mContext, "下载失败，网络连接错误，请重新下载", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                showUpdateDialog();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 安装apk
     */
    private void installApk() {
        // 获取当前sdcard存储路径
        File apkfile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bopinjia/Customer.apk");
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        // 安装，如果签名不一致，可能出现程序未安装提示
        i.setDataAndType(Uri.fromFile(new File(apkfile.getAbsolutePath())), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    public interface OnUpdateCancelListener {
        public void onUpdateFinish();

        public void onUpdateCancel();
    }

}
