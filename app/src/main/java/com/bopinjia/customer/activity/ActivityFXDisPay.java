package com.bopinjia.customer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alipay.sdk.app.PayTask;
import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterMemeberLevelModel;
import com.bopinjia.customer.adapter.AdapterMemeberLevellist;
import com.bopinjia.customer.alipay.AlipayRequest;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.CustomProgressDialog;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityFXDisPay extends BaseActivity {
	private String orderId;
	private String payid;
	private String name;
	private RadioButton alipayBtn;
	private RadioButton weixinpayBtn;

	private List<RadioButton> radionButtonList = new ArrayList<RadioButton>();

	public static ActivityFXDisPay instance = null;
	private String Msgtype;
	private TextView mName;
	private TextView mTVprice;

	private String paytype;

	private String price;
	/** 续费时显示的等级列表 */
	private ListView list;
	private List<AdapterMemeberLevelModel> dataList;
	private String mSelfLevel;
	/** 开通续费时显示的时间 */
	private LinearLayout mLLtime;
	/** 获取选择支付的等级 */
	private String selectLevel;

	private boolean isXf = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_distributionpay);
		instance = this;

		findViewById(R.id.ll_aipay).setVisibility(View.GONE);

		list = (ListView) findViewById(R.id.list);

		mLLtime = (LinearLayout) findViewById(R.id.ll_time);

		mSelfLevel = getBopinjiaSharedPreference(Constants.KEY_FXS_LEVEL);
		if (getIntent().hasExtra("mytype")) {
			isXf = true;
		} else {
			isXf = false;
		}
		name = getIntent().getStringExtra("name");
		payid = getIntent().getStringExtra("id");
		// type= 1 申请。type = 2 续费。 type=3 。升级
		Msgtype = getIntent().getStringExtra("type");

		if (Msgtype.equals("1")) {
			paytype = "2";
			putSharedPreferences(Constants.KEY_PREFERENCE_PAY_TYPE, "2");
		} else if (Msgtype.equals("2")) {
			paytype = "3";
			list.setVisibility(View.VISIBLE);
			mLLtime.setVisibility(View.GONE);
			putSharedPreferences(Constants.KEY_PREFERENCE_PAY_TYPE, "3");
		} else if (Msgtype.equals("3")) {
			paytype = "4";
			putSharedPreferences(Constants.KEY_PREFERENCE_PAY_TYPE, "4");
		}

		getOrder(1);
		mTVprice = (TextView) findViewById(R.id.txt_total_amount);
		mName = (TextView) findViewById(R.id.tv_name);

		// mName.setText(name + "一年");

		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 去支付
		findViewById(R.id.btn_go_pay).setOnClickListener(this);

		alipayBtn = (RadioButton) findViewById(R.id.chk_alipay);
		alipayBtn.setOnClickListener(this);
		radionButtonList.add(alipayBtn);

		weixinpayBtn = (RadioButton) findViewById(R.id.chk_weixinpay);
		weixinpayBtn.setOnClickListener(this);
		radionButtonList.add(weixinpayBtn);
		getGDLlist();
	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		switch (viewId) {
		case R.id.chk_alipay:
		case R.id.chk_weixinpay:
			for (RadioButton button : radionButtonList) {
				if (button.getId() != viewId) {
					button.setChecked(false);
				}
			}
			break;
		case R.id.btn_return:
			finish();
			break;
		case R.id.btn_go_pay:

			if (orderId == null || orderId.equals("")) {
				showToast("支付数据异常，请稍后支付");
			} else {
				if (isXf) {
					for (int i = 0, j = list.getCount(); i < j; i++) {
						View child = list.getChildAt(i);
						RadioButton rdoBtn = (RadioButton) child.findViewById(R.id.rb);
						if (rdoBtn.isChecked()) {

							payid = dataList.get(i).getId();
							selectLevel = dataList.get(i).getLevel();
							name = dataList.get(i).getName();
						}
					}

					String level = getBopinjiaSharedPreference(Constants.KEY_FXS_LEVEL);

					if (level.equals(selectLevel) || selectLevel == null) {
						// 续费
						Msgtype = "2";
						paytype = "3";
						putSharedPreferences(Constants.KEY_PREFERENCE_PAY_TYPE, "3");
					} else {
						// 升级
						Msgtype = "3";
						paytype = "4";
						putSharedPreferences(Constants.KEY_PREFERENCE_PAY_TYPE, "4");
					}

					getOrder(2);
				} else {
					// 支付宝支付
					if (alipayBtn.isChecked()) {
						try {
							aliPay();
						} catch (JSONException e) {
						}
						return;
					}

					// 微信支付
					if (weixinpayBtn.isChecked()) {

						if (!isWeixinAvilible(this)) {
							showToast("请先安装微信客户端~");
						} else {
							GetOrderWXInfo();
						}
					}

				}
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 支付宝支付
	 */
	private void aliPay() throws JSONException {
		// 订单号
		String outTradeNo = orderId;
		// 商品名称
		String subject = name;
		// 商品描述
		String body = "";
		// 支付价格
		double orderPrice = new BigDecimal(price).doubleValue();
		String totalPrice = String.valueOf(orderPrice);

		String url = Constants.WEBAPI_ADDRESS + "api/AlipayNotify/BpwGDSServiceCallBack";

		// 支付宝支付请求
		final AlipayRequest payReq = new AlipayRequest(outTradeNo, subject, body, totalPrice, url);

		Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(ActivityFXDisPay.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payReq.generateAlipayOrderInfo());

				// 跳转到支付结果页面
				Intent intent = new Intent();
				intent.putExtra("alipayResult", result);
				intent.putExtra("orderId", orderId);
				/**
				 * type 判断从哪进入回调 1 普通购物 2 开通分销会员 3分销会员续费 4分销会员升级
				 */
				intent.putExtra("type", paytype);
				forward(ActivityAlipayResult.class, intent);
				if (Msgtype.equals("1")) {
					ActivityFXDisLevel.instance.finish();
				}

			}
		};

		// 必须异步调用
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	/**
	 * 微信支付
	 */
	private void weixinPay(JSONObject orderInfo) throws JSONException {
		// 参数设定
		PayReq payReq = new PayReq();
		payReq.appId = Constants.WEIXIN_APP_ID; // 商户在微信开放平台申请的应用id
		payReq.partnerId = orderInfo.getString("partnerid"); // 商户id
		payReq.packageValue = orderInfo.getString("package"); // 数据和签名
		payReq.nonceStr = orderInfo.getString("noncestr"); // 随机串，防重发
		payReq.timeStamp = orderInfo.getString("timestamp"); // 时间戳，防重发
		payReq.prepayId = orderInfo.getString("prepayid"); // 预支付订单
		payReq.sign = orderInfo.getString("sign"); // 签名

		// 发起微信支付
		IWXAPI api = WXAPIFactory.createWXAPI(this, Constants.WEIXIN_APP_ID);
		api.registerApp(Constants.WEIXIN_APP_ID);
		api.sendReq(payReq);
		if (Msgtype.equals("1")) {
			ActivityFXDisLevel.instance.finish();
		}

	}

	/**
	 * 获取微信需要的参数
	 */
	private void GetOrderWXInfo() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("OrderId", orderId);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Otype", "1");
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

		final CustomProgressDialog dialog = new CustomProgressDialog(this, "正在加载中", R.anim.frame);
		dialog.show();

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/GDSType/WXPay");
		params.addBodyParameter("OrderId", orderId);
		params.addBodyParameter("Sign", Sign);
		params.addBodyParameter("Otype", "1");
		params.addBodyParameter("Ts", Ts);
		params.setAsJsonContent(true);

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						JSONObject orderInfo = jo.getJSONObject("Data");
						weixinPay(orderInfo);
						dialog.dismiss();
					} else {
						showToast("支付异常！");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();

			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
			}
		});
	}

	/**
	 * 判断 用户是否安装微信客户端
	 */
	public boolean isWeixinAvilible(Context context) {
		final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
		List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName;
				if (pn.equals("com.tencent.mm")) {
					return true;
				}
			}
		}
		return false;
	}

	private void getOrder(int id) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();
		String MdId = "";
		if (Msgtype.equals("1")) {
			MdId = getBindingShop();
		} else {
			MdId = getBopinjiaSharedPreference(Constants.FXSMD);
		}

		map.put("UserID", getLoginUserId());
		map.put("MDUserID", MdId);

		map.put("GDSType_ID", payid);
		map.put("MsgType", Msgtype);
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
		maps.put("UserID", getLoginUserId());
		maps.put("MDUserID", MdId);
		maps.put("GDSType_ID", payid);
		maps.put("MsgType", Msgtype);

		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/GDSType/GDSSubmit", maps, new getOrderCallBack(),
				id, null, this);

	}

	class getOrderCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					if (id == 1) {
						findViewById(R.id.btn_go_pay).setEnabled(true);
						JSONObject jsData = jo.getJSONObject("Data");
						orderId = jsData.getString("OrderNum");
						price = jsData.getString("Buyprice");
						mTVprice.setText("¥" + price);

						((TextView) findViewById(R.id.tv_endtime)).setText("有效期：" + jsData.getString("Endtime"));

						mName.setText(jsData.getString("GDSTypeName"));
					} else if (id == 2) {
						findViewById(R.id.btn_go_pay).setEnabled(true);
						JSONObject jsData = jo.getJSONObject("Data");
						orderId = jsData.getString("OrderNum");
						price = jsData.getString("Buyprice");
						// 支付宝支付
						if (alipayBtn.isChecked()) {
							try {
								aliPay();
							} catch (JSONException e) {
							}
							return;
						}

						// 微信支付
						if (weixinpayBtn.isChecked()) {

							if (!isWeixinAvilible(ActivityFXDisPay.this)) {
								showToast("请先安装微信客户端~");
							} else {
								GetOrderWXInfo();
							}
						}

					}
				} else {
					showToast("获取信息失败");
					findViewById(R.id.btn_go_pay).setEnabled(false);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 获取会员级别列表
	 */
	private void getGDLlist() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
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

		String url = Constants.WEBAPI_ADDRESS + "api/GDSType/List_Upgrade?UserId=" + getLoginUserId() + "&Sign=" + Sign
				+ "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getGDLlistCallBack(), this);
	}

	class getGDLlistCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parseList(result);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 解析会员级别列表
	 * 
	 * @param jsonarray
	 */
	private void parseList(String jsonarray) {
		try {

			JSONObject jo = new JSONObject(jsonarray);
			JSONArray dataArray = jo.getJSONArray("Data");

			if (dataArray != null && dataArray.length() > 0) {
				dataList = new ArrayList<AdapterMemeberLevelModel>();
				int self = Integer.parseInt(mSelfLevel);
				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					int level = Integer.parseInt(data.getString("GDSType_Level"));
					if (level < self) {
						continue;
					}
					AdapterMemeberLevelModel m = new AdapterMemeberLevelModel();

					m.setImg(data.getString("GDSType_Img"));

					m.setName(data.getString("GDSType_Name"));

					m.setPrice(data.getString("GDSType_UpPrice"));

					m.setId(data.getString("GDSType_ID"));

					m.setTime(data.getString("GDSType_UpTime"));

					m.setLevel(data.getString("GDSType_Level"));

					m.setSelflevel(mSelfLevel);

					dataList.add(m);
				}

				AdapterMemeberLevellist mLevel = new AdapterMemeberLevellist(ActivityFXDisPay.this, dataList);
				list.setAdapter(mLevel);
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

}
