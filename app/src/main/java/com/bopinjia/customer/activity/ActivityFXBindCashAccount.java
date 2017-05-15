package com.bopinjia.customer.activity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ReflectAccountBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.google.gson.Gson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityFXBindCashAccount extends BaseActivity {

	private TextView mTiTleBack;
	private TextView mTiTleName, mTiTleReflect;

	/** 支付宝账号 */
	@ViewInject(R.id.tv_alipay_number)
	private TextView mTVAlipayNum;
	/** 微信账号 */
	@ViewInject(R.id.tv_wx_number)
	private TextView mTVWXNum;
	/** 银联账号 */
	@ViewInject(R.id.tv_yl_number)
	private TextView mTVYLNum;

	/** 银行卡名称 */
	@ViewInject(R.id.tv_yl_name)
	private TextView mYHKname;

	/** 支付宝按钮 */
	@ViewInject(R.id.rl_alipay)
	private RelativeLayout mBtnalipay;
	/** 微信按钮 */
	@ViewInject(R.id.ll_wx)
	private LinearLayout mBtnWX;
	/** 银联按钮 */
	@ViewInject(R.id.ll_yl)
	private LinearLayout mBtnyl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_fxbind_cash_account);
		x.view().inject(this);

		setTitle();
		init();
	}

	private void init() {
		mBtnalipay.setOnClickListener(this);
		mBtnWX.setOnClickListener(this);
		mBtnyl.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		getAccountList();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);

		mTiTleName.setText("提现账号");
		mTiTleBack.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent();
		switch (v.getId()) {
		case R.id.btn_return:
			finish();
			break;
		case R.id.rl_alipay:
			i.putExtra("type", "1");
			i.putExtra("json", mjsonarray.toString());
			forward(ActivityFXSubmitAccount.class, i);
			break;
		case R.id.ll_wx:
			i.putExtra("type", "2");
			i.putExtra("json", mjsonarray.toString());
			forward(ActivityFXSubmitAccount.class, i);
			break;
		case R.id.ll_yl:
			i.putExtra("type", "3");
			i.putExtra("json", mjsonarray.toString());
			forward(ActivityFXSubmitAccount.class, i);
			break;
		default:
			break;
		}
	}

	private String mjsonarray;

	private void getAccountList() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());

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

		String url = Constants.WEBAPI_ADDRESS + "api/UserAccount/List?UserId=" + getLoginUserId() + "&Sign=" + Sign
				+ "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getAccountListCallBack(), this);
	}

	class getAccountListCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				mjsonarray = result;
				if (jsonresult.equals("1")) {
					JSONArray jsonarray = jo.getJSONArray("Data");
					Gson gson = new Gson();
					for (int i = 0; i < jsonarray.length(); i++) {
						ReflectAccountBean mReflectBean = new ReflectAccountBean();
						mReflectBean = gson.fromJson(jsonarray.get(i).toString(), ReflectAccountBean.class);
						if (mReflectBean.getUserAccountTypeId().equals("1")) {
							mTVAlipayNum.setText(mReflectBean.getUserAccounts());
						} else if (mReflectBean.getUserAccountTypeId().equals("2")) {
							mTVWXNum.setText(mReflectBean.getUserAccounts());
						} else if (mReflectBean.getUserAccountTypeId().equals("3")) {
							mTVYLNum.setText(mReflectBean.getUserAccounts());
							mYHKname.setText(mReflectBean.getUserAccountName());
						}
					}
				} else {

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

}
