package com.bopinjia.customer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.tencent.smtt.sdk.CacheManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ActivitySetting extends BaseActivity {

	public static ActivitySetting instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_setting);
		
		instance = this;
		
		findViewById(R.id.ll_setting_anquan).setOnClickListener(this);
		findViewById(R.id.ll_setting_gerenzil).setOnClickListener(this);
		findViewById(R.id.ll_setting_binding).setOnClickListener(this);

		findViewById(R.id.ll_setting_gongyingshanghezuo).setOnClickListener(this);

		findViewById(R.id.ll_setting_guanyu).setOnClickListener(this);
		findViewById(R.id.ll_setting_join).setOnClickListener(this);
		findViewById(R.id.ll_setting_lianxiwomen).setOnClickListener(this);
		findViewById(R.id.ll_setting_licheng).setOnClickListener(this);

		findViewById(R.id.ll_setting_qingli).setOnClickListener(this);
		findViewById(R.id.ll_setting_tiaokuan).setOnClickListener(this);
		findViewById(R.id.ll_setting_zizhi).setOnClickListener(this);

		findViewById(R.id.ll_setting_person_join).setOnClickListener(this);

		findViewById(R.id.btn_exit).setOnClickListener(this);

		findViewById(R.id.btn_return).setOnClickListener(this);
		// GetIsMerchant();
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent();
		switch (v.getId()) {
		case R.id.btn_return:
			// 返回
			finish();
			break;
		case R.id.btn_exit:
			// 退出登录
			putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "0");
			removeSharedPreferences(Constants.KEY_PREFERENCE_USER_INFO);
			removeSharedPreferences(Constants.KEY_PREFERENCE_PASSWORD);
			removeSharedPreferences(Constants.KEY_PREFERENCE_USER_ID);
			showToast("已退出当前账号");
			backward();
			break;
		case R.id.ll_setting_anquan:
			// 账户安全
			forward(ActivityUserSecurity.class);
			break;
		case R.id.ll_setting_gerenzil:
			// 个人资料
			forward(ActivityMyself.class);
			break;
		case R.id.ll_setting_gongyingshanghezuo:
			// 供应商合作
			// forward(ActivityGongYingShangJoin.class);
			// GetIsMerchant();
			forward(ActivityGongYingShangJoin.class);
			break;
		case R.id.ll_setting_guanyu:
			// 关于舶品家
			i.putExtra("newsId", "1");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.ll_setting_join:
			// 我要加盟
			i.putExtra("newsId", "5");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.ll_setting_lianxiwomen:
			// 联系我们
			i.putExtra("newsId", "4");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.ll_setting_licheng:
			// 舶品家历程
			i.putExtra("newsId", "3");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.ll_setting_qingli:
			// 清理缓存
			File file = CacheManager.getCacheFileBaseDir();
			if (file != null && file.exists() && file.isDirectory()) {
				for (File item : file.listFiles()) {
					item.delete();
				}
				file.delete();
			}
			Context context = getApplicationContext();
			context.deleteDatabase("webview.db");
			context.deleteDatabase("webviewCache.db");
			Toast.makeText(ActivitySetting.this, "缓存清理成功  ", Toast.LENGTH_SHORT).show();
			break;
		case R.id.ll_setting_tiaokuan:
			// 舶品家服务条款
			i.putExtra("newsId", "6");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.ll_setting_zizhi:
			// 舶品家资质
			i.putExtra("newsId", "2");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.ll_setting_person_join:
			showToast("该功能暂未开放");
			break;

		case R.id.ll_setting_binding:
			// 账户绑定
			forward(ActivityBinding.class);
			break;

		default:
			break;
		}
	}

	/**
	 * 查询是否提交过供应商加盟申请
	 */
	private void GetIsMerchant() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/UserSupplier/Exists?UserId=" + getLoginUserId() + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new CallBack(), this);
	}

	class CallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					findViewById(R.id.ll_setting_gongyingshanghezuo).setEnabled(false);
				} else if (jsonresult.equals("2")) {
					findViewById(R.id.ll_setting_gongyingshanghezuo).setEnabled(true);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
