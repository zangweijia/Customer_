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

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityChangePhoneTwo extends BaseActivity {

	private EditText mEtPhone;
	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String mRandom;
	private TextView mTvDaoJiShi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_change_phone_two);

		init();
	}

	private void init() {
		mTvDaoJiShi = (TextView) findViewById(R.id.tv_getcode);

		findViewById(R.id.tv_getcode).setOnClickListener(this);
		findViewById(R.id.tv_next).setOnClickListener(this);
		findViewById(R.id.tv_back).setOnClickListener(this);
		mEtPhone = (EditText) findViewById(R.id.et_phone);
		mEtPhone.addTextChangedListener(mPhoneTextWatcher);
		mEtCode = (EditText) findViewById(R.id.et_code);
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
				findViewById(R.id.tv_next).setBackgroundResource(R.drawable.bg_press_);
			} else {
				findViewById(R.id.tv_next).setBackgroundResource(R.drawable.bg_password_next);
			}
		}
	};
	private EditText mEtCode;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_getcode:
			// 验证码未申请时
			if (StringUtils.isNull(mEtPhone.getText().toString().trim())) {
				showToast("请输入手机号！");
				return;
			} else {
				sendSms();
			}
			break;
		case R.id.tv_back:
			backward();
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
			ResetPassword();
			break;

		default:
			break;
		}
	}

	/**
	 * 更换手机号
	 * 
	 * @param
	 */
	private void ResetPassword() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("UserId", getLoginUserId());
		map.put("RegisterPhone", mEtPhone.getText().toString().trim());
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
		maps.put("RegisterPhone", mEtPhone.getText().toString().trim());
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/User/UpdateRegisterPhone", maps,
				new ResetPasswordCallback(),this);

	}

	/**
	 * 更换手机号回调
	 * 
	 * @author ZWJ
	 *
	 */
	class ResetPasswordCallback implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, mEtPhone.getText().toString().trim());
					forward(ActivityHome.class);
					mScreenManager.clearTempList();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

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

}
