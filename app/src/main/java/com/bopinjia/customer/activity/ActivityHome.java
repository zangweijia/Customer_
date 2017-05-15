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
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.mainpage.DirectMailPage;
import com.bopinjia.customer.mainpage.GoodsInStock;
import com.bopinjia.customer.mainpage.MainCartFragment;
import com.bopinjia.customer.mainpage.MainMyFragment;
import com.bopinjia.customer.mainpage.MainXHFragment;
import com.bopinjia.customer.mainpage.MainZYFragment;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.SecurityUtil;
import com.bopinjia.customer.util.StringUtils;
import com.bopinjia.customer.view.MyBadgeView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityHome extends BaseActivity {

	public static ActivityHome instance = null;

	private long mExitTime;

	private Fragment mContent;
	private TextView mCart;
	private MyBadgeView badge1;

	private FragmentManager fragmentManager;
	private FragmentTransaction transaction;
	private RadioGroup radioGroup;
	private Map<Integer, Fragment> fmap = new HashMap<Integer, Fragment>();
	private static int currentRadio = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_customer__home);

		// -----防止fragment崩溃

		if (savedInstanceState != null) {
			fragmentManager = getSupportFragmentManager();// 重新创建Manager，防止此对象为空
			fragmentManager.popBackStackImmediate(null, 1);// 弹出所有fragment
		}
		// -------
		instance = this;
		mCart = ((TextView) findViewById(R.id.tv_cart));
		remind();
		fmap.clear();
		 fmap.put(R.id._tab_zhiyou, new MainZYFragment());
//		fmap.put(R.id._tab_zhiyou, new DirectMailPage());
		// fmap.put(R.id._tab_xianhuo, new MainXHFragment());
		fmap.put(R.id._tab_xianhuo, new GoodsInStock());
		fmap.put(R.id._tab_cart, new MainCartFragment());
		fmap.put(R.id._tab_main, new MainMyFragment());

		initView();

	}

	/**
	 * 防止fragment崩溃
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getCartnumber();
	}

	public void toZY() {
		((RadioButton) findViewById(R.id._tab_zhiyou)).setChecked(true);
		switchContent(fmap.get(R.id._tab_zhiyou), R.id._tab_zhiyou);
	}

	public void toCart() {
		((RadioButton) findViewById(R.id._tab_cart)).setChecked(true);
		switchContent(fmap.get(R.id._tab_cart), R.id._tab_cart);
	}

	/**
	 * 购物车 数量标识
	 * 
	 * @param view
	 */
	private void remind() {
		// BadgeView的具体使用
		badge1 = new MyBadgeView(this, mCart);
		// 需要显示的提醒类容
		badge1.setBadgePosition(MyBadgeView.POSITION_TOP_RIGHT);// 显示的位置.右上角,BadgeView.POSITION_BOTTOM_LEFT,下左，还有其他几个属性
		badge1.setTextColor(Color.WHITE); // 文本颜色
		badge1.setBadgeBackgroundColor(getResources().getColor(R.color.main_color)); // 提醒信息的背景颜色，自己设置
		badge1.setTextSize(10); // 文本大小
		badge1.setGravity(Gravity.CENTER);
		badge1.setBadgeMargin(3);// 各边间隔

	}

	/**
	 * 获取购物车数量
	 */
	public void getCartnumber() {
		String uuid = getIMEI();
		String userid;
		if (isLogged()) {
			userid = getLoginUserId();
		} else {
			userid = "0";
		}
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("UserId", userid);
		map.put("UUID", uuid);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwGetShppingCartSum?UserId="
				+ userid + "&UUID=" + uuid + "&Sign=" + Sign + "&Ts=" + Ts);
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						ScaleAnimation animation = new ScaleAnimation(0.0f, 1.2f, 0.0f, 1.2f,
								Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f);
						animation.setDuration(1000);// 设置动画持续时间
						ScaleAnimation animation1 = new ScaleAnimation(0.0f, 1.2f, 0.0f, 1.2f,
								Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f);
						animation1.setDuration(500);// 设置动画持续时间

						String number = jo.getString("Data");

						int i = Integer.parseInt(number);
						if (i == 0) {
							badge1.hide();
						} else if (i > 99) {
							// 动画效果
							badge1.toggle(animation, animation1);
							badge1.setText("99+");
							badge1.show();
						} else if (number.equals(badge1.getText().toString().trim())) {
							badge1.setText(number);
							badge1.show();
						} else {
							// 动画效果
							badge1.toggle(animation, animation1);
							badge1.setText(number);
							badge1.show();
						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(CancelledException cex) {
				Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFinished() {

			}
		});
	}

	private void initView() {

		radioGroup = (RadioGroup) findViewById(R.id.ll);
		((RadioButton) radioGroup.findViewById(R.id._tab_zhiyou)).setChecked(true);
		getSupportFragmentManager().beginTransaction().add(R.id.content, fmap.get(R.id._tab_zhiyou)).commit();

		currentRadio = R.id._tab_zhiyou;
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int i) {
				Fragment temp = fmap.get(i);
				if (temp != null) {
					switch (i) {
					case R.id._tab_zhiyou:
						switchContent(temp, i);
						break;
					case R.id._tab_xianhuo:
						switchContent(temp, i);
						break;
					case R.id._tab_cart:
						switchContent(temp, i);
						break;
					case R.id._tab_main:
						switchContent(temp, i);
						break;
					}

				}
			}
		});

	}

	/** 修改显示的内容 不会重新加载 */
	public void switchContent(Fragment to, int id) {
		mContent = fmap.get(currentRadio);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		if (to != mContent) {
			if (!to.isAdded()) { // 先判断是否被add过
				transaction.hide(mContent).add(R.id.content, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
			} else {
				transaction.hide(mContent).show(to).commit(); // 隐藏当前的fragment，显示下一个
			}
			currentRadio = id;
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				this.finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
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

		XutilsHttp.getInstance().get(url, null, new searchCallBack(), 0, code, this);

	}

	class searchCallBack implements XCallBackID {

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

}
