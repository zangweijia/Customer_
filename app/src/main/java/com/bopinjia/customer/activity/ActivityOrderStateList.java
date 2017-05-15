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
import com.bopinjia.customer.adapter.CartTuiJianProductAdapter;
import com.bopinjia.customer.adapter.ProductListModel;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.NoUnderlineSpan;
import com.bopinjia.customer.view.NoScrollGridView;
import com.bopinjia.customer.view.NoScrollListview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ActivityOrderStateList extends BaseActivity {

	private NoScrollListview listView;
	private TimelineAdapter timelineAdapter;

	private String mOrderId, Otype;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_order_state_list);
		Otype = getIntent().getStringExtra("Otype");
		mOrderId = getIntent().getStringExtra("OrderId");
		findViewById(R.id.btn_return).setOnClickListener(this);

		mGridNew = (NoScrollGridView) findViewById(R.id.grid_tuijian);

		listView = (NoScrollListview) this.findViewById(R.id.listview_logistic_det);
		listView.setDividerHeight(0);

		listView.setOnItemClickListener(null);

	}

	protected void onStart() {
		super.onStart();
		// 订单状态查询
		GetOrderState(mOrderId);

		// 猜你喜欢
		getTuiJian();
	}

	public class TimelineAdapter extends BaseAdapter {

		private Context context;
		private List<Map<String, Object>> list;
		private LayoutInflater inflater;

		public TimelineAdapter(Context context, List<Map<String, Object>> list) {
			super();
			this.context = context;
			this.list = list;
		}

		@Override
		public int getCount() {

			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;

			if (convertView == null) {
				inflater = LayoutInflater.from(parent.getContext());
				convertView = inflater.inflate(R.layout.item_logistic_detail, null);
				viewHolder = new ViewHolder();

				viewHolder.msg = (TextView) convertView.findViewById(R.id.tv_msg);
				viewHolder.updateTime = (TextView) convertView.findViewById(R.id.tv_update_time);
				viewHolder.view1 = convertView.findViewById(R.id.view_1);
				viewHolder.view2 = convertView.findViewById(R.id.view_2);
				viewHolder.image1 = convertView.findViewById(R.id.image1);
				viewHolder.image = convertView.findViewById(R.id.image);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final String msgStr = list.get(position).get("msg").toString();
			String timeStr = list.get(position).get("updateTime").toString();
			String isfirst = list.get(position).get("isFirst").toString();
			String isLast = list.get(position).get("isLast").toString();

			if (isfirst.equals("1")) {
				viewHolder.image1.setVisibility(View.GONE);
				viewHolder.view2.setVisibility(View.VISIBLE);
			}
			if (isLast.equals("1")) {
				viewHolder.image1.setVisibility(View.VISIBLE);
				viewHolder.view2.setVisibility(View.GONE);
			}
			viewHolder.msg.setText(msgStr);
			viewHolder.updateTime.setText(timeStr);

			return convertView;
		}

	}

	static class ViewHolder {
		public TextView updateTime;
		public TextView msg;
		public TextView stateName;
		public TextView orderId;
		public View view1;
		public View view2;
		public View image1;
		public View image;
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
			backward();
			break;
		}
	}

	/**
	 * 获取订单状态列表
	 * 
	 * @param orderId
	 *            订单id
	 * @param Otype
	 *            订单类型 直邮现货自营
	 */
	private void GetOrderState(String orderId) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("OrderId", orderId);
		map.put("Otype", Otype);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Order/StateList?OrderId=" + orderId + "&Otype=" + Otype + "&Sign="
				+ Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetOrderStateCallBack(), this);
	}

	class GetOrderStateCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");

					TextView shopPhone = (TextView) findViewById(R.id.shop_phone);
					shopPhone.setText(Data.getString("ShopMobile"));

					TextView Phone = (TextView) findViewById(R.id.tousu_phone);
					Phone.setText(Data.getString("ComplaintCall"));

					NoUnderlineSpan mNoUnderlineSpan = new NoUnderlineSpan();
					if (shopPhone.getText() instanceof Spannable) {
						Spannable s = (Spannable) shopPhone.getText();
						s.setSpan(mNoUnderlineSpan, 0, s.length(), Spanned.SPAN_MARK_MARK);
					}
					if (Phone.getText() instanceof Spannable) {
						Spannable s = (Spannable) Phone.getText();
						s.setSpan(mNoUnderlineSpan, 0, s.length(), Spanned.SPAN_MARK_MARK);
					}

					JSONArray dataArray = Data.getJSONArray("ordersObjList");
					if (dataArray != null && dataArray.length() > 0) {
						List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
						int len = dataArray.length();
						for (int i = len - 1; i >= 0; i--) {
							Map<String, Object> map = new HashMap<String, Object>();
							JSONObject data = dataArray.getJSONObject(i);
							map.put("msg", data.get("Msg"));
							map.put("orderId", data.get("OrderId"));
							map.put("stateName", data.get("StateName"));
							map.put("updateTime", data.get("UpdateTime"));
							map.put("isFirst", data.get("IsFirst"));
							map.put("isLast", data.get("IsLast"));
							list.add(map);
						}
						timelineAdapter = new TimelineAdapter(ActivityOrderStateList.this, list);
						listView.setAdapter(timelineAdapter);
					} else {
						listView.setVisibility(View.GONE);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private NoScrollGridView mGridNew;
	private List<ProductListModel> list;

	private void getTuiJian() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("MUserID", getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP));
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("MUserID", getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP));
		maps.put("Sign", NetUtils.getSign(map));
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/CSC/BpwCarRecommend", maps, new CallBack(), this);

	}

	class CallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// getCartContent();
					ParseTuiJian(result);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void ParseTuiJian(String result) {

		try {
			JSONObject jo = new JSONObject(result);
			JSONArray jDList = jo.getJSONArray("Data");

			if (jDList.length() > 0) {

				if (jDList != null) {
					list = new ArrayList<ProductListModel>();
					for (int i = 0; i < jDList.length(); i++) {
						JSONObject data = jDList.getJSONObject(i);
						ProductListModel m = new ProductListModel();
						m.setMarket_price(data.getString("MarketPrice"));
						m.setName(data.getString("SkuName"));
						m.setThumbnails(data.getString("ProductThumbnail"));
						m.setSale_price(data.getString("SellPrice"));
						m.setIsShip(data.getString("IsFreeShipping"));
						m.setSkuid(data.getString("SkuId"));
						//m.setRealStock(data.getString("RealStock"));
						list.add(m);
					}

					CartTuiJianProductAdapter mCartTJ = new CartTuiJianProductAdapter(ActivityOrderStateList.this,
							list,R.layout.wj_item_grid_product);
					mGridNew.setAdapter(mCartTJ);
					mGridNew.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							Intent ii = new Intent();
							ii.putExtra("IsFreeShipping", list.get(position).getIsShip());
							ii.putExtra("ProductSKUId", list.get(position).getSkuid());
							ii.setClass(ActivityOrderStateList.this, ActivityProductDetailsNew.class);
							startActivity(ii);

						}
					});
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}