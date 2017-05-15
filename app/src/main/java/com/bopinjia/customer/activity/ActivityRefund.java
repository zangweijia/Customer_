package com.bopinjia.customer.activity;

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

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ActivityRefund extends BaseActivity {

	private String orderSn;
	private String otype;
	private String orderAmount;
	// private String name;
	// private String phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_refund);

		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 下一步
		findViewById(R.id.btn_next).setOnClickListener(this);

		orderSn = getIntent().getStringExtra("OrderSn");
		otype = getIntent().getStringExtra("otype");

		orderAmount = getIntent().getStringExtra("orderAmount");

		getOrderDetails();
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
		case R.id.btn_next:
			// 下一步
			returnOrder();
			break;
		default:
			break;
		}
	}

	private void returnOrder() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		String reason = ((TextView) findViewById(R.id.edt_reason)).getText().toString().trim();

		map.put("UserId", getLoginUserId());
		map.put("OrderSn", orderSn);
		map.put("ApplyReturnMoney", orderAmount);
		map.put("ReturnOrderReason", reason);
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
		maps.put("OrderSn", orderSn);
		maps.put("ApplyReturnMoney", orderAmount);
		maps.put("ReturnOrderReason", reason);

		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/ReturnOrderMoney/Add", maps,
				new returnOrderCallBack(), this);
	}

	class returnOrderCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					showToast("申请退款成功!如有问题请致电400-997-8757!");
					finish();
				} else {
					showToast("亲，申请退款失败~");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 获取订单详情
	 */
	public void getOrderDetails() {

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("OrderId", orderSn);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Otype", otype);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Order/Detail_New?OrderId=" + orderSn + "&Otype=" + otype + "&Sign="
				+ Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getOrderDetailsCallBack(), this);

	}

	class getOrderDetailsCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject jsonData = jo.getJSONObject("Data");
					if (jsonData != null && jsonData.length() > 0) {
						JSONObject order = jsonData.getJSONObject("order");
						((TextView) findViewById(R.id.name)).setText(order.getString("Consignee"));
						((TextView) findViewById(R.id.phone)).setText(order.getString("Mobile"));
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

}
