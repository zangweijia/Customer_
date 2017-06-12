package com.bopinjia.customer.activity;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.SelectModeFragment;
import com.bopinjia.customer.fragment.SelectModeFragment.IOnSelectModeDismissListner;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ActivityFXSPersonalCenter extends BaseActivity implements IOnSelectModeDismissListner {

	private TextView mTiTleBack;
	private TextView mTiTleName;
	private TextView mTitleSave;

	private Bitmap mHeadPortraitBitmap;
	private String mMode;
	private TextView mode;
	/**
	 * 分销商级别
	 */
	private String mFXSLevel;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_fxsdata);
		setTitle();
		search();
		getDistributionInfo();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTitleSave = (TextView) mTiTle.findViewById(R.id.btn_edit);

		mTitleSave.setText("保存");
		mTiTleName.setText("会员资料");
		mTiTleBack.setOnClickListener(this);
		mTitleSave.setOnClickListener(this);

		findViewById(R.id.iv_head).setOnClickListener(this);
		findViewById(R.id.edt_mode).setOnClickListener(this);

		findViewById(R.id.iv_sj).setOnClickListener(this);

		mode = (TextView) findViewById(R.id.edt_mode);

		progressBar = (ProgressBar) findViewById(R.id.pb_update);

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
		case R.id.btn_edit:
			updata();
			break;
		case R.id.iv_head:
			// 更换头像
			SelectModeFragment fragment = new SelectModeFragment(getLoginPhone(), 0, Constants.IMAGE_TYPE_HEAD_PORTRAIT,
					this);
			fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
			fragment.show(getFragmentManager(), "dialog");
			break;

		case R.id.iv_sj:
			Intent i = new Intent();
			i.putExtra("fxslevel", mFXSLevel);
			i.putExtra("type", "3");
			// type = 3升级进入
			forward(ActivityFXDisLevel.class, i);

			break;
		default:
			break;
		}
	}

	/**
	 * 更新个人信息
	 */
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
		map.put("IDCard", "00");
		map.put("NickName", ((EditText) findViewById(R.id.edt_user_name)).getText().toString());
		map.put("Sex", sex);
		map.put("Userqm", "0");

		Map<String, String> maps = new HashMap<String, String>();
		maps.put("UserId", getLoginUserId());
		maps.put("RealName", "0");
		maps.put("IDCard", "00");
		maps.put("NickName", ((EditText) findViewById(R.id.edt_user_name)).getText().toString());
		maps.put("Sex", sex);
		maps.put("Userqm", "0");
		maps.put("Sign", NetUtils.getSign(map));
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/User/Update", maps, new updataCallBack(), this);

	}

	/**
	 * 更新个人信息回调
	 * 
	 * @author ZWJ
	 *
	 */
	class updataCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					Toast.makeText(ActivityFXSPersonalCenter.this, "  保存成功  ", Toast.LENGTH_SHORT).show();
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

	/**
	 * 检索回调
	 */
	class searchCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);

				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject data = jo.getJSONObject("Data");

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
		}

	}

	/**
	 * 获取分销商信息
	 */
	private void getDistributionInfo() {
		String s = getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GetGDSUserInfo?UserId=" + s + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getDistributionInfoCallBack(), this);
	}

	/**
	 * 获取分销商信息回调
	 */
	class getDistributionInfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");

					mFXSLevel = Data.getString("GDSType_Level");
					if (mFXSLevel.equals("0")) {
						((TextView) findViewById(R.id.tv_tiyan))
								.setTextColor(getResources().getColor(R.color.main_color));
						setPgBar(20);

						((ImageView) findViewById(R.id.iv_1)).setVisibility(View.VISIBLE);

					} else if (mFXSLevel.equals("1")) {
						((TextView) findViewById(R.id.tv_tongpai))
								.setTextColor(getResources().getColor(R.color.main_color));
						setPgBar(40);
						((ImageView) findViewById(R.id.iv_2)).setVisibility(View.VISIBLE);

					} else if (mFXSLevel.equals("2")) {
						((TextView) findViewById(R.id.tv_yinpai))
								.setTextColor(getResources().getColor(R.color.main_color));
						setPgBar(60);

						((ImageView) findViewById(R.id.iv_3)).setVisibility(View.VISIBLE);

					} else if (mFXSLevel.equals("3")) {
						((TextView) findViewById(R.id.tv_jinpai))
								.setTextColor(getResources().getColor(R.color.main_color));
						setPgBar(80);

						((ImageView) findViewById(R.id.iv_4)).setVisibility(View.VISIBLE);

					}

					// 会员头像
					ImageFromUrl(Data.getString("GDSType_ImgBig"), R.id.iv_typeImgBig);
					// 分销会员编号
					((TextView) findViewById(R.id.tv_fxs_number)).setText("会员号：" + Data.getString("MDGDSM_Number"));
					// 有效期
					((TextView) findViewById(R.id.tv_date)).setText("有效期至：" + Data.getString("MDGDSR_EndDate"));

					// 升级费用
					((TextView) findViewById(R.id.tv_sj_price))
							.setText("距离下一个等级只需" + Data.getString("MDGDSR_sjprice") + "元");

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void setPgBar(final int len) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				for (int i = 0; i <= len; i++) {

					try {
						Thread.sleep(100);
						Message msg = new Message();
						msg.arg1 = i;
						msg.what = 1;
						ActivityFXSPersonalCenter.this.handler.sendMessage(msg);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private Handler handler = new Handler() {// 实例化Handler类

		@Override
		public void handleMessage(Message msg) {// chǔ理得到消息

			progressBar.setProgress(msg.arg1);
			Thread.currentThread().interrupt();
		}
	};

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
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					((ImageView) findViewById(R.id.iv_head)).setImageBitmap(mHeadPortraitBitmap);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
