package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterUseRedList;
import com.bopinjia.customer.bean.DropRedBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityUseRedList extends BaseActivity {

	private LinearLayout ll_unuse;
	private LinearLayout ll_use;
	private TextView tv_unuse;
	private TextView tv_use;
	private View v_unuse;
	private View v_use;
	private ListView mList;

	private List<DropRedBean> mlist;
	private AdapterUseRedList mAdapter;

	private int tabid = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_use_red_list);

		init();
		// 获取初始化列表 “0”为 可使用的红包 第二个0为初次加载id标识
		getContent("0", 0);
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		ll_unuse = (LinearLayout) findViewById(R.id.ll_unuse);
		ll_use = (LinearLayout) findViewById(R.id.ll_use);

		tv_unuse = (TextView) findViewById(R.id.tv_unuse);
		tv_use = (TextView) findViewById(R.id.tv_use);

		v_unuse = (View) findViewById(R.id.v_unuse);
		v_use = (View) findViewById(R.id.v_use);

		mList = (ListView) findViewById(R.id.lv_use_red);

		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (tabid == 0) {
					String mRedid = mlist.get(arg2).getRed_id();
					Intent data = new Intent();
					data.putExtra("redId", mRedid);
					setResult(4, data);
					finish();
				} else if (tabid == 1) {

				}
			}
		});

		ll_unuse.setOnClickListener(this);
		ll_use.setOnClickListener(this);

		findViewById(R.id.btn_return).setOnClickListener(this);
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
			tabid = 1;
			tv_unuse.setTextColor(this.getResources().getColor(R.color.main_color));
			v_unuse.setVisibility(View.VISIBLE);

			tv_use.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_use.setVisibility(View.INVISIBLE);
			getContent("1", 1);

			break;
		case 2:
			tabid = 0;
			tv_unuse.setTextColor(this.getResources().getColor(R.color.findpass_title));
			v_unuse.setVisibility(View.INVISIBLE);

			tv_use.setTextColor(this.getResources().getColor(R.color.main_color));
			v_use.setVisibility(View.VISIBLE);

			getContent("0", 1);

			break;

		default:
			break;
		}
	}

	private String mStateId;

	/**
	 * 获取红包列表
	 * 
	 * @param stateid
	 * @param id
	 *            id 不为1 说明是初始化加载
	 */
	private void getContent(String stateid, final int id) {
		mStateId = stateid;

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		map.put("UserId", getLoginUserId());
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

		String url = Constants.WEBAPI_ADDRESS + "api/RedPacketReceive/ListDataOrder?UserId=" + getLoginUserId() +

				"&StateId=" + stateid + "&PageIndex=" + "1" + "&pageSize=" + "100" +

				"&Sign=" + Sign + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new UseCallBack(), id, null, this);

	}

	class UseCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {

			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject jdata = jo.getJSONObject("Data");
					JSONObject Paging = jdata.getJSONObject("Paging");
					JSONArray dataArray = jdata.getJSONArray("Records");

					if (mStateId.equals("0")) {
						tv_use.setText("可用红包(" + Paging.getString("Total") + ")");
					} else if (mStateId.equals("1")) {
						tv_unuse.setText("不可使用红包(" + Paging.getString("Total") + ")");
					}

					if (dataArray != null && dataArray.length() > 0) {

						List<DropRedBean> list = new ArrayList<DropRedBean>();
						for (int i = 0; i < dataArray.length(); i++) {
							JSONObject data = dataArray.getJSONObject(i);
							DropRedBean drb = new DropRedBean();
							drb.setAmount(data.getString("RPR_Price"));
							drb.setRed_detail(data.getString("RPT_type"));
							drb.setRed_isuse(mStateId);
							drb.setRed_time(data.getString("RPT_time"));
							drb.setRed_type(data.getString("RPT_Name"));
							drb.setRed_id(data.getString("RPR_ID"));
							list.add(drb);
						}
						mList.setVisibility(View.VISIBLE);
						mlist = list;
						mAdapter = new AdapterUseRedList(mlist, ActivityUseRedList.this,
								R.layout.wj_item_use_red_list_item);
						mList.setAdapter(mAdapter);
					} else {
						mList.setVisibility(View.INVISIBLE);
						showToast("暂时没有红包");
					}

				} else if (jsonresult.equals("2")) {
					showToast("暂时没有红包");
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

}
