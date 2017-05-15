package com.bopinjia.customer.activity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.SelectModeFragment;
import com.bopinjia.customer.fragment.SelectModeFragment.IOnSelectModeDismissListner;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMyself extends BaseActivity implements IOnSelectModeDismissListner {

	private Bitmap mHeadPortraitBitmap;
	private String mMode;
	private TextView mode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_myself);

		findViewById(R.id.btn_return).setOnClickListener(this);
		findViewById(R.id.btn_save).setOnClickListener(this);
		findViewById(R.id.iv_change_head).setOnClickListener(this);
		findViewById(R.id.iv_head).setOnClickListener(this);
		findViewById(R.id.edt_mode).setOnClickListener(this);

		mode = (TextView) findViewById(R.id.edt_mode);

		search();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.edt_mode:
			getSex();
			break;
		case R.id.btn_return:
			finish();
			break;
		case R.id.btn_save:
			updata();
			break;
		case R.id.iv_head:
		case R.id.iv_change_head:
			// 更换头像
			SelectModeFragment fragment = new SelectModeFragment(getLoginPhone(), 0, Constants.IMAGE_TYPE_HEAD_PORTRAIT,
					this);
			fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
			fragment.show(getFragmentManager(), "dialog");
			break;
		default:
			break;
		}
	}

	public void updata() {
		String sex = null;
		String mm = mode.getText().toString();
		if (mm.equals("") || mm == null) {
			sex = mMode;
		} else {
			if (mm.equals("男")) {
				sex = "1";
			} else if (mm.equals("女")) {
				sex = "2";
			}
		}

		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("RealName", "0");
		map.put("IDCard", "130636199911119999");
		map.put("NickName", ((EditText) findViewById(R.id.edt_user_name)).getText().toString());
		map.put("Sex", sex);
		map.put("Userqm", ((EditText) findViewById(R.id.edt_introduce_oneself)).getText().toString());

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("UserId", getLoginUserId());
		maps.put("RealName", "0");
		maps.put("IDCard", "130636199911119999");
		maps.put("NickName", ((EditText) findViewById(R.id.edt_user_name)).getText().toString());
		maps.put("Sex", sex);
		maps.put("Userqm", ((EditText) findViewById(R.id.edt_introduce_oneself)).getText().toString());
		maps.put("Sign", NetUtils.getSign(map));
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/User/Update", maps, new updataCallBack(), this);
	}

	class updataCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					Toast.makeText(ActivityMyself.this, "  保存成功  ", 0).show();
					finish();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void getSex() {
		String[] items = new String[] { "男", "女" };

		new AlertDialog.Builder(this).setTitle("性别(保存后将不能更改)").setSingleChoiceItems(items, -1, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					mode.setText("男");
					mMode = "1";
				} else if (which == 1) {
					mode.setText("女");
					mMode = "2";
				}
				dialog.dismiss();
			}
		}).show();
	}

	/**
	 * 检索处理
	 */
	private void search() {
		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		String url = Constants.WEBAPI_ADDRESS + "api/User/GetUserList?UserId=" + getLoginUserId() + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new searchCallBack(), this);

	}

	class searchCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			parse(1, result);
		}

	}

	/**
	 * 解析返回数据
	 * 
	 * @param result
	 */
	private void parse(int id, String result) {

		switch (id) {
		case 1:
			try {
				JSONObject jo = new JSONObject(result);

				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject data = jo.getJSONObject("Data");
					((EditText) findViewById(R.id.edt_introduce_oneself)).setText(data.getString("Userqm").trim());
					((EditText) findViewById(R.id.edt_name)).setText(data.getString("RealName").trim());
					((EditText) findViewById(R.id.edt_idcard)).setText(data.getString("IDCard").trim());
					((EditText) findViewById(R.id.edt_user_name)).setText(data.getString("NickName").trim());
					EditText et = (EditText) findViewById(R.id.edt_user_name);
					et.setSelection(data.getString("NickName").trim().length());

					int i = Integer.parseInt(data.getString("Sex"));
					if (i == 0) {
						mode.setText("请选择性别");
					} else if (i == 1) {
						mode.setText("男");
						mode.setEnabled(false);
					} else if (i == 2) {
						mode.setText("女");
						mode.setEnabled(false);
					}

					ImageFromUrl(data.getString("HeadPortrait"), R.id.iv_head);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		case 2:
			// 头像返回数据
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					((ImageView) findViewById(R.id.iv_head)).setImageBitmap(mHeadPortraitBitmap);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}

	}

	@Override
	public void onSelectModeDismiss(Bitmap bitmap, int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelectModeDismissGetString(Bitmap bitmaps, String bitmap, int code) {
		mHeadPortraitBitmap = bitmaps;
		setHeard(bitmap);
	}

	/**
	 * 把头像上传到服务器
	 * 
	 * @param str
	 */
	private void setHeard(String str) {

		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Upload/UploadPortrait?UserId=" + getLoginUserId() + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		Map<String, File> file = new HashMap<String, File>();
		file.put("Filedata1", new File(str));
		XutilsHttp.getInstance().upLoadFile(url, null, file, new setHeardCallBack(), this);

	}

	class setHeardCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			parse(2, result);

		}

	}

}
