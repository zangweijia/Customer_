package com.bopinjia.customer.activity;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.StringUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivitySetPassword extends BaseActivity {

	@ViewInject(R.id.tv_back)
	private TextView mBack;

	@ViewInject(R.id.et_password)
	private EditText mEtPassword;

	@ViewInject(R.id.iv_yan)
	private ImageView mYan;

	@ViewInject(R.id.tv_next)
	private TextView mNext;

	private boolean isHidden = true;
	private String mD5Pass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_set_password);
		x.view().inject(this);
		init();
	}

	private void init() {
		mEtPassword.addTextChangedListener(myPhoneTextWatcher);
		mYan.setOnClickListener(this);
		mBack.setOnClickListener(this);
		mNext.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.tv_back:
			backward();
			break;
		case R.id.tv_next:
			// 下一步
			String pass = mEtPassword.getText().toString().trim();
			// 密码未填写
			if (StringUtils.isNull(pass)) {
				showToast(MessageFormat.format(getString(R.string.msg_err_empty), "密码"));
				return;
			} else {
				// 特殊字符校验
				if (StringUtils.validPass(pass)) {
					showToast(MessageFormat.format(getString(R.string.msg_err_invalid), "密码"));
					return;
				}
			}
			if (pass.length() < 6) {
				showToast("请输入6为及以上密码");
				return;
			}
			mD5Pass = MD5.Md5(pass);
			ResetCustomerPassword();
			break;
		case R.id.iv_yan:
			mYan.setSelected(!mYan.isSelected());
			if (isHidden) {
				// 设置EditText文本为可见的
				mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			} else {
				// 设置EditText文本为隐藏的
				mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			isHidden = !isHidden;
			mEtPassword.postInvalidate();
			// 切换后将EditText光标置于末尾
			CharSequence charSequence = mEtPassword.getText();
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
	 * 监听mEtCode 如果有输入显示，否则隐藏
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

		@SuppressLint("NewApi")
		@Override
		public void afterTextChanged(Editable edit) {
			if (s.length() > 0) {
				mYan.setVisibility(View.VISIBLE);
				mNext.setBackgroundResource(R.drawable.bg_press_);
			} else {
				mNext.setBackgroundResource(R.drawable.bg_password_next);
				mYan.setVisibility(View.GONE);
			}
		}
	};

	private void ResetCustomerPassword() {
		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();

		map.put("RegisterPhone", getIntent().getStringExtra("phone"));
		map.put("Password", mD5Pass);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("RegisterPhone", getIntent().getStringExtra("phone"));
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
					AlertDialog m = new AlertDialog.Builder(ActivitySetPassword.this).create();
					m.requestWindowFeature(Window.FEATURE_NO_TITLE);
					m.getWindow().setLayout(200, 200);
					m.setView(view, 0, 0, 0, 0);
					m.show();
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								Thread.sleep(2000);
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

}
