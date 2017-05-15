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
import com.bopinjia.customer.activity.ActivityHome.useCodeCallBack;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.mainpage.CategoryFragmentAdapter;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.SecurityUtil;
import com.bopinjia.customer.util.StringUtils;
import com.bopinjia.customer.view.NoScrollViewPager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

@SuppressLint("ResourceAsColor")
public class ActivityCategory extends BaseActivity {
	public static final int TAB_CATEGORY = 0;
	public static final int TAB_BRAND = 1;
	/**
	 * 分类按钮
	 */
	@ViewInject(R.id.btn_category)
	private TextView mCategory;
	/**
	 * 品牌按钮
	 */
	@ViewInject(R.id.btn_brand)
	private TextView mBrand;

	@ViewInject(R.id.viewpager)
	private NoScrollViewPager viewPager;
	/**
	 * 存储是从哪个界面进来的 0 现货 、 1 直邮 、 3 自营
	 */
	public static int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_category);
		x.view().inject(this);
		type = getIntent().getIntExtra("type", 1);

		findViewById(R.id.btn_return).setOnClickListener(this);
		findViewById(R.id._search).setOnClickListener(this);
		findViewById(R.id._scan).setOnClickListener(this);

		mBrand.setOnClickListener(this);
		mCategory.setOnClickListener(this);
		viewPager.setOffscreenPageLimit(2);
		CategoryFragmentAdapter cfa = new CategoryFragmentAdapter(getSupportFragmentManager());
		viewPager.setAdapter(cfa);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_category:
			// 分类
			viewPager.setCurrentItem(TAB_CATEGORY, false);
			findViewById(R.id.btn_category).setBackgroundResource(R.color.main_color);
			findViewById(R.id.btn_brand).setBackgroundResource(R.color.bg_ffffff);

			((TextView) findViewById(R.id.btn_category)).setTextColor(this.getResources().getColor(R.color.bg_ffffff));
			((TextView) findViewById(R.id.btn_brand)).setTextColor(this.getResources().getColor(R.color.bg_0000));

			break;
		case R.id.btn_brand:
			// 品牌
			findViewById(R.id.btn_brand).setBackgroundResource(R.color.main_color);
			findViewById(R.id.btn_category).setBackgroundResource(R.color.bg_ffffff);

			((TextView) findViewById(R.id.btn_brand)).setTextColor(this.getResources().getColor(R.color.bg_ffffff));
			((TextView) findViewById(R.id.btn_category)).setTextColor(this.getResources().getColor(R.color.bg_0000));
			viewPager.setCurrentItem(TAB_BRAND, false);
			break;
		case R.id.btn_return:
			// 返回
			backward();
			break;
		case R.id._search:
			// 跳转搜索
			Intent toSearch = new Intent();
			toSearch.setClass(ActivityCategory.this, ActivitySearch.class);
			toSearch.putExtra("type", type);
			startActivity(toSearch);
			break;
		case R.id._scan:
			// 扫码
			Intent intent = new Intent(ActivityCategory.this, CaptureActivity.class);
			startActivityForResult(intent, 1);
			break;
		default:
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			String scanResult = SecurityUtil.decode(data.getStringExtra(Constants.INTENT_EXTRA_SCAN_RESULT));

			// 2016/04/15 需求变更 修正
			// 店铺的情况
			if (scanResult.length() < 8) {
				useCode(scanResult);
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

	private void useCode(String code) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("GDSUser_Num", code);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Store/GetGDSUser_Num?GDSUser_Num=" + code + "&Sign=" + Sign
				+ "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new useCodeCallBack(), this);
	}

	class useCodeCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject data = jo.getJSONObject("Data");
					String mGDSUserId = data.getString("GDSUserId");
					putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_SHOP, data.getString("MDUserId"));

					if (mGDSUserId == null) {
						putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_GDSUSERID, "");
					} else {
						putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_GDSUSERID, mGDSUserId);
					}

					ActivityHome.instance.finish();
					forward(ActivityHome.class);

				} else {
					showToast(getString(R.string.msg_err_scan_result_invalid));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 根据条码搜索商品判断是否有直邮现货
	 * 
	 * @param userid
	 * @param code
	 */
	private void search(String shopid, final String code) {
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

		XutilsHttp.getInstance().get(url, null, new searchCallack(), 0, code, this);
	}

	/**
	 * 根据条码搜索商品判断是否有直邮现货 回调
	 */
	class searchCallack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONArray ja = jo.getJSONArray("Data");
					int i = ja.length();
					if (i >= 1) {
						Intent ii = new Intent();
						ii.putExtra("ScanCode", str);
						ii.putExtra("isScan", "0");
						forward(ActivityCart.class, ii);
					} else if (i == 0) {
						showToast(getString(R.string.msg_err_no_product));
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
