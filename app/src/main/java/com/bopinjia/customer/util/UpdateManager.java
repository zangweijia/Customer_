package com.bopinjia.customer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bopinjia.customer.R;

/**
 * APK更新管理类
 * 
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
	 * 
	 * @param info
	 *            版本信息对象
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

		AlertDialog.Builder builder = new Builder(new ContextThemeWrapper(mContext, R.style.Theme_AppCompat_Light_Dialog));
		dialog = builder.create();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.update_prgress, null);
		progressBar = (ProgressBar) view.findViewById(R.id.pb_update);
		tv_percentage = (TextView) view.findViewById(R.id.tv_percentage);

		TextView t = (TextView) view.findViewById(R.id.tv_cancle);
		t.setOnClickListener(this);

		dialog.setView(view, 0, 0, 0, 0);
		dialog.setCancelable(false);
		dialog.show();
		// 下载apk
		downloadApk();
	}

	@Override
	public void onClick(View v) {
		dialog.dismiss();
		// 终止下载
		isInterceptDownload = true;
		mListener.onUpdateCancel();

	}

	/**
	 * 下载apk
	 */
	private void downloadApk() {
		// 开启另一线程下载
		Thread downLoadThread = new Thread(downApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 从服务器下载新版apk的线程
	 */
	private Runnable downApkRunnable = new Runnable() {
		@Override
		public void run() {
			if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
				// 如果没有SD卡
				Builder builder = new Builder(mContext);
				builder.setTitle("提示");
				builder.setMessage("当前设备无SD卡，数据无法下载");
				builder.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.show();
				return;
			} else {
				try {
					// 服务器上新版apk地址
					URL url = new URL(mDownloadUrl);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					int length = conn.getContentLength();
					InputStream is = conn.getInputStream();
					File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bopinjia/");
					if (!file.exists()) {
						// 如果文件夹不存在,则创建
						file.mkdir();
					}
					// 下载服务器中新版本软件（写文件）
					String apkFile = Environment.getExternalStorageDirectory().getAbsolutePath()
							+ "/bopinjia/Customer.apk";
					File ApkFile = new File(apkFile);
					FileOutputStream fos = new FileOutputStream(ApkFile);
					int count = 0;
					byte buf[] = new byte[1024];
					do {
						int numRead = is.read(buf);
						count += numRead;
						// 更新进度条
						progress = (int) (((float) count / length) * 100);
						handler.sendEmptyMessage(1);
						if (numRead <= 0) {
							// 下载完成通知安装
							handler.sendEmptyMessage(0);
							break;
						}
						fos.write(buf, 0, numRead);
						// 当点击取消时，则停止下载
					} while (!isInterceptDownload);

					fos.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * 声明一个handler来跟进进度条
	 */
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				// 更新进度情况
				tv_percentage.setText(progress + "%");
				progressBar.setProgress(progress);
				break;
			case 0:
				progressBar.setVisibility(View.INVISIBLE);
				mListener.onUpdateFinish();
				// 安装apk文件
				installApk();
				break;
			default:
				break;
			}
		};
	};

	private TextView tv_percentage;

	private AlertDialog dialog;

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
