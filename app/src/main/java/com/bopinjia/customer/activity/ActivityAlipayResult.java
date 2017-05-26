package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.alipay.AlipayResult;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.MD5;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityAlipayResult extends BaseActivity {

	private String merId;

	private String state;

	private String orderid;

	/**
	 * 判断从哪进入回调 1、购物 2、分销会员
	 */
	private String type;

	private TextView txtMessage;

	private TextView txtMessageInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);

		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		findViewById(R.id.btn_return).setVisibility(View.INVISIBLE);

		String strResult = getIntent().getStringExtra("alipayResult");
		orderid = getIntent().getStringExtra("orderId");

		AlipayResult alipayResult = new AlipayResult(strResult);
		String resultStatus = alipayResult.getResultStatus();

		txtMessage = (TextView) findViewById(R.id.txt_msg);
		txtMessageInfo = (TextView) findViewById(R.id.txt_info);

		merId = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_TEMP_SHOP);

		type = getIntent().getStringExtra("type");

		if (TextUtils.equals(resultStatus, "9000")) {
			// 支付成功
			txtMessage.setText(R.string.txt_pay_success);
			txtMessageInfo.setText(R.string.txt_pay_success_message);
			if (type.equals("1")) {
				state = "2";
				RedpacketReceive();
			} else if (type.equals("2")) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						forward(ActivityFXKTSuccess.class);
						ActivityFXDisPay.instance.finish();
						finish();
					}
				}, 3000);
			} else if (type.equals("3")) {
				// 分销会员续费
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						forward(ActivityFXGL.class);
						ActivityFXDisPay.instance.finish();
						finish();
					}
				}, 2000);
			} else if (type.equals("4")) {
				// 分销会员升级
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent i = new Intent();
						i.putExtra("shengji", "1");
						forward(ActivityFXGL.class);
						ActivityFXDisPay.instance.finish();
						finish();
					}
				}, 2000);

			}
		} else if (TextUtils.equals(resultStatus, "8000")) {
			// 需要等待支付结果
			txtMessage.setText(R.string.txt_pay_warning);
			txtMessageInfo.setText(R.string.txt_pay_success_message);
		} else {
			// 支付失败
			state = "1";
			ImageView imageView = (ImageView) findViewById(R.id.icon_image);
			imageView.setImageResource(R.drawable.ic_err);

			if (type.equals("1")) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						// 购物
						txtMessage.setText(R.string.txt_pay_error);
						txtMessageInfo.setText(R.string.txt_pay_success_message);
						Intent i = new Intent();
						i.putExtra("status", state);
						forward(ActivityOrderList.class, i);
						finish();
					}
				}, 3000);
			} else {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {

						// 会员支付失败
						txtMessage.setText("支付失败");
						txtMessageInfo.setText(R.string.txt_pay_success_message);
						finish();

					}
				}, 3000);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		case R.id.btn_return:

			// Intent i = new Intent();
			// i.putExtra("status", "1");
			// forward(ActivityOrderList.class, i);
			// finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 判断门店是否存在红包活动
	 */
	private void RedpacketReceive() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("MdUserId", getBindingShop());

		map.put("RPTID", "2");
		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			stringBuffer.append(key).append("=").append(map.get(key)).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);
		String Sign = MD5.Md5(stringBuffer.toString());

		String url = Constants.WEBAPI_ADDRESS + "api/RedPacketReceive/Exists?MdUserId=" + getBindingShop() + "&RPTID=2"
				+ "&Sign=" + Sign + "&Ts=" + Ts;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						// 存在活动跳到摇一摇界面
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent i = new Intent();
								i.putExtra("orderid", orderid);
								forward(ActivityTurntable.class, i);
								finish();
							}
						}, 3000);
					} else {
						// 不存在活动等待3秒跳到订单列表
						new Handler().postDelayed(new Runnable() {
							@Override
							public void run() {
								Intent i = new Intent();
								i.putExtra("status", state);
								forward(ActivityOrderList.class, i);
								finish();
							}
						}, 3000);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

}
