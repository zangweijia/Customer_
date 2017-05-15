package com.bopinjia.customer.activity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;


import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
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

public class ActivityChangePhoneOne extends BaseActivity {
	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String mRandom;
	private TextView mTvDaoJiShi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_change_phone_one);
		if (!StringUtils.isNull(getLoginPhone())) {
			String bindingPhone = getLoginPhone().substring(0, 4) + "****" + getLoginPhone().substring(8, 11);
			((TextView) findViewById(R.id.tv_now_phone)).setText("当前手机号码为 " + bindingPhone);
		} else {
			findViewById(R.id.tv_now_phone).setVisibility(View.GONE);
		}
		init();
	}

	private void init() {
		findViewById(R.id.tv_next).setOnClickListener(this);
		findViewById(R.id.tv_back).setOnClickListener(this);
		findViewById(R.id.tv_getcode).setOnClickListener(this);
		mTvDaoJiShi = (TextView) findViewById(R.id.tv_getcode);
		mEtCode = (EditText) findViewById(R.id.et_code);
		mEtCode.addTextChangedListener(mPhoneTextWatcher);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
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
			forward(ActivityChangePhoneTwo.class);

			break;
		case R.id.tv_back:
			backward();
			break;
		case R.id.tv_getcode:
			sendSms();
			break;
		default:
			break;
		}
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

	/**
	 * 发送验证短信
	 */
	private void sendSms() {
		// 生成一个6位随机数
		DecimalFormat df = new DecimalFormat("000000");
		mRandom = df.format(Math.random() * 100000);
		
		String message = MessageFormat.format(getString(R.string.msg_info_verify), mRandom);

		SendSMS sm = new SendSMS(this, new sendsmsSuccess());
		sm.sendSMS(message, getLoginPhone());

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
	 * 
	 * @author ZWJ
	 *
	 */
	class sendsmsSuccess implements MessageCallBack {

		@Override
		public void DisposalProblem() {
			// 短信发送成功之后倒计时弹出toast
			showToast(getString(R.string.msg_info_verify_sended));
		}

	}

}
