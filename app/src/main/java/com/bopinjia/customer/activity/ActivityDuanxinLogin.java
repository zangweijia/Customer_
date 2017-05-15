package com.bopinjia.customer.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.LoginBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

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

public class ActivityDuanxinLogin extends BaseActivity {

	/**
	 * 倒计时
	 */
	@ViewInject(R.id.tv_daojishi)
	private TextView mTvDaoJiShi;

	@ViewInject(R.id.tv_back)
	private TextView mBack;

	@ViewInject(R.id.et_phone)
	private EditText mEtPhone;

	@ViewInject(R.id.et_code)
	private EditText mEtCode;

	@ViewInject(R.id.tvbtn_login)
	private TextView mLogin;

	@ViewInject(R.id.iv_delete_phone)
	private ImageView mDeletePhone;
	@ViewInject(R.id.iv_code_delete)
	private ImageView mDeleteCode;

	private String mRandom;
	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String device_token;
	/**
	 * 手机号是否注册
	 */
	private boolean isHas = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_duanxin_login);
		x.view().inject(this);
		init();

	}

	private void init() {


		mTvDaoJiShi.setOnClickListener(this);
		mLogin.setOnClickListener(this);
		mDeletePhone.setOnClickListener(this);
		findViewById(R.id.tv_back).setOnClickListener(this);
		findViewById(R.id.iv_delete_phone).setOnClickListener(this);
		findViewById(R.id.iv_code_delete).setOnClickListener(this);
		mEtPhone.addTextChangedListener(myPhoneTextWatcher);
		mEtCode.addTextChangedListener(myTextWatcher);
	}

	/**
	 * code监听
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
				mDeleteCode.setVisibility(View.VISIBLE);
			} else {
				mDeleteCode.setVisibility(View.GONE);

			}
		}
	};
	/**
	 * phone监听
	 */
	TextWatcher myPhoneTextWatcher = new TextWatcher() {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_daojishi:
			if (StringUtils.isNull(mEtPhone.getText().toString().trim())) {
				// 手机号未输入时
				showToast("请输入手机号！");
				return;
			} else {
				sendSms();
				// 自动跳到输入验证码Edittext
				mEtCode.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mEtCode, InputMethodManager.HIDE_NOT_ALWAYS);

			}
			break;
		case R.id.tvbtn_login:
			// 登录
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
			Login();
			break;
		case R.id.tv_back:
			// 返回
			backward();
			break;
		case R.id.iv_delete_phone:
			mEtPhone.setText("");
			break;
		case R.id.iv_code_delete:
			mEtCode.setText("");
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

	/**
	 * 登录
	 */
	public void Login() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();
		map.put("RegisterPhone", mEtPhone.getText().toString().trim());
		map.put("DeviceType", "0");
		map.put("DeviceToken", "0");
		map.put("Password", "0");
		map.put("UUID", getIMEI());
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
		maps.put("RegisterPhone", mEtPhone.getText().toString().trim());
		maps.put("DeviceType", "0");
		maps.put("DeviceToken", "0");
		maps.put("Password", "0");
		maps.put("UUID", getIMEI());
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);
		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/Login/PostPhone", maps, new LoginCallBack(),this);

	}

	class LoginCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parse(result);
				} else if (jsonresult.equals("5")) {
					showToast("用户不存在");
				} else if (jsonresult.equals("4")) {
					showToast("密码不正确");
				} else {
					showToast("请检查网络设置");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void parse(String str) {
		Gson gson = new Gson();
		LoginBean mLogBean = new LoginBean();
		mLogBean = gson.fromJson(str, LoginBean.class);

		putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "1");

		String userId = mLogBean.getData().getUserId().toString();
		putSharedPreferences(Constants.KEY_PREFERENCE_USER_ID, userId);

		// 推送
//		mPushAgent.setAlias(userId, ALIAS_TYPE.SINA_WEIBO);
//		mPushAgent.setExclusiveAlias(userId, ALIAS_TYPE.SINA_WEIBO);

		putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, mLogBean.getData().getRegisterPhone());
		putSharedPreferences(Constants.KEY_PREFERENCE_USER_INFO, str);

		ActivityLogin.instance.finish();
		finish();

	}

}
