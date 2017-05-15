package com.bopinjia.customer.activity;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ActivityBinding extends BaseActivity {

	private TextView mWXBind;
	private TextView mQQBind;
	private TextView mXLBind;
	private TextView mWXshow;
	private TextView mQQshow;
	private TextView mXLshow;
	private Dialog mDialog;
	private View dialogView;

	private int mLoginType;
	public static final int LOGIN_TYPE_XL = 3;
	public static final int LOGIN_TYPE_WX = 1;
	public static final int LOGIN_TYPE_QQ = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_binding);
		mShareAPI = UMShareAPI.get(ActivityBinding.this);
		init();
	}

	private void init() {
		View headView = (View) findViewById(R.id.include_title);
		TextView mtitle = (TextView) headView.findViewById(R.id.txt_page_title);
		mtitle.setText("账号绑定");

		((TextView) headView.findViewById(R.id.btn_return)).setOnClickListener(this);

		mWXBind = (TextView) findViewById(R.id.wx_binding);
		mQQBind = (TextView) findViewById(R.id.qq_binding);
		mXLBind = (TextView) findViewById(R.id.xl_binding);

		mWXBind.setOnClickListener(this);
		mQQBind.setOnClickListener(this);
		mXLBind.setOnClickListener(this);

		mWXshow = (TextView) findViewById(R.id.wx_show_binding);
		mQQshow = (TextView) findViewById(R.id.qq_show_binding);
		mXLshow = (TextView) findViewById(R.id.xl_show_binding);
		initData();
	}

	private void initData() {

		mDialog = new Dialog(ActivityBinding.this, R.style.CustomDialogTheme);
		dialogView = LayoutInflater.from(ActivityBinding.this).inflate(R.layout.send_tel_dailog, null);
		// 设置自定义的dialog布局
		mDialog.setContentView(dialogView);
		// false表示点击对话框以外的区域对话框不消失，true则相反
		mDialog.setCanceledOnTouchOutside(false);
		// -----------------------------------
		Binding("1");
		Binding("2");
		Binding("3");
	}

	private UMShareAPI mShareAPI;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.wx_binding:
			if ((mWXBind.getText().toString()).equals("绑定")) {
				mShareAPI.getPlatformInfo(ActivityBinding.this, SHARE_MEDIA.WEIXIN, umListener);
				mLoginType = LOGIN_TYPE_WX;

			} else if ((mWXBind.getText().toString()).equals("解绑")) {
				mDialog.show();
				// 获取自定义dialog布局控件
				((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("您确定要解除绑定微信账号?");
				Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
				Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
				// 确定按钮点击事件
				confirmBt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						RelieveBinding("1");
						mDialog.dismiss();

					}
				});
				// 取消按钮点击事件
				cancelBt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						mDialog.dismiss();
					}
				});

			}
			// Share();

			break;
		case R.id.qq_binding:
			if ((mQQBind.getText().toString()).equals("绑定")) {
				mShareAPI.getPlatformInfo(ActivityBinding.this, SHARE_MEDIA.QQ, umListener);
				mLoginType = LOGIN_TYPE_QQ;

			} else if ((mQQBind.getText().toString()).equals("解绑")) {
				mDialog.show();
				// 获取自定义dialog布局控件
				((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("您确定要解除绑定QQ账号?");
				Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
				Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
				// 确定按钮点击事件
				confirmBt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						RelieveBinding("2");
						mDialog.dismiss();

					}
				});
				// 取消按钮点击事件
				cancelBt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						mDialog.dismiss();
					}
				});

			}
			break;

		case R.id.xl_binding:
			if ((mXLBind.getText().toString()).equals("绑定")) {
				mShareAPI.getPlatformInfo(ActivityBinding.this, SHARE_MEDIA.SINA, umListener);
				mLoginType = LOGIN_TYPE_XL;
			} else if ((mXLBind.getText().toString()).equals("解绑")) {
				mDialog.show();
				// 获取自定义dialog布局控件
				((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("您确定要解除绑定新浪微博账号?");
				Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
				Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
				// 确定按钮点击事件
				confirmBt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						RelieveBinding("3");
						mDialog.dismiss();

					}
				});
				// 取消按钮点击事件
				cancelBt.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						mDialog.dismiss();
					}
				});

			}
			break;
		case R.id.btn_return:
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 获取信息
	 */
	private UMAuthListener umListener = new UMAuthListener() {

		@Override
		public void onCancel(SHARE_MEDIA arg0, int arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onComplete(SHARE_MEDIA arg0, int arg1, Map<String, String> data) {
			// BindAccount
			if (mLoginType == LOGIN_TYPE_XL) {
				BindAccount("3", data.get("uid"));
			} else if (mLoginType == LOGIN_TYPE_WX) {
				BindAccount("1", data.get("unionid"));
			} else if (mLoginType == LOGIN_TYPE_QQ) {
				BindAccount("2", data.get("uid"));
			}

		}

		@Override
		public void onError(SHARE_MEDIA arg0, int arg1, Throwable arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStart(SHARE_MEDIA arg0) {
			// TODO Auto-generated method stub
			
		}

	};

	/**
	 * 查询绑定
	 * 
	 * @param type
	 */
	private void Binding(final String type) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("UserType", type);

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

		String url = Constants.WEBAPI_ADDRESS + "api/UserBinding/ExitQuicklogin?UserId=" + getLoginUserId()
				+ "&UserType=" + type + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new BindingCallBack(), 0, type, this);
	}

	/**
	 * 查询绑定回调
	 */
	class BindingCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				String type = str;
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// 已绑定
					if (type.equals("1")) {
						mWXBind.setText("解绑");
						mWXshow.setText("已绑定");
					} else if (type.equals("2")) {
						mQQBind.setText("解绑");
						mQQshow.setText("已绑定");
					} else if (type.equals("3")) {
						mXLBind.setText("解绑");
						mXLshow.setText("已绑定");
					}
				} else if (jsonresult.equals("0")) {
					// 未绑定
					if (type.equals("1")) {
						mWXBind.setText("绑定");
						mWXshow.setText("未绑定");
					} else if (type.equals("2")) {
						mQQBind.setText("绑定");
						mQQshow.setText("未绑定");
					} else if (type.equals("3")) {
						mXLBind.setText("绑定");
						mXLshow.setText("未绑定");
					}
				} else {
					showErr();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * 解除绑定 1、 微信 2、QQ 3、 新浪
	 * 
	 * @param type
	 */
	private void RelieveBinding(final String type) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("UserType", type);

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

		String url = Constants.WEBAPI_ADDRESS + "api/UserBinding/DeleteQuicklogin?UserId=" + getLoginUserId()
				+ "&UserType=" + type + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new RelieveBindingCallback(), 0, type, this);

	}

	/**
	 * 解除绑定 回调
	 */
	class RelieveBindingCallback implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				String type = str;
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// 已解绑
					if (type.equals("1")) {
						mWXBind.setText("绑定");
						mWXshow.setText("未绑定");
						showToast("微信账号已解除绑定");
					} else if (type.equals("2")) {
						mQQBind.setText("绑定");
						mQQshow.setText("未绑定");
						showToast("微信账号已解除绑定");
					} else if (type.equals("3")) {
						mXLBind.setText("绑定");
						mXLshow.setText("未绑定");
						showToast("微信账号已解除绑定");
					}
				} else {
					showErr();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 绑定号码
	 * 
	 * @param type
	 */
	private void BindAccount(final String type, String account) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("UserType", type);
		map.put("UserBindingAccount", account);

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

		String url = Constants.WEBAPI_ADDRESS + "api/UserBinding/QuickloginUser?UserType=" + type + "&UserId="
				+ getLoginUserId() + "&UserBindingAccount=" + account + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new BindAccountCallBack(), 0, type, this);

	}

	/**
	 * 绑定号码回调
	 */
	class BindAccountCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				String type = str;
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// 已绑定
					if (type.equals("1")) {
						mWXBind.setText("解绑");
						mWXshow.setText("已绑定");
						showToast("微信账号已绑定");
					} else if (type.equals("2")) {
						mQQBind.setText("解绑");
						mQQshow.setText("已绑定");
						showToast("QQ账号已绑定");
					} else if (type.equals("3")) {
						mXLBind.setText("解绑");
						mXLshow.setText("已绑定");
						showToast("新浪账号已绑定");
					}
				} else {
					showErr();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

	}
}
