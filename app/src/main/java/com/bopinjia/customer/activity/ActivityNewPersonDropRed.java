package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterDayDropRed;
import com.bopinjia.customer.bean.DayDropRedBean;
import com.bopinjia.customer.bean.LoginBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.NoScrollListview;
import com.google.gson.Gson;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;

public class ActivityNewPersonDropRed extends BaseActivity {

	NoScrollListview red_list;
	private String userid;
	private String mPhone;
	private String mD5Pass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_new_person_drop_red);

		userid = getIntent().getStringExtra("userid");

		mPhone = getIntent().getStringExtra("phone");
		mD5Pass = getIntent().getStringExtra("pas");

		red_list = (NoScrollListview) findViewById(R.id.red_list);
		getList();

		findViewById(R.id.tv_next).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Login();

			}
		});
	}

	/**
	 * 登录
	 */
	public void Login() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();
		map.put("RegisterPhone", mPhone);
		map.put("Password", mD5Pass);
		map.put("DeviceType", "0");
		map.put("DeviceToken", "0");
		map.put("UUID", getIMEI());
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
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("RegisterPhone", mPhone);
		maps.put("Password", mD5Pass);
		maps.put("DeviceType", "0");
		maps.put("DeviceToken", "0");
		maps.put("UUID", getIMEI());
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/login", maps, new LoginCallBack(), this);

	}

	class LoginCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parses(result);
				} else {
					showToast("登陆失败请手动登录");
					finish();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void parses(String str) {
		Gson gson = new Gson();
		LoginBean mLogBean = new LoginBean();
		mLogBean = gson.fromJson(str, LoginBean.class);

		putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "1");

		String userId = mLogBean.getData().getUserId().toString();
		putSharedPreferences(Constants.KEY_PREFERENCE_USER_ID, userId);
		putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, mLogBean.getData().getRegisterPhone());
		putSharedPreferences(Constants.KEY_PREFERENCE_PASSWORD, mD5Pass);
		putSharedPreferences(Constants.KEY_PREFERENCE_USER_INFO, str);

		ActivityLogin.instance.finish();

		finish();

	}

	private void getList() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", userid);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("MdUserId", getBindingShop());
		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			stringBuffer.append(key).append("=").append(map.get(key)).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);
		String Sign = MD5.Md5(stringBuffer.toString());

		String url = Constants.WEBAPI_ADDRESS + "api/RedPacketReceive/List?UserId=" + userid + "&MdUserId="
				+ getBindingShop() + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getListCallBack(), this);
	}

	class getListCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					parse(result);

				} else {
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void parse(String str) {
		try {
			JSONObject jo = new JSONObject(str);
			JSONArray mDate = jo.getJSONArray("Data");

			if (mDate != null || mDate.length() > 0) {
				List<DayDropRedBean> list = new ArrayList<DayDropRedBean>();

				for (int i = 0; i < mDate.length(); i++) {
					JSONObject data = mDate.getJSONObject(i);
					DayDropRedBean dr = new DayDropRedBean();

					dr.setAmount(String.valueOf(Math.floor(Double.parseDouble(data.getString("RPR_Price")))));

					dr.setRed_type("满" + data.getString("RPR_FPrice") + "元可用");
					if (data.getString("RPR_OrderType").equals("0")) {
						dr.setRed_detail("限现货区使用");
					} else if (data.getString("RPR_OrderType").equals("1")) {
						dr.setRed_detail("限直邮区使用");
					}
					dr.setRed_time(data.getString("RPR_Date") + "-" + data.getString("RPR_EndDate"));
					list.add(dr);
				}
				AdapterDayDropRed mAdapter = new AdapterDayDropRed(list, ActivityNewPersonDropRed.this,
						R.layout.wj_item_day_drop_red);
				red_list.setAdapter(mAdapter);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
