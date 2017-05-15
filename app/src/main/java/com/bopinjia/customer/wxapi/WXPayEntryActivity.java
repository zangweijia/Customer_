package com.bopinjia.customer.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityFXDisPay;
import com.bopinjia.customer.activity.ActivityFXGL;
import com.bopinjia.customer.activity.ActivityFXKTSuccess;
import com.bopinjia.customer.activity.ActivityOrderList;
import com.bopinjia.customer.activity.ActivityTurntable;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.MD5;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

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

public class WXPayEntryActivity extends BaseActivity implements IWXAPIEventHandler {

	private IWXAPI api;
	private String merId;

	private String state;
	private String orderid;
	private String type;
	private TextView txtMessage;
	private TextView txtMessageInfo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information);
		merId = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_TEMP_SHOP);

		type = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_PAY_TYPE);

		api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID);
		api.handleIntent(getIntent(), this);

		findViewById(R.id.btn_return).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});
		findViewById(R.id.btn_return).setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
	}

	@Override
	public void onResp(BaseResp resp) {
		if (resp.errCode == 0) {
			// 支付成功
			state = "2";

			TextView txtMessage = (TextView) findViewById(R.id.txt_msg);
			txtMessage.setText(R.string.txt_pay_success);
			TextView txtMessageInfo = (TextView) findViewById(R.id.txt_info);
			txtMessageInfo.setText(R.string.txt_pay_success_message);
			/**
			 * type 判断从哪进入回调 1 普通购物 2 开通分销会员 3分销会员续费 4分销会员升级
			 */
			if (type.equals("1")) {
				// 购买商品
				orderid = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_ORDER_ID);
				RedpacketReceive();
			} else if (type.equals("2")) {
				// 开通分销商
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
						forward(ActivityFXGL.class);
						ActivityFXDisPay.instance.finish();
						finish();
					}
				}, 2000);

			}

		} else {
			// 支付失败
			state = "1";

			ImageView imageView = (ImageView) findViewById(R.id.icon_image);
			imageView.setImageResource(R.drawable.ic_err);

			txtMessage = (TextView) findViewById(R.id.txt_msg);
			txtMessageInfo = (TextView) findViewById(R.id.txt_info);

			if (type.equals("1")) {
				txtMessage.setText(R.string.txt_pay_error);
				txtMessageInfo.setText(R.string.txt_pay_success_message);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {

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

				}, 2000);
			}

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