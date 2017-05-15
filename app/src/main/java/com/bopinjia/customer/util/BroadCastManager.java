package com.bopinjia.customer.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

public class BroadCastManager {
	private static BroadCastManager broadCastManager = new BroadCastManager();

	public static BroadCastManager getInstance() {
		return broadCastManager;
	}

	// 注册广播接收者
	public void registerReceiver(Activity activity, BroadcastReceiver receiver, IntentFilter filter) {
		activity.registerReceiver(receiver, filter);
	}

	// 注销广播接收者
	public void unregisterReceiver(Activity activity, BroadcastReceiver receiver) {
		activity.unregisterReceiver(receiver);
	}

	// 发送广播
	public void sendBroadCast(Activity activity, Intent intent) {
		activity.sendBroadcast(intent);
	}

}
