package com.bopinjia.customer.activity;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityScanResult extends BaseActivity {

	private String mSkuid;
	private String productskuid2, productskuid1;
	private LinearLayout mLL1, mLL2;
	private String mOtype;
	private String otype1, otype2;
	private String scanCode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_scan_result);

		scanCode = getIntent().getStringExtra("ScanCode");

		get(scanCode);

		mLL1 = (LinearLayout) findViewById(R.id.ll_1);
		mLL2 = (LinearLayout) findViewById(R.id.ll_2);

		mLL1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLL1.setBackgroundResource(R.drawable.kuang_);
				mLL2.setBackgroundResource(R.drawable.white_kuang);
				mSkuid = productskuid1;
				mOtype = otype1;
			}
		});

		mLL2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLL2.setBackgroundResource(R.drawable.kuang_);
				mLL1.setBackgroundResource(R.drawable.white_kuang);
				mSkuid = productskuid2;
				mOtype = otype2;
			}
		});

		findViewById(R.id.addCart).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addCart(mSkuid, mOtype);

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

	private void get(String code) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getBindingShop());
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

		String str = "http://newapi.bopinwang.com/" + "api/ProductNew/ProductListScanCodeBpw?UserId=" + getBindingShop()
				+ "&EanCode=" + code + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(str, null, new GetCallBack(), this);
	}

	class GetCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				if (jo.getString("Result").equals("1")) {
					JSONArray ja = jo.getJSONArray("Data");
					JSONObject jo1 = ja.getJSONObject(0);
					((TextView) findViewById(R.id.txt_title_1))
							.setText("              " + jo1.getString("ProductSKUName"));
					((TextView) findViewById(R.id.txt_price_1)).setText("¥" + jo1.getString("ScanPrice"));
					if (jo1.getString("CustomerInitiaQuantity").equals("0")) {
						((TextView) findViewById(R.id.buycount1)).setVisibility(View.INVISIBLE);
					} else {
						((TextView) findViewById(R.id.buycount1))
								.setText("起订量:" + jo1.getString("CustomerInitiaQuantity"));
					}
					ImageOptions imageOptions = new ImageOptions.Builder()
							.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
							// .setFailureDrawableId(R.drawable.ic_default_image)//
							// 加载失败后默认显示图片
							.build();
					x.image().bind((ImageView) findViewById(R.id.img_1), jo1.getString("ProductThumbnail"),
							imageOptions);
					productskuid1 = jo1.getString("ProductSKUId");
					otype1 = jo1.getString("IsDirectMail");
					if (otype1.equals("0")) {
						((TextView) findViewById(R.id.btn_isdirectmail1)).setText("现货区");
						((TextView) findViewById(R.id.btn_isdirectmail1))
								.setBackgroundResource(R.drawable.bg_order_state2);
					} else {
						((TextView) findViewById(R.id.btn_isdirectmail1)).setText("直邮区");
						((TextView) findViewById(R.id.btn_isdirectmail1))
								.setBackgroundResource(R.drawable.bg_order_state1);
					}
					mSkuid = productskuid1;
					mOtype = otype1;

					JSONObject jo2 = ja.getJSONObject(1);
					((TextView) findViewById(R.id.txt_title_2))
							.setText("              " + jo2.getString("ProductSKUName"));
					((TextView) findViewById(R.id.txt_price_2)).setText("¥" + jo2.getString("ScanPrice"));

					if (jo2.getString("CustomerInitiaQuantity").equals("0")) {
						((TextView) findViewById(R.id.buycount2)).setVisibility(View.INVISIBLE);
					} else {
						((TextView) findViewById(R.id.buycount2))
								.setText("起订量:" + jo2.getString("CustomerInitiaQuantity"));
					}
					ImageOptions imageOption = new ImageOptions.Builder()
							.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
							// .setFailureDrawableId(R.drawable.ic_default_image)//
							// 加载失败后默认显示图片
							.build();
					x.image().bind((ImageView) findViewById(R.id.img_2), jo2.getString("ProductThumbnail"),
							imageOption);
					productskuid2 = jo2.getString("ProductSKUId");
					otype2 = jo2.getString("IsDirectMail");
					if (otype2.equals("0")) {
						((TextView) findViewById(R.id.btn_isdirectmail2)).setText("现货区");
						((TextView) findViewById(R.id.btn_isdirectmail2))
								.setBackgroundResource(R.drawable.bg_order_state2);
					} else {
						((TextView) findViewById(R.id.btn_isdirectmail2)).setText("直邮区");
						((TextView) findViewById(R.id.btn_isdirectmail2))
								.setBackgroundResource(R.drawable.bg_order_state1);
					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void addCart(String skuid, String otype) {

		String muserId = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
		String userid;
		if (isLogged()) {
			userid = getLoginUserId();
		} else {
			userid = "0";
		}
		String Ts = MD5.getTimeStamp();
		String str = "Key=" + Constants.WEBAPI_KEY + "&MUserId=" + muserId + "&Paystate=0&ProductType=" + otype
				+ "&Quantity=" + "1" + "&SkuId=" + skuid + "&Ts=" + Ts + "&UserId=" + userid + "&UUID=" + getIMEI()
				+ "&WarehouseId=0";

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("ProductType", otype);
		maps.put("SkuId", skuid);
		maps.put("MUserId", muserId);
		maps.put("Paystate", "0");
		maps.put("WarehouseId", "0");
		maps.put("UUID", getIMEI());
		maps.put("Quantity", "1");
		maps.put("UserId", userid);

		maps.put("Sign", MD5.Md5(str));
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/CSC/BpwAdd", maps, new addCartCallBack(), this);

	}

	class addCartCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// 跳转到加入购物车成功界面
					forward(ActivityAddCartSuccess.class);
					finish();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
