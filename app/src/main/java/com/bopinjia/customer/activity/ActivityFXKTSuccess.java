package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ActivityFXKTSuccess extends BaseActivity {

	private TextView mNumber;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_fxsuccess);

		mNumber = (TextView) findViewById(R.id.tv_number);
		getDistributionInfo();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //自动跳转到 店铺信息界面
                    Thread.sleep(2000);
                    Intent i =new Intent();
                    i.putExtra("isFxSuccess","1");
                    forward(ActivityFXSStoreInfo.class,i);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

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
