package com.bopinjia.customer.activity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ReflectAccountBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;
import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityFXSubmitAccount extends BaseActivity {

	private TextView mTiTleBack;
	private TextView mTiTleName;
	private String type;

	/** 账户名 */
	@ViewInject(R.id.tv_account)
	private TextView mAccount;

	/** 确定 */
	@ViewInject(R.id.tv_enter)
	private TextView mEnter;
	/** 倒计时 */
	@ViewInject(R.id.tv_daojishi)
	private TextView mDaoJiShi;

	/** ET 姓名 */
	@ViewInject(R.id.edt_name)
	private EditText mEtName;

	/** ET 账户 */
	@ViewInject(R.id.edt_account)
	private EditText mEtAccount;

	/** ET 手机号 */
	@ViewInject(R.id.edt_phone)
	private EditText mEtPhone;

	/** ET 验证码 */
	@ViewInject(R.id.edt_code)
	private EditText mEtCode;

	private int mType;

	private String mRandom;
	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String json;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_fxsubmit_account);
		x.view().inject(this);
		type = getIntent().getStringExtra("type");

		setTitle();
		init();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		if (type.equals("1")) {
			mTiTleName.setText("添加支付宝账号");
		} else if (type.equals("2")) {
			mTiTleName.setText("添加微信账号");
		} else if (type.equals("3")) {
			mTiTleName.setText("添加银行卡");
		}

		mTiTleBack.setOnClickListener(this);

	}

	private void init() {
		mType = Integer.parseInt(type);
		setdefult(mType);
		mEnter.setOnClickListener(this);
		mDaoJiShi.setOnClickListener(this);

		if (getIntent().hasExtra("json")) {
			json = getIntent().getStringExtra("json");
		}

		if (json != null) {
			try {
				JSONObject jo = new JSONObject(json);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					Gson gson = new Gson();
					JSONArray jsonarray = jo.getJSONArray("Data");
					for (int i = 0; i < jsonarray.length(); i++) {
						ReflectAccountBean mReflectBean = new ReflectAccountBean();
						mReflectBean = gson.fromJson(jsonarray.get(i).toString(), ReflectAccountBean.class);

						if (mReflectBean.getUserAccountTypeId().equals(type)) {
							mEtName.setText(mReflectBean.getUserAccountRealName());
							mEtAccount.setText(mReflectBean.getUserAccounts());
							mEtPhone.setText(mReflectBean.getUserAccountPhone());
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * 设置默认提示
	 * 
	 * @param i
	 */
	private void setdefult(int i) {
		switch (i) {
		case 1:
			mAccount.setText("支付宝账号");
			mEtAccount.setHint("请输入支付宝账号");
			mEtName.setHint("真实姓名");
			mEtPhone.setHint("请输入联系电话");
			mEtCode.setHint("请输入短息验证码");
			break;
		case 2:
			mAccount.setText("微信账号");
			mEtAccount.setHint("请输入微信账号");
			mEtName.setHint("真实姓名");
			mEtPhone.setHint("请输入联系电话");
			mEtCode.setHint("请输入短息验证码");
			break;
		case 3:
			mAccount.setText("银行卡号");
			mEtAccount.setHint("请输入银行卡账号");
			mEtName.setHint("请与银行卡姓名一致");
			mEtPhone.setHint("请输入银行预留手机号");
			mEtCode.setHint("请输入短息验证码");
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			finish();
			break;
		case R.id.tv_enter:
			if (StringUtils.isNull(mEtName.getText().toString().trim())
					|| StringUtils.isNull(mEtPhone.getText().toString().trim())
					|| StringUtils.isNull(mEtAccount.getText().toString().trim())
					|| StringUtils.isNull(mEtCode.getText().toString().trim())) {
				showToast("请确认所有信息都已经填写！");
				break;
			}
			if (mEtPhone.getText().toString().trim().length() != 11) {
				showToast("请正确输入11位联系电话 ！");
				break;
			}
			// 验证码未申请时
			if (StringUtils.isNull(mEtCode.getText().toString().trim())) {
				showToast(getString(R.string.msg_err_verify_unrequest));
				return;
			}
			// 验证码校验
			if (!mRandom.equals(mEtCode.getText().toString().trim())) {
				showToast(getString(R.string.msg_err_verify_wrong));
				return;
			}

			if (mType == 3) {
				getBank(mEtAccount.getText().toString().trim());

			} else {
				sendUserAccount("银联");
			}

			break;

		case R.id.tv_daojishi:

			// 验证码未申请时
			if (StringUtils.isNull(mEtPhone.getText().toString().trim())) {
				showToast("请输入联系电话 ！");
				return;
			} else if (mEtPhone.getText().toString().trim().length() != 11) {
				showToast("请输入11位正确联系电话 ！");
				return;
			}

			sendSms(mEtPhone.getText().toString().trim());
		default:
			break;
		}
	}

	/**
	 * 发送验证短信
	 */
	private void sendSms(String phone) {
		// 生成一个6位随机数
		DecimalFormat df = new DecimalFormat("000000");
		mRandom = df.format(Math.random() * 100000);
		// mRandom = "123456";
		String message = MessageFormat.format(getString(R.string.msg_info_verify), mRandom);

		SendSMS sm = new SendSMS(this, new sendsmsSuccess());
		sm.sendSMS(message, phone);

		final Handler handler = new Handler() {
			@SuppressLint("HandlerLeak")
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					mDaoJiShi.setEnabled(false);
					interval--;
					mDaoJiShi.setText(interval + "");
					break;
				case 2:
					mDaoJiShi.setEnabled(true);
					mDaoJiShi.setText("重新发送");
					interval = Constants.MAX_INTERVAL_FOR_SECURITY;
					break;
				}
				super.handleMessage(msg);
			}
		};

		TimerTask task = new TimerTask() {
			public void run() {
				Message message = new Message();
				if (interval > 0) {
					message.what = 1;
					handler.sendMessage(message);
				} else {
					message.what = 2;
					handler.sendMessage(message);
					cancel();
				}

			}
		};

		Timer timer = new Timer(true);
		timer.schedule(task, 0, 1000);

	}

	/**
	 * 短信发送成功回调
	 */
	class sendsmsSuccess implements MessageCallBack {

		@Override
		public void DisposalProblem() {
			// 短信发送成功之后倒计时弹出toast
			showToast(getString(R.string.msg_info_verify_sended));
		}

	}

	private void sendUserAccount(String bankname) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		String Ts = MD5.getTimeStamp();

		map.put("UserId", getLoginUserId());
		map.put("UserAccountTypeId", type);
		map.put("UserAccount", mEtAccount.getText().toString().trim());
		map.put("UserAccountName", bankname);
		map.put("UserAccountPhone", mEtPhone.getText().toString().trim());
		map.put("UserAccountRealName", mEtName.getText().toString().trim());
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

		maps.put("UserAccountTypeId", type);
		maps.put("UserAccount", mEtAccount.getText().toString().trim());
		maps.put("UserAccountName", bankname);

		maps.put("UserAccountPhone", mEtPhone.getText().toString().trim());
		maps.put("UserAccountRealName", mEtName.getText().toString().trim());

		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/UserAccount/Add", maps,
				new sendUserAccountCallBack(), this);
	}

	class sendUserAccountCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					showToast("提交申请成功");
					backward();

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void getBank(String strbank) {
		String url = "http://api.avatardata.cn/Bank/Query?key=ab9fb259d0a34ae5b963d15620dab4a9&cardnum=" + strbank;

		XutilsHttp.getInstance().get(url, null, new getBankCallBack(), this);
	}

	class getBankCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("error_code");
				if (jsonresult.equals("0")) {
					JSONObject mResult = jo.getJSONObject("result");
					String mBankName = mResult.getString("bankname");
					sendUserAccount(mBankName);
				} else if (jsonresult.equals("1")) {
					showToast("银行卡信息有误，请检查");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
