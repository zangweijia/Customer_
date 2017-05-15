package com.bopinjia.customer.util;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class ToastUtils {

	private static Toast mToast;

	/** 之前显示的内容 */
	private static String oldMsg;
	/** Toast对象 */
	private static Toast toast = null;
	/** 第一次时间 */
	private static long oneTime = 0;
	/** 第二次时间 */
	private static long twoTime = 0;

	/**
	 * 普通的toast提示
	 */
	public static void showNOrmalToast(Context context, String message) {

		ToastUtils.cancel();
		if (toast == null) {
			toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			toast.show();
			oneTime = System.currentTimeMillis();
		} else {
			twoTime = System.currentTimeMillis();
			if (message.equals(oldMsg)) {
				if (twoTime - oneTime > Toast.LENGTH_SHORT) {
					toast.show();
				}
			} else {
				oldMsg = message;
				toast.setText(message);
				toast.show();
			}
		}
		oneTime = twoTime;
	}

	/**
	 * toast取消
	 */
	public static void cancel() {

		if (mToast != null) {
			mToast.cancel();
			mToast = null;
		}

	}

}
