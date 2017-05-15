package com.bopinjia.customer.activity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.LoginBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.google.gson.Gson;

import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityBindLogin extends BaseActivity {

	private String uid,unionid;
	private String type;
	private TextView tv;
	private EditText et_phone;
	private EditText et_pas;
	private TextView tv_login;
	private TextView tv_forget_password;
	private String mType;
	private String psw;
	private ImageView mDeletePhone;
	private ImageView mYan;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_bind_login);
		initClick();
		init();
		findViewById(R.id.btn_return).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	private void init() {
		uid = getIntent().getStringExtra("uid");
		unionid= getIntent().getStringExtra("unionid");
		type = getIntent().getStringExtra("type");

		tv = (TextView) findViewById(R.id.tv);

		if (type.equals("xl")) {
			tv.setText("关联后，您的新浪账号与舶品家账号都可以登录");
			mType = "3";
		} else if (type.equals("qq")) {
			tv.setText("关联后，您的腾讯账号与舶品家账号都可以登录");
			mType = "2";
		} else if (type.equals("wx")) {
			tv.setText("关联后，您的微信账号与舶品家账号都可以登录");
			mType = "1";
		}
	}

	private boolean isHidden = true;

	private void initClick() {
		et_phone = (EditText) findViewById(R.id.et_phone);
		et_phone.addTextChangedListener(myTextWatcher);
		et_pas = (EditText) findViewById(R.id.et_pas);
		mDeletePhone = (ImageView) findViewById(R.id.iv_delete_phone);

		mYan = (ImageView) findViewById(R.id.iv_password_isshow);
		tv_login = (TextView) findViewById(R.id.tv_login);
		tv_forget_password = (TextView) findViewById(R.id.tv_forget_password);
		tv_forget_password.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				forward(ActivityForGetPass.class);
			}
		});

		tv_login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				BingLogin(mType, uid);
			}
		});
		mDeletePhone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				et_phone.setText("");
			}
		});

		mYan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mYan.setSelected(!mYan.isSelected());
				if (isHidden) {
					// 设置EditText文本为可见的
					et_pas.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					// 设置EditText文本为隐藏的
					et_pas.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				isHidden = !isHidden;
				et_pas.postInvalidate();
				// 切换后将EditText光标置于末尾
				CharSequence charSequence = et_pas.getText();
				if (charSequence instanceof Spannable) {
					Spannable spanText = (Spannable) charSequence;
					Selection.setSelection(spanText, charSequence.length());
				}

			}
		});

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
	 * 绑定登陆
	 * 
	 * @param type
	 * @param bindingaccount
	 */
	private void BingLogin(String type, String bindingaccount) {
		String Ts = MD5.getTimeStamp();
		String phone = et_phone.getText().toString().trim();
		psw = MD5.Md5(et_pas.getText().toString().trim());
		String uuid = getIMEI();

		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("RegisterPhone", phone);
		map.put("Password", psw);
		map.put("UserUnionID", unionid);
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

		String url = Constants.WEBAPI_ADDRESS + "api/UserBinding/PostQuicklogin_New?RegisterPhone=" + phone + "&Password="
				+ psw + "&UserType=" + type + "&UserBindingAccount=" + bindingaccount +"&UserUnionID="+unionid+ "&UUID=" + uuid + "&Sign="
				+ Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new BindLoginCallBack(),this);
	}

	/**
	 * 绑定登陆 回调
	 */
	class BindLoginCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					showToast("绑定成功");
					Gson gson = new Gson();
					LoginBean mLogBean = new LoginBean();
					mLogBean = gson.fromJson(result, LoginBean.class);
					putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "1");
					String userId = mLogBean.getData().getUserId().toString();
					putSharedPreferences(Constants.KEY_PREFERENCE_USER_ID, userId);
					putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, mLogBean.getData().getRegisterPhone());
					putSharedPreferences(Constants.KEY_PREFERENCE_PASSWORD, psw);
					putSharedPreferences(Constants.KEY_PREFERENCE_USER_INFO, result);
					ActivityLogin.instance.finish();
					ActivityOtherLogin.instance.finish();
					finish();

				} else if (jsonresult.equals("5")) {
					showToast("用户不存在");
				} else if (jsonresult.equals("4")) {
					showToast("密码不正确");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
