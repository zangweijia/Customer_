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
import android.widget.TextView;

public class ActivityCustomerNews extends BaseActivity {
	private WebView mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_customer_news);

		findViewById(R.id.btn_return).setOnClickListener(this);

		// 消息详细控件的格式化
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		mContent = (WebView) findViewById(R.id.wv_message);
		mContent.clearCache(true);
		mContent.setInitialScale(100 * (dm.widthPixels - 40) / 480);
		mContent.setVerticalScrollBarEnabled(false);

		String id = getIntent().getStringExtra("newsId");
		if (id.equals("7")) {
			((TextView) findViewById(R.id.txt_page_title)).setText("舶品家注册协议");
			mContent.loadUrl("file:///android_asset/fwtk.html");

		} else {
			search();
		}

	}

	/**
	 * 检索处理
	 */
	private void search() {
		getContent();
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		default:
		case R.id.btn_return:
			backward();
			break;
		}
	}

	private void getContent() {
		Intent intent = getIntent();

		String newsId = intent.getStringExtra("newsId");

		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("NewsId", newsId);
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

		XutilsHttp.getInstance().get(
				Constants.WEBAPI_ADDRESS + "api/News/Detail?NewsId=" + newsId + "&Sign=" + Sign + "&Ts=" + Ts, null,
				new getContentCallBack(), this);
	}

	class getContentCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					JSONObject data = jo.getJSONObject("Data");

					String contentText = data.getString("NewsContent");
					mContent.loadData(contentText, "text/html; charset=UTF-8", null);
					((TextView) findViewById(R.id.txt_page_title)).setText(data.getString("NewsTitle"));
					// ((TextView)
					// findViewById(R.id.txt_title)).setText(data.getString("NewsTitle"));
					// ((TextView)
					// findViewById(R.id.txt_time)).setText(data.getString("CreateTime"));
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
