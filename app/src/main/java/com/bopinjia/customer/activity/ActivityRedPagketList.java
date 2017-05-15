package com.bopinjia.customer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterDropRedList;
import com.bopinjia.customer.bean.DropRedBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityRedPagketList extends BaseActivity {

	private LinearLayout ll_unuse;
	private LinearLayout ll_use;
	private LinearLayout ll_expired;
	private TextView tv_unuse;
	private TextView tv_use;
	private TextView tv_expired;
	private View v_unuse;
	private View v_use;
	private View v_expired;
	private ListView mList;
	private AdapterDropRedList mAdapter;
	private List<DropRedBean> mlist;

	/**
	 * 刷新控件
	 */
	private XRefreshView outView;
	public static long lastRefreshTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_red_pagket);

		init();
		getContent("0", 0);
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		ll_unuse = (LinearLayout) findViewById(R.id.ll_unuse);
		ll_use = (LinearLayout) findViewById(R.id.ll_use);
		ll_expired = (LinearLayout) findViewById(R.id.ll_expired);

		tv_unuse = (TextView) findViewById(R.id.tv_unuse);
		tv_use = (TextView) findViewById(R.id.tv_use);
		tv_expired = (TextView) findViewById(R.id.tv_expired);

		v_unuse = (View) findViewById(R.id.v_unuse);
		v_use = (View) findViewById(R.id.v_use);
		v_expired = (View) findViewById(R.id.v_expired);

		mList = (ListView) findViewById(R.id.lv_drop_red);

		ll_unuse.setOnClickListener(this);
		ll_use.setOnClickListener(this);
		ll_expired.setOnClickListener(this);

		findViewById(R.id.btn_return).setOnClickListener(this);

		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
		outView.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
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
						showToast("没有更多了~");
						outView.stopLoadMore();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			// 返回
			backward();
			break;
		case R.id.ll_unuse:
			settab(1);
			break;

		case R.id.ll_use:
			settab(2);
			break;

		case R.id.ll_expired:
			settab(3);
			break;
		default:
			break;
		}
	}

	/**
	 * tab 标签切换
	 * 
	 * @param index
	 */
	private void settab(int index) {
		switch (index) {
		case 1:
			tv_unuse.setTextColor(this.getResources().getColor(R.color.main_color));
			v_unuse.setVisibility(View.VISIBLE);

			tv_use.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_use.setVisibility(View.INVISIBLE);

			tv_expired.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_expired.setVisibility(View.INVISIBLE);
			getContent("0", 0);
			break;
		case 2:
			tv_unuse.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_unuse.setVisibility(View.INVISIBLE);

			tv_use.setTextColor(this.getResources().getColor(R.color.main_color));
			v_use.setVisibility(View.VISIBLE);

			tv_expired.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_expired.setVisibility(View.INVISIBLE);
			getContent("1", 1);
			break;
		case 3:
			tv_unuse.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_unuse.setVisibility(View.INVISIBLE);

			tv_use.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_use.setVisibility(View.INVISIBLE);

			tv_expired.setTextColor(this.getResources().getColor(R.color.main_color));
			v_expired.setVisibility(View.VISIBLE);
			getContent("2", 1);
			break;

		default:
			break;
		}
	}

	private String stateid;

	private void getContent(String stateid, final int id) {
		this.stateid = stateid;
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		map.put("UserId", getLoginUserId());
		map.put("MdUserId", getBindingShop());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("PageIndex", "1");
		map.put("pageSize", "100");
		map.put("StateId", stateid);
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

		String url = Constants.WEBAPI_ADDRESS + "api/RedPacketReceive/ListData?UserId=" + getLoginUserId()
				+ "&MdUserId=" + getBindingShop() + "&StateId=" + stateid + "&PageIndex=" + "1" + "&pageSize=" + "100"
				+ "&Sign=" + Sign + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new CallBack(), id, null, this);

	}

	class CallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parse(result, id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void parse(String result, int id) {

		try {
			JSONObject jo = new JSONObject(result);
			JSONObject jdata = jo.getJSONObject("Data");
			JSONObject Paging = jdata.getJSONObject("Paging");
			JSONArray dataArray = jdata.getJSONArray("Records");

			if (dataArray != null && dataArray.length() > 0) {

				List<DropRedBean> list = new ArrayList<DropRedBean>();
				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					DropRedBean drb = new DropRedBean();
					drb.setAmount(data.getString("RPR_Price"));
					drb.setRed_detail(data.getString("RPT_type"));
					drb.setRed_isuse(stateid);
					drb.setRed_time(data.getString("RPT_time"));
					drb.setRed_type(data.getString("RPT_Name"));
					list.add(drb);
				}
				mList.setVisibility(View.VISIBLE);
				if (id != 1) {
					mlist = list;
					mAdapter = new AdapterDropRedList(mlist, this, R.layout.wj_item_drop_red);
					mList.setAdapter(mAdapter);
				} else {
					if (mlist != null) {
						mlist.clear();
						mlist.addAll(list);
						mAdapter.notifyDataSetChanged();
					} else {
						mlist = list;
						mAdapter = new AdapterDropRedList(mlist, this, R.layout.wj_item_drop_red);
						mList.setAdapter(mAdapter);
					}
				}
			} else {
				mList.setVisibility(View.INVISIBLE);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
