package com.bopinjia.customer.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class ActivityFXKTSuccess extends BaseActivity {

	private TextView mNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_fxsuccess);

		mNumber = (TextView) findViewById(R.id.tv_number);
		getDistributionInfo();
		// 店铺信息
		findViewById(R.id.tv_to_DPXX).setOnClickListener(this);
		// 分销管理
		findViewById(R.id.tv_to_FXGL).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_to_FXGL:

			break;
		case R.id.tv_to_DPXX:
			forward(ActivityFXSStoreInfo.class);
			finish();
			break;

		default:
			break;
		}
	}

	private void getDistributionInfo() {
		String s = getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GetGDSUserInfo?UserId=" + s + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getDistributionInfoCallBack(),this);
	}

	class getDistributionInfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");
					// 分销商绑定的店铺
					String MDGDSR_MDUserID = Data.getString("MDGDSR_MDUserID");
					// 把分销商绑定的门店 保存。
					putSharedPreferences(Constants.FXSMD, MDGDSR_MDUserID);

					// 分销会员编号
					mNumber.setText(Data.getString("MDGDSM_Number"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			forward(ActivityFXSStoreInfo.class);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
