package com.bopinjia.customer.activity;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.SendSMS;
import com.bopinjia.customer.net.SendSMS.MessageCallBack;
import com.bopinjia.customer.util.StringUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

public class ActivityForGetPass extends BaseActivity {

	@ViewInject(R.id.tv_back)
	private TextView mBack;

	@ViewInject(R.id.et_phone)
	private EditText mEtPhone;

	/**
	 * 验证码
	 */
	@ViewInject(R.id.et_verification_code)
	private EditText mEtCode;

	@ViewInject(R.id.tv_next)
	private TextView mNext;

	/**
	 * 获取验证码
	 */
	@ViewInject(R.id.tv_getcode)
	private TextView mGetCode;

	@ViewInject(R.id.iv_delete_phone)
	private ImageView mDeletePhone;

	private int interval = Constants.MAX_INTERVAL_FOR_SECURITY;
	private String mRandom;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_forget_password);
		x.view().inject(this);
		init();
	}

	private void init() {
		mBack.setOnClickListener(this);
		mDeletePhone.setOnClickListener(this);
		mGetCode.setOnClickListener(this);
		mNext.setOnClickListener(this);
		mEtPhone.addTextChangedListener(myPhoneTextWatcher);
		mEtCode.addTextChangedListener(mCodeWatcher);

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
			Intent i = new Intent();
			String ss = mEtPhone.getText().toString().trim();
			i.putExtra("phone", ss);
			forward(ActivitySetPassword.class, i);
			finish();

			break;
		case R.id.tv_back:
			backward();
			break;
		case R.id.iv_delete_phone:
			mEtPhone.setText("");
			break;
		case R.id.tv_getcode:
			// 验证码未申请时
			if (StringUtils.isNull(mEtPhone.getText().toString().trim())) {
				showToast("请输入手机号！");
				return;
			} else {
				sendSms();
				mEtCode.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mEtCode, InputMethodManager.HIDE_NOT_ALWAYS);

			}
			break;
		default:
			break;
		}

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
	 * 发送验证短信
	 */
	private void sendSms() {
		// 生成一个6位随机数
		DecimalFormat df = new DecimalFormat("000000");
		mRandom = df.format(Math.random() * 100000);
		String message = MessageFormat.format(getString(R.string.msg_info_verify), mRandom);

		SendSMS sm = new SendSMS(this, new sendsmsSuccess());
		sm.sendSMS(message, mEtPhone.getText().toString());

		final Handler handler = new Handler() {
			@SuppressLint("HandlerLeak")
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					mGetCode.setEnabled(false);
					interval--;
					mGetCode.setText(interval + "");
					break;
				case 2:
					mGetCode.setEnabled(true);
					mGetCode.setText("重新发送");
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
	 * 监听mEtPhone 如果有输入显示，否则隐藏
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
	/**
	 * 监听mEtCode 如果有输入显示，否则隐藏
	 */
	TextWatcher mCodeWatcher = new TextWatcher() {
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
				mNext.setBackgroundResource(R.drawable.bg_press_);
			} else {
				mNext.setBackgroundResource(R.drawable.bg_password_next);
			}
		}
	};
}
