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
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.SecurityUtil;
import com.bopinjia.customer.util.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 扫码加入购物车成功弹出界面
 * 
 * @author Administrator
 *
 */
public class ActivityAddCartSuccess extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_activity_add_cart_success);

		findViewById(R.id.btn_jixu).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 扫码
				Intent intent = new Intent(ActivityAddCartSuccess.this, CaptureActivity.class);
				startActivityForResult(intent, 1);
			}
		});

		findViewById(R.id.btn_go_cart).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				forward(ActivityCart.class);
				finish();
			}
		});

		findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			String scanResult = SecurityUtil.decode(data.getStringExtra(Constants.INTENT_EXTRA_SCAN_RESULT));

			// 2016/04/15 需求变更 修正
			// 店铺的情况
			if (scanResult.length() < 8) {
				isStore(scanResult);
			} else {
				// 商品的情况
				String shopCd = getBindingShop();

				// 还没有扫描店铺的时候
				if (StringUtils.isNull(shopCd)) {
					showToast(getString(R.string.msg_err_no_binding_shop));
				} else {
					search(shopCd, scanResult);
				}
			}

		}
	}

	private  String code ;
	
	/**
	 * 根据条码搜索商品判断是否有直邮现货
	 * 
	 * @param userid
	 * @param code
	 */
	private void search(String shopid, final String code) {
		this.code = code;
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", shopid);
		map.put("EanCode", code);
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

		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListScanCodeBpw?UserId=" + shopid + "&EanCode="
				+ code + "&Sign=" + Sign + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new searchCallBack(),this);
	}

	class searchCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONArray ja = jo.getJSONArray("Data");
					int i = ja.length();
					if (i > 1) {
						// 当i大于1 说明有直邮有现货 跳到选择界面
						Intent ii = new Intent();
						ii.putExtra("ScanCode", code);
						forward(ActivityScanResult.class, ii);
						finish();
					} else if (i == 1) {
						// 直接购买
						JSONObject jo1 = ja.getJSONObject(0);
						String skuid = jo1.getString("ProductSKUId");
						String otype = jo1.getString("IsDirectMail");
						AddCart(skuid, otype);
					} else if (i == 0) {

						showToast(getString(R.string.msg_err_no_product));
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	private void AddCart(String SkuId, String ProductType) {

		String userid;
		if (isLogged()) {
			userid = getLoginUserId();
		} else {
			userid = "0";
		}
		String shopid = getBindingShop();

		String Ts = MD5.getTimeStamp();
		String str = "Key=" + Constants.WEBAPI_KEY + "&MUserId=" + shopid + "&Paystate=0" + "&ProductType="
				+ ProductType + "&Quantity=" + "1" + "&SkuId=" + SkuId + "&Ts=" + Ts + "&UserId=" + userid + "&UUID="
				+ getIMEI1() + "&WarehouseId=0";

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwAdd");
		params.addBodyParameter("ProductType", ProductType);
		params.addBodyParameter("SkuId", SkuId);
		params.addBodyParameter("MUserId", shopid);
		params.addBodyParameter("Paystate", "0");
		params.addBodyParameter("WarehouseId", "0");
		params.addBodyParameter("UUID", getIMEI1());
		params.addBodyParameter("Quantity", "1");
		params.addBodyParameter("UserId", userid);

		params.addBodyParameter("Sign", MD5.Md5(str));
		params.addBodyParameter("Ts", Ts);
		params.setAsJsonContent(true);

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {

					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				showErr();
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});

	}

	private void isStore(final String userid) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", userid);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Store/Info?UserId=" + userid + "&Sign=" + Sign + "&Ts=" + Ts;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_SHOP, userid);
						ActivityHome.instance.finish();
						forward(ActivityHome.class);
					} else if (jsonresult.equals("2")) {
						showToast(getString(R.string.msg_err_scan_result_invalid));
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
