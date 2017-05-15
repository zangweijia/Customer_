package com.bopinjia.customer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterReflectList;
import com.bopinjia.customer.bean.ReflectBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityFXReflectList extends BaseActivity {

	private TextView mTiTleBack;
	private TextView mTiTleName;

	@ViewInject(R.id.tv_time)
	private TextView mTime;
	/**
	 * 已提现
	 */
	@ViewInject(R.id.tv_already_present)
	private TextView mAlreadyPresent;

	@ViewInject(R.id.tv_all_price)
	private TextView mAllPrice;

	@ViewInject(R.id.list_reflect)
	private ListView mList;

	/**
	 * 刷新控件
	 */
	private XRefreshView outView;
	public static long lastRefreshTime;

	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;
	private List<ReflectBean> datalist;

	private int id = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_reflect_list);
		x.view().inject(this);
		setTitle();
		init();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);

		mTiTleName.setText("提现记录");
		mTiTleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	private void init() {
		getDistributionInfo();
		getBillRecordsList(PageIndex, 0);

		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
//		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		outView.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						getBillRecordsList(PageIndex, 0);
						outView.stopRefresh();
						lastRefreshTime = outView.getLastRefreshTime();
					}
				}, 2000);

			}

			@Override
			public void onLoadMore(boolean isSilence) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;

							getBillRecordsList(PageIndex, 1);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mList.postDelayed(new Runnable() {
								@Override
								public void run() {
									showToast("没有更多了~");
									outView.stopLoadMore();
								}
							}, 500);
						}

					}
				}, 1500);
			}



			@Override
			public void onRelease(float direction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onHeaderMove(double offset, int offsetY) {

			}
		});
		outView.restoreLastRefreshTime(lastRefreshTime);
	}

	private void getBillRecordsList(int index, int id) {
		this.id = id;
		String s = getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("PageIndex", String.valueOf(index));
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/UserBillRecords/List?UserId=" + s + "&PageIndex="
				+ String.valueOf(index) + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new BillRecordsListCallBack(),this);
	}

	class BillRecordsListCallBack implements XCallBack {

		private AdapterReflectList mAdapter;

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				JSONObject jod = jo.getJSONObject("Data");
				JSONArray dataArray = jod.getJSONArray("Records");

				JSONObject Paging = jod.getJSONObject("Paging");
				mAllPages = Paging.getString("Pages");

				if (dataArray != null && dataArray.length() > 0) {
					List<ReflectBean> list = new ArrayList<ReflectBean>();
					for (int i = 0; i < dataArray.length(); i++) {
						JSONObject data = dataArray.getJSONObject(i);
						ReflectBean Rfb = new ReflectBean();

						Rfb.setPrice(data.getString("MUbMoney"));
						Rfb.setState(data.getString("MUbState"));
						Rfb.setTime(data.getString("MUbTime"));
						list.add(Rfb);

					}
					if (id == 0) {
						datalist = list;
						mAdapter = new AdapterReflectList(list, ActivityFXReflectList.this, R.layout.wj_item_reflect);
						mList.setAdapter(mAdapter);
					} else if (id == 1) {
						if (list != null && !list.isEmpty()) {
							datalist.addAll(list);
							mAdapter.notifyDataSetChanged();
						}
					}
				} else {
				}
			} catch (Exception e) {

			}
		}

	}

	/**
	 * 获取分销商信息
	 */
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
		XutilsHttp.getInstance().get(url, null, c,this);

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
					mAllPrice.setText(CumulativeMoney);

					// 已提现金额
					String MDGDSM_ToMyInMoney = Data.getString("MDGDSM_ToMyInMoney");
					mAlreadyPresent.setText(MDGDSM_ToMyInMoney);

					SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月");
					Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
					String str = formatter.format(curDate);
					mTime.setText(str);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
