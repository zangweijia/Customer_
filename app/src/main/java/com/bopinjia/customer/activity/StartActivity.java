package com.bopinjia.customer.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.UpdateManager;
import com.bopinjia.customer.util.UpdateManager.OnUpdateCancelListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator 启动界面
 */
public class StartActivity extends BaseActivity implements OnUpdateCancelListener {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		//initWindow();
		if (!this.isTaskRoot()) {
			Intent intent = getIntent();
			if (intent != null) {
				String action = intent.getAction();
				if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
					finish();
					return;
				}
			}
		}

//        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
//
//            finish();
//
//            return;
//        }

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					getVersion();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}

	@TargetApi(19)
	private void initWindow() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			// 如果仅仅是用来判断网络连接则可以使用 cm.getActiveNetworkInfo().isAvailable();
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void forward() {
		getAdvertisement();

	}

	private void GoNext() {
		String s = getBopinjiaSharedPreference("FirstEnter");
		if (s != null && s.equals("1")) {
			String shopid = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
			if (shopid == null || shopid.length() <= 0) {
				Intent i = new Intent();
				i.putExtra("type", 1);
				forward(ActivityShopList.class, i);
				finish();
			} else {
				forward(ActivityHome.class);
				overridePendingTransition(R.anim.logo_anim_in, R.anim.logo_anim_out);
				finish();
			}
		} else {
			forward(ActivityWelcome.class);
			overridePendingTransition(R.anim.logo_anim_in, R.anim.logo_anim_out);
			finish();
		}
	}

	@Override
	public void onUpdateCancel() {
		forward();
		finish();
	}

	/**
	 * 检查当前网络是否可用
	 *
	 * @param
	 * @return
	 */

	public boolean isNetworkAvailable(Activity activity) {
		Context context = activity.getApplicationContext();
		// 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void getVersion() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("Id", "3");
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/S/V?Id=" + "3" + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						parse(result);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				showToast("网络连接失败");
				forward();
				finish();
			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});

	}

	private void parse(String result) {
		try {
			JSONObject data1 = new JSONObject(result);
			JSONObject data = data1.getJSONObject("Data");
			// 获取packagemanager的实例
			PackageManager packageManager = getPackageManager();
			// getPackageName()是你当前类的包名，0代表是获取版本信息
			PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
			if (Integer.parseInt(data.getString("UpdateSoftwareVersion")) <= packInfo.versionCode) {
				forward();
				mScreenManager.popActivity(this);
			} else {
				UpdateManager um = new UpdateManager(this, data.getString("DownloadAddress"), this, R.id.m,
						StartActivity.this);
				um.checkUpdate(data.getInt("UpdateSoftwareVersion"));
			}
		} catch (Exception e) {
			showSysErr(e);
		}
	}

	@Override
	public void onUpdateFinish() {
		finish();
	}

	private void getAdvertisement() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Startpage/GetStartpage?Sign=" + NetUtils.getSign(map) + "&Ts="
				+ Ts;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					JSONObject data = jo.getJSONObject("Data");
					String state = data.getString("State");

					if (!state.equals("1")) {
						Intent i = new Intent();
						i.putExtra("url", data.getString("PageUrl"));
						forward(ActivityAdvertisement.class, i);
					} else {
						GoNext();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				GoNext();
			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});

	}
}
