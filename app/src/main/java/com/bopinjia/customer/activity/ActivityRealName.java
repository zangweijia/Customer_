package com.bopinjia.customer.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
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
import com.bopinjia.customer.util.StringUtils;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityRealName extends BaseActivity {

	@SuppressLint("WrongViewCast")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_real_name);
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 保存
		findViewById(R.id.btn_save).setOnClickListener(this);

		// 读取数据
		try {
			JSONObject data = new JSONObject(getIntent().getStringExtra("RealIdentityData"));
			// 已认证
			((EditText) findViewById(R.id.edt_id_card)).setText(data.getString("IDCard"));
			((EditText) findViewById(R.id.edt_real_name)).setText(data.getString("RealName"));

			if ("0".equals(data.getString("RealNameAuthe"))) {
				((TextView) findViewById(R.id.bottom_bar)).setVisibility(View.GONE);
				((TextView) findViewById(R.id.btn_save)).setEnabled(false);
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		case R.id.btn_return:
			// 返回
			finish();
			break;
		case R.id.btn_save:
			// 输入项目校验
			if (StringUtils.isNull(((EditText) findViewById(R.id.edt_id_card)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_real_name)).getText().toString())) {
				showToast(" 请填写完整的实名信息 ");
				break;
			}

			submitRealIdentily();

			break;
		default:
			break;
		}
	}

	private void submitRealIdentily() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();
		String realName = null;
		try {
			realName = URLEncoder.encode(((EditText) findViewById(R.id.edt_real_name)).getText().toString(), "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		map.put("UserId", getLoginUserId());
		map.put("IDCard", ((EditText) findViewById(R.id.edt_id_card)).getText().toString());
		map.put("RealName", realName);
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

		maps.put("UserId", getLoginUserId());
		maps.put("IDCard", ((EditText) findViewById(R.id.edt_id_card)).getText().toString());
		maps.put("RealName", realName);
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/User/RealIdentity", maps, new CallBack(), this);
	}

	class CallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// showToast("实名认证成功！");
					showToast("认证信息已提交，请耐心等待回复！！！");
					finish();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
