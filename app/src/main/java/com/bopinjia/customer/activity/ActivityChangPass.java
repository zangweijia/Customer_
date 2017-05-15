package com.bopinjia.customer.activity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityChangPass extends BaseActivity {

	private EditText mEtPhone, mEtCode, mEtPass, mEtAgainPass;

	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String mRandom;
	private TextView mTvDaoJiShi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_chang_pass);

		init();

	}

	private void init() {

		mTvDaoJiShi = (TextView) findViewById(R.id.tv_getcode);

		findViewById(R.id.tv_back).setOnClickListener(this);
		findViewById(R.id.tv_getcode).setOnClickListener(this);
		findViewById(R.id.iv_delete_verification_code).setOnClickListener(this);
		findViewById(R.id.iv_delete_phone).setOnClickListener(this);
		findViewById(R.id.iv_delete_pass).setOnClickListener(this);
		findViewById(R.id.iv_delete_again_pass).setOnClickListener(this);
		findViewById(R.id.tv_next).setOnClickListener(this);

		mEtPhone = (EditText) findViewById(R.id.et_phone);
		mEtPhone.addTextChangedListener(mPhoneTextWatcher);
		mEtCode = (EditText) findViewById(R.id.et_verification_code);
		mEtCode.addTextChangedListener(mCodeTextWatcher);
		mEtPass = (EditText) findViewById(R.id.et_pass);
		mEtPass.addTextChangedListener(mPassTextWatcher);
		mEtAgainPass = (EditText) findViewById(R.id.et_again_pass);
		mEtAgainPass.addTextChangedListener(mAgainPassTextWatcher);
	}

	/**
	 * 监听mEtPhone 如果有输入显示，否则隐藏
	 */
	TextWatcher mPhoneTextWatcher = new TextWatcher() {
		String s;

		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count) {
			s = text.toString().trim();
		}

		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable edit) {
			if (s.length() > 0) {
				findViewById(R.id.iv_delete_phone).setVisibility(View.VISIBLE);
				findViewById(R.id.tv_next).setBackgroundResource(R.drawable.bg_press_);
			} else {
				findViewById(R.id.iv_delete_phone).setVisibility(View.GONE);
				findViewById(R.id.tv_next).setBackgroundResource(R.drawable.bg_password_next);
			}
		}
	};
	/**
	 * 监听mEtCode 如果有输入显示，否则隐藏
	 */
	TextWatcher mCodeTextWatcher = new TextWatcher() {
		String s;

		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count) {
			s = text.toString().trim();
		}

		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable edit) {
			if (s.length() > 0) {
				findViewById(R.id.iv_delete_verification_code).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.iv_delete_verification_code).setVisibility(View.GONE);
			}
		}
	};
	/**
	 * 监听mEtPass 如果有输入显示，否则隐藏
	 */
	TextWatcher mPassTextWatcher = new TextWatcher() {
		String s;

		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count) {
			s = text.toString().trim();
		}

		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable edit) {
			if (s.length() > 0) {
				findViewById(R.id.iv_delete_pass).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.iv_delete_pass).setVisibility(View.GONE);
			}
		}
	};
	/**
	 * 监听mEtAgainPass 如果有输入显示，否则隐藏
	 */
	TextWatcher mAgainPassTextWatcher = new TextWatcher() {
		String s;

		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count) {
			s = text.toString().trim();
		}

		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable edit) {
			if (s.length() > 0) {
				findViewById(R.id.iv_delete_again_pass).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.iv_delete_again_pass).setVisibility(View.GONE);
			}
		}
	};

	private String mD5Pass;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_back:
			backward();
			break;
		case R.id.tv_getcode:
			// 验证码未申请时
			if (StringUtils.isNull(mEtPhone.getText().toString().trim())) {
				showToast("请输入手机号！");
				return;
			} else {
				sendSms();
			}
			break;
		case R.id.iv_delete_verification_code:
			mEtCode.setText("");
			break;
		case R.id.iv_delete_phone:
			mEtPhone.setText("");
			break;
		case R.id.iv_delete_pass:
			mEtPass.setText("");
			break;
		case R.id.iv_delete_again_pass:
			mEtAgainPass.setText("");
			break;
		case R.id.tv_next:
			// 验证码未申请时
			if (StringUtils.isNull(mRandom)) {
				showToast(getString(R.string.msg_err_verify_unrequest));
				return;
			}
			// 验证码校验
			if (!mRandom.equals(mEtCode.getText().toString().trim())) {
				showToast(getString(R.string.msg_err_verify_wrong));
				return;
			}
			if (StringUtils.isNull(mEtPass.getText().toString().trim())) {
				showToast(MessageFormat.format(getString(R.string.msg_err_empty), "密码"));
				return;
			} else {
				// 特殊字符校验
				if (StringUtils.validPass(mEtPass.getText().toString().trim())) {
					showToast(MessageFormat.format(getString(R.string.msg_err_invalid), "密码"));
					return;
				}
			}

			if (!mEtPass.getText().toString().trim().equals(mEtAgainPass.getText().toString().trim())) {
				showToast(getString(R.string.msg_err_confirm));
				return;
			}

			mD5Pass = MD5.Md5(mEtPass.getText().toString().trim());
			ResetCustomerPassword();
			break;

		default:
			break;
		}
	}

	/**
	 * 发送验证短信
	 */
	private void sendSms() {
		// 生成一个6位随机数
		DecimalFormat df = new DecimalFormat("000000");
		mRandom = df.format(Math.random() * 100000);
		// mRandom = "123456";
		String message = MessageFormat.format(getString(R.string.msg_info_verify), mRandom);

		SendSMS sm = new SendSMS(this, new sendsmsSuccess());
		sm.sendSMS(message, mEtPhone.getText().toString());

		final Handler handler = new Handler() {
			@SuppressLint("HandlerLeak")
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					mTvDaoJiShi.setEnabled(false);
					interval--;
					mTvDaoJiShi.setText(interval + "");
					break;
				case 2:
					mTvDaoJiShi.setEnabled(true);
					mTvDaoJiShi.setText("重新发送");
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

	private void ResetCustomerPassword() {
		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();

		map.put("RegisterPhone", mEtPhone.getText().toString().trim());
		map.put("Password", mD5Pass);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		Map<String, String> maps = new HashMap<String, String>();
		maps.put("RegisterPhone", mEtPhone.getText().toString().trim());
		maps.put("Password", mD5Pass);
		maps.put("Sign", NetUtils.getSign(map));
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/User/ResetCustomerPassword", maps,
				new ResetCustomerPasswordCallBack(), this);
	}

	class ResetCustomerPasswordCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					View view = getLayoutInflater().inflate(R.layout.activity_success, null);
					TextView tv = (TextView) view.findViewById(R.id.txt);
					tv.setText("修改成功");
					AlertDialog m = new AlertDialog.Builder(ActivityChangPass.this).create();
					m.requestWindowFeature(Window.FEATURE_NO_TITLE);
					m.setView(view, 0, 0, 0, 0);
					m.show();
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								Thread.sleep(3000);
								putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "0");
								removeSharedPreferences(Constants.KEY_PREFERENCE_PASSWORD);
								forward(ActivityLogin.class);
								ActivitySetting.instance.finish();
								ActivityUserSecurity.instance.finish();
								finish();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}).start();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
