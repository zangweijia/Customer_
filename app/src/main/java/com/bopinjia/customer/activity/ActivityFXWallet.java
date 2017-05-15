package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterMyWalletList;
import com.bopinjia.customer.bean.MyWalletBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActivityFXWallet extends BaseActivity {

	private TextView mTiTleBack;
	private TextView mTiTleName;
	private RelativeLayout mTiTleMain;
	private ListView mWallet;
	private TextView mWalletList;

	@ViewInject(R.id.ll_binding_account)
	private LinearLayout mBindingAccount;

	/** 余额 */
	@ViewInject(R.id.tv_balance)
	private TextView mBalance;
	/** 累计收益 */
	@ViewInject(R.id.tv_total_amount)
	private TextView mTotalAmount;
	/** 账号数量 */
	@ViewInject(R.id.tv_account_number)
	private TextView mAccountNumber;

	/** 已提现 */
	@ViewInject(R.id.tv_reflect)
	private TextView mTomyInmoney;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_my_wallet);
		x.view().inject(this);
		setTitle();
		init();
		getMyWalletList("0", "1", "5");

	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);

		View v = (View) mTiTle.findViewById(R.id.view);
		v.setVisibility(View.GONE);

		mWalletList = (TextView) mTiTle.findViewById(R.id.btn_edit);
		mWalletList.setBackgroundResource(R.drawable.ic_my_wallet_list);

		mTiTleMain = (RelativeLayout) mTiTle.findViewById(R.id.rl_main);
		mTiTleMain.setBackgroundResource(R.color.bg_ff4f04);

		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTiTleName.setText("钱包");
		mTiTleName.setTextColor(getResources().getColor(R.color.bg_ffffff));

		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleBack.setBackgroundResource(R.drawable.ic_back_white);

	}

	@Event(value = { R.id.btn_edit, R.id.ll_binding_account, R.id.btn_return })
	private void getEvent(View v) {

		switch (v.getId()) {
		case R.id.btn_edit:
			forward(ActivityFXWalletList.class);
			break;
		case R.id.ll_binding_account:
			forward(ActivityFXBindCashAccount.class);
			break;
		case R.id.btn_return:
			finish();
			break;
		default:
			break;
		}
	}

	private void init() {
		getDistributionInfo();
		mWallet = (ListView) findViewById(R.id.lv_wallet);
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
		getDistributionInfocallback c = new getDistributionInfocallback();

		XutilsHttp.getInstance().get(url, null, c, this);

	}

	class getDistributionInfocallback implements XCallBack {

		@Override
		public void onResponse(String result) {

			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");
					// 累计收益
					String CumulativeMoney = Data.getString("MDGDSM_CumulativeMoney");
					mTotalAmount.setText(CumulativeMoney);
					// 我的余额
					String mBalances = Data.getString("MDGDSM_ToMyMoney");
					mBalance.setText(mBalances);
					// 已提现金额
					String MDGDSM_ToMyInMoney = Data.getString("MDGDSM_ToMyInMoney");
					mTomyInmoney.setText(MDGDSM_ToMyInMoney);
					// 银行卡数量
					String MDGDSM_ToMyUserAccount = Data.getString("MDGDSM_ToMyUserAccount");
					mAccountNumber.setText(MDGDSM_ToMyUserAccount);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 获取我的钱包列表
	 */
	private void getMyWalletList(String time, String index, String size) {
		String s = getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("Time", time);
		map.put("PageIndex", index);
		map.put("PageSize", size);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/UserBillRecords/List_Log?UserId=" + s + "&Time=" + time
				+ "&PageIndex=" + index + "&PageSize=" + size + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getMyWalletListCallback(), this);

	}

	class getMyWalletListCallback implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");
					if (dataArray != null && dataArray.length() > 0) {
						List<MyWalletBean> mlist = new ArrayList<MyWalletBean>();

						for (int i = 0; i < dataArray.length(); i++) {
							JSONObject data = dataArray.getJSONObject(i);
							MyWalletBean mb = new MyWalletBean();

							mb.setAccount(
									data.getString("UserAccountTypeId") + "(" + data.getString("UserAccountNum") + ")");

							mb.setDatayear(data.getString("UserBill_Creatime_Y"));

							mb.setDatetime(data.getString("UserBill_Creatime_M"));

							mb.setPrice(data.getString("UserBill_Amount"));

							mb.setType(data.getString("UserBill_TypeName"));

							mb.setTypeId(data.getString("UserBill_TypeState"));
							mlist.add(mb);

						}
						AdapterMyWalletList mAdapter = new AdapterMyWalletList(mlist, ActivityFXWallet.this,
								R.layout.wj_item_my_wallet);

						mWallet.setAdapter(mAdapter);

					}
				} else {

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

}
