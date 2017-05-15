package com.bopinjia.customer.activity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;

public class ActivityFXIntroduce extends BaseActivity {

	private WebView mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_distribution);
		init();

	}

	private void init() {
		findViewById(R.id.btn_kthy).setOnClickListener(this);
		findViewById(R.id.btn_return).setOnClickListener(this);

		// 消息详细控件的格式化
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		mContent = (WebView) findViewById(R.id.mwv);
		mContent.clearCache(true);
		mContent.setInitialScale(100 * (dm.widthPixels - 40) / 480);
		mContent.setVerticalScrollBarEnabled(false);

		get();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_kthy:
			Intent i = new Intent();
			i.putExtra("type", "1");
			// type = 1 申请时进入
			forward(ActivityFXDisLevel.class, i);
			finish();
			break;
		case R.id.btn_return:
			finish();
			break;
		default:
			break;
		}
	}

	private void get() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("NewsId", "9");
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			stringBuffer.append(key).append("=").append(map.get(key)).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);

		String Sign = MD5.Md5(stringBuffer.toString());

		String url = Constants.WEBAPI_ADDRESS + "api/News/Detail?NewsId=" + "9" + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getCallBack(),this);
	}

	class getCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					JSONObject data = jo.getJSONObject("Data");

					String contentText = data.getString("NewsContent");
					mContent.loadData(contentText, "text/html; charset=UTF-8", null);

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
