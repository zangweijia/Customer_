package com.bopinjia.customer.activity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.LoginBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.StringUtils;
import com.google.gson.Gson;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityBindRegister extends BaseActivity {
	/**
	 * 返回
	 */
	@ViewInject(R.id.btn_return)
	private TextView mBack;
	/**
	 * 输入手机号
	 */
	@ViewInject(R.id.et_phone)
	private EditText mEtPhone;

	/**
	 * 删除手机号按钮
	 */
	@ViewInject(R.id.iv_delete_phone)
	private ImageView mDeletePhone;

	/**
	 * 倒计时
	 */
	@ViewInject(R.id.tv_daojishi)
	private TextView mTvDaoJiShi;

	/**
	 * 输入验证码
	 */
	@ViewInject(R.id.et_verification_code)
	private EditText mEtCode;
	/**
	 * 删除验证码
	 */
	@ViewInject(R.id.iv_delete_verification_code)
	private ImageView mDeleteCode;

	/**
	 * 输入密码
	 */
	@ViewInject(R.id.et_password)
	private EditText mEtPassWord;
	/**
	 * 密码是否显示明文
	 */
	@ViewInject(R.id.iv_password_isshow)
	private ImageView mYan;

	/**
	 * 注册按钮
	 */
	@ViewInject(R.id.tvbtn_register)
	private TextView mBtnReGister;
	/**
	 * 注册协议
	 */
	@ViewInject(R.id.zhucexieyi)
	private TextView mZCXY;

	@ViewInject(R.id.ck)
	private CheckBox mCK;

	private String mRandom;
	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String mD5Pass;

	private boolean isHidden = true;
	private String mType;

	private String uid,unionid;

	private String mResult;

	private String phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_bind_register);
		x.view().inject(this);
		init();

	}

	private void init() {
		mBtnReGister.setOnClickListener(this);
		mDeletePhone.setOnClickListener(this);
		mYan.setOnClickListener(this);
		mTvDaoJiShi.setOnClickListener(this);
		mDeleteCode.setOnClickListener(this);
		mBack.setOnClickListener(this);
		mZCXY.setOnClickListener(this);
		mEtPhone.addTextChangedListener(myTextWatcher);
		mEtCode.addTextChangedListener(myCodeTextWatcher);
		uid = getIntent().getStringExtra("uid");
		unionid= getIntent().getStringExtra("unionid");
		
		String type = getIntent().getStringExtra("type");
		TextView tv = (TextView) findViewById(R.id.tv);
		if (type != null) {
			if (type.equals("xl")) {
				tv.setText("注册后，您的新浪账号与舶品家账号都可以登录");
				mType = "3";
			} else if (type.equals("qq")) {
				tv.setText("注册后，您的腾讯账号与舶品家账号都可以登录");
				mType = "2";
			} else if (type.equals("wx")) {
				tv.setText("注册后，您的微信账号与舶品家账号都可以登录");
				mType = "1";
			} else {

			}
		}
	}

	/**
	 * 监听mEtPhone 如果有输入显示，否则隐藏
	 */
	TextWatcher myTextWatcher = new TextWatcher() {
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
				mDeletePhone.setVisibility(View.VISIBLE);
			} else {
				mDeletePhone.setVisibility(View.GONE);
			}

		}
	};
	/**
	 * 监听mEtCode 如果有输入显示，否则隐藏
	 */
	TextWatcher myCodeTextWatcher = new TextWatcher() {
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
			if (s.length() > 3) {
				mDeleteCode.setVisibility(View.VISIBLE);
			} else {
				mDeleteCode.setVisibility(View.GONE);
			}

			if (s.length() == 6) {
				mEtPassWord.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mEtPassWord, InputMethodManager.HIDE_NOT_ALWAYS);
			}

		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tv_daojishi:

			// 验证码未申请时
			if (StringUtils.isNull(mEtPhone.getText().toString().trim())) {
				showToast("请输入手机号！");
				return;
			} else if (mEtPhone.getText().toString().trim().length() != 11) {
				showToast("请输入11位正确手机号！");
				return;
			}
			whether();

			break;
		case R.id.tvbtn_register:
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
			if (StringUtils.isNull(mEtPassWord.getText().toString().trim())) {
				showToast(MessageFormat.format(getString(R.string.msg_err_empty), "密码"));
				return;
			} else {
				// 特殊字符校验
				if (StringUtils.validPass(mEtPassWord.getText().toString().trim())) {
					showToast(MessageFormat.format(getString(R.string.msg_err_invalid), "密码"));
					return;
				}
			}
			if ((mEtPassWord.getText().toString().trim()).length() < 6) {
				showToast("请输入6为及以上密码");

			}
			mD5Pass = MD5.Md5(mEtPassWord.getText().toString().trim());

			if (mCK.isChecked()) {
				RegisterUse(mType, uid,unionid);
			} else {
				showToast("请先同意舶品网注册协议");
				return;
			}

			break;
		case R.id.iv_delete_verification_code:
			mEtCode.setText("");
			break;
		case R.id.iv_delete_phone:
			mEtPhone.setText("");
			break;
		case R.id.btn_return:
			finish();
			break;
		case R.id.zhucexieyi:
			Intent i = new Intent();
			i.putExtra("newsId", "7");
			forward(ActivityCustomerNews.class, i);
			break;
		case R.id.iv_password_isshow:
			mYan.setSelected(!mYan.isSelected());
			if (isHidden) {
				// 设置EditText文本为可见的
				mEtPassWord.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			} else {
				// 设置EditText文本为隐藏的
				mEtPassWord.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			isHidden = !isHidden;
			mEtPassWord.postInvalidate();
			// 切换后将EditText光标置于末尾
			CharSequence charSequence = mEtPassWord.getText();
			if (charSequence instanceof Spannable) {
				Spannable spanText = (Spannable) charSequence;
				Selection.setSelection(spanText, charSequence.length());
			}
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
		String message = MessageFormat.format(getString(R.string.msg_info_verify), mRandom);

		SendSMS sm = new SendSMS(this, new sendMsgCallBack());
		sm.sendSMS(message, mEtPhone.getText().toString());

		// 短信发送成功之后倒计时弹出toast
		showToast(getString(R.string.msg_info_verify_sended));

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
	 * 验证码发送回调
	 * 
	 * @author ZWJ
	 */
	class sendMsgCallBack implements MessageCallBack {
		@Override
		public void DisposalProblem() {
			showToast(getString(R.string.msg_info_verify_sended));
		}

	}

	/**
	 * 注册用户
	 */
	private void RegisterUse(String type, String bindingaccount,String unionid) {
		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();
		phone = mEtPhone.getText().toString().trim();
		String uuid = getIMEI();

		map.put("UserUnionID", unionid);
		map.put("RegisterPhone", phone);
		map.put("Password", mD5Pass);
		map.put("UserType", type);
		map.put("UserBindingAccount", bindingaccount);
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

		String url = Constants.WEBAPI_ADDRESS + "api/UserBinding/RegisterQuicklogin_New?RegisterPhone=" + phone
				+ "&Password=" + mD5Pass + "&UserType=" + type + "&UserBindingAccount=" + bindingaccount +"&UserUnionID="+unionid+ "&UUID="
				+ uuid + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new RegisterUseCallBack(),this);
	}

	/**
	 * 注册用户回调
	 * 
	 * @author ZWJ
	 *
	 */
	class RegisterUseCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			mResult = result;
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					View view = getLayoutInflater().inflate(R.layout.activity_success, null);
					TextView tv = (TextView) view.findViewById(R.id.txt);
					tv.setText("注册成功");

					AlertDialog m = new AlertDialog.Builder(ActivityBindRegister.this).create();
					m.requestWindowFeature(Window.FEATURE_NO_TITLE);
					m.getWindow().setLayout(200, 200);
					m.setView(view, 0, 0, 0, 0);
					m.show();
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								Thread.sleep(2000);
								Gson gson = new Gson();
								LoginBean mLogBean = new LoginBean();
								mLogBean = gson.fromJson(mResult, LoginBean.class);
								putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "1");
								String userId = mLogBean.getData().getUserId().toString();
								putSharedPreferences(Constants.KEY_PREFERENCE_USER_ID, userId);
								putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, phone);
								putSharedPreferences(Constants.KEY_PREFERENCE_PASSWORD, mD5Pass);

								ActivityLogin.instance.finish();
								ActivityOtherLogin.instance.finish();
								finish();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}).start();
				} else {
					showToast(jo.getString("Message"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 判断手机号是否注册
	 */
	private void whether() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("Phone", mEtPhone.getText().toString().trim());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		String url = Constants.WEBAPI_BOPINWANG + "api/User/ExistsRegisterPhone?RegisterPhone="
				+ mEtPhone.getText().toString().trim() + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new WhetherCallBack(),this);

	}

	/**
	 * 判断手机号是否注册回调
	 */
	class WhetherCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// 已注册

					showToast("亲，您的手机号已被注册");
				} else if (jsonresult.equals("2")) {
					// 未注册
					sendSms();
					mEtCode.requestFocus();
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(mEtCode, InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
					showErr();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}
}
