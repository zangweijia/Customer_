package com.bopinjia.customer.activity;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.alipay.sdk.app.PayTask;
import com.bopinjia.customer.R;
import com.bopinjia.customer.alipay.AlipayRequest;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Data;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class ActivityPay extends BaseActivity {

	private List<RadioButton> radionButtonList = new ArrayList<RadioButton>();
	private ProgressDialog m_pDialog;
	private String purchaseOrderId;
	private String payAmount;
	private boolean isOrder = false;
	public static String ALIPAY_NOTIFY_URL;

	private long mhour, mmin, msecond;// 天，小时，分钟，秒
	private TextView mHour, mMin, mSec;
	private boolean time = true;
	private long hours;
	private long minutes;

	private Dialog mDialog;
	private View dialogView;
	private Button confirmBt;
	private Button cancelBt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_pay);
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 去支付
		findViewById(R.id.btn_go_pay).setOnClickListener(this);
		// 自定义dialog
		showMyDialog();
		mHour = (TextView) findViewById(R.id.tv_hour);
		mMin = (TextView) findViewById(R.id.tv_min);
		mSec = (TextView) findViewById(R.id.tv_sec);

		alipayBtn = (RadioButton) findViewById(R.id.chk_alipay);
		alipayBtn.setOnClickListener(this);
		radionButtonList.add(alipayBtn);

		weixinpayBtn = (RadioButton) findViewById(R.id.chk_weixinpay);
		weixinpayBtn.setOnClickListener(this);
		radionButtonList.add(weixinpayBtn);

		// 采购订单Id
		Intent intent = getIntent();
		purchaseOrderId = intent.getStringExtra("OrderId");

		putSharedPreferences(Constants.KEY_PREFERENCE_ORDER_ID, purchaseOrderId);

		putSharedPreferences(Constants.KEY_PREFERENCE_PAY_TYPE, "1");
		payAmount = intent.getStringExtra("payAmount");
		productName = intent.getStringExtra("JsonSkuName");
		((TextView) findViewById(R.id.txt_total_amount))
				.setText(MessageFormat.format(getString(R.string.txt_pay_total_amount), "¥" + payAmount));

		// 设置支付宝回调地址
		if (intent.hasExtra("Mode")) {
			// 订单页直接跳转
			isOrder = true;
		} else {
			// 购物车直接付款
			isOrder = false;
		}
		ALIPAY_NOTIFY_URL = Constants.WEBAPI_ADDRESS + "api/AlipayNotify/BpwProductCallBack";

		// 设置倒计时时间
		if (!isOrder) {
			// 购物车直接付款
			mhour = 23;
			mmin = 59;
			msecond = 60;
		} else {
			// 订单页直接跳转
			String time = getIntent().getStringExtra("CreateTime");
			getSubtractTime(time);
			mhour = hours;
			mmin = minutes;
			msecond = 00;
		}
		handler.postDelayed(runnable, 1000);
		GetWXInfo();

	}

	private void showMyDialog() {
		// ------------------------------
		mDialog = new Dialog(this, R.style.CustomDialogTheme);
		dialogView = LayoutInflater.from(this).inflate(R.layout.send_tel_dailog, null);
		// 设置自定义的dialog布局
		mDialog.setContentView(dialogView);
		// false表示点击对话框以外的区域对话框不消失，true则相反
		mDialog.setCanceledOnTouchOutside(false);
		confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
		cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
		// ------------------------------

	}

	/**
	 * 时间相减，倒计时
	 * 
	 * @param time1
	 * @param time2
	 */
	private void getSubtractTime(String creattime) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String mNowTime = sdf.format(new Date());
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(df.parse(creattime));
			calendar.add(calendar.DATE, 1);// 把日期往后增加一天.整数往后推,负数往前移动
			Date date = calendar.getTime(); // 这个时间就是日期往后推一天的结果

			// Date date = df.parse(creattime);
			Date d1 = df.parse(mNowTime);
			long diff = date.getTime() - d1.getTime();
			long days = diff / (1000 * 60 * 60 * 24);
			hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
			minutes = (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) / (1000 * 60);
		} catch (Exception e) {
		}

	}

	Handler handler = new Handler();
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			handler.postDelayed(this, 1000);
			if (time) {
				msecond--;
				if (msecond < 0) {
					msecond = 60;
					mmin--;
					if (mmin < 0) {
						mmin = 59;
						mhour--;
						if (mhour < 0) {
							mhour = 0;
							msecond = 0;
							mmin = 0;
							time = false;
						}
					}
				}
				mSec.setText("" + msecond);
				mMin.setText("" + mmin);
				mHour.setText("" + mhour);
			} else {
				handler.removeCallbacks(runnable);
			}
		}
	};
	private String productName;
	private String otype;
	private RadioButton alipayBtn;
	private RadioButton weixinpayBtn;

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
			mDialog.show();
			// 获取自定义dialog布局控件
			((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("确认要离开收银台？");
			confirmBt.setText("狠心离开");
			// 确定按钮点击事件
			confirmBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.putExtra("status", "1");
					forward(ActivityOrderList.class, i);
					mDialog.dismiss();
					// 停止计时器
					handler.removeCallbacks(runnable);
					finish();

				}
			});
			cancelBt.setText("继续支付");
			// 取消按钮点击事件
			cancelBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			break;
		case R.id.btn_go_pay:
			// 微信支付
			TextView payBtn = (TextView) findViewById(R.id.btn_go_pay);
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
					try {
						if (mWXorderInfo != null) {
							weixinPay(mWXorderInfo);
						} else {
							showToast("获取支付信息失败");
						}

					} catch (JSONException e) {

						e.printStackTrace();
					}
				}
			}
			payBtn.setEnabled(true);
			break;
		default:
			break;
		}
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

	/**
	 * 支付宝支付
	 */
	private void aliPay() throws JSONException {
		// 订单号
		String outTradeNo = purchaseOrderId;
		// 商品名称
		String subject = productName;
		// 商品描述
		String body = "";
		// 支付价格
		double orderPrice = new BigDecimal(payAmount).doubleValue();
		String totalPrice = String.valueOf(orderPrice);

		String url = Constants.WEBAPI_ADDRESS + "api/AlipayNotify/BpwProductCallBack";
		// 支付宝支付请求
		final AlipayRequest payReq = new AlipayRequest(outTradeNo, subject, body, totalPrice, url);

		Runnable payRunnable = new Runnable() {
			@Override
			public void run() {
				// 构造PayTask 对象
				PayTask alipay = new PayTask(ActivityPay.this);
				// 调用支付接口，获取支付结果
				String result = alipay.pay(payReq.generateAlipayOrderInfo());

				// 跳转到支付结果页面
				Intent intent = new Intent();
				intent.putExtra("alipayResult", result);
				intent.putExtra("orderId", purchaseOrderId);
				// type 判断从哪进入回调 1 购物 2 开通分销会员
				intent.putExtra("type", "1");
				forward(ActivityAlipayResult.class, intent);
				finish();
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

		finish();
	}

	private JSONObject mWXorderInfo;

	/**
	 * 获取微信需要的参数
	 */
	private void GetWXInfo() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("OrderId", purchaseOrderId);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Otype", "");
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

		maps.put("OrderId", purchaseOrderId);
		maps.put("Sign", Sign);
		maps.put("Otype", "");
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/CSC/WXPay", maps, new GetWXInfoCallBack(), this);
	}

	class GetWXInfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject orderInfo = jo.getJSONObject("Data");
					mWXorderInfo = orderInfo;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 单个订单id获取微信需要的参数
	 */
	private void GetOrderWXInfo() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("OrderId", purchaseOrderId);
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

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("OrderId", purchaseOrderId);
		maps.put("Otype", otype);
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/Order/WXPay", maps, new GetOrderWXInfoCallBack(),
				this);

	}

	class GetOrderWXInfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					m_pDialog.hide();
					JSONObject orderInfo = jo.getJSONObject("Data");
					weixinPay(orderInfo);
				} else {
					m_pDialog.hide();
					showToast("支付失败！");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mDialog.show();
			// 获取自定义dialog布局控件
			((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("确认要离开收银台？");
			confirmBt.setText("狠心离开");
			// 确定按钮点击事件
			confirmBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent i = new Intent();
					i.putExtra("status", "1");
					forward(ActivityOrderList.class, i);
					mDialog.dismiss();
					// 停止计时器
					handler.removeCallbacks(runnable);
					finish();

				}
			});
			cancelBt.setText("继续支付");
			// 取消按钮点击事件
			cancelBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});

		}
		return false;
	}

}
