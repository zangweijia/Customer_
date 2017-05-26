package com.bopinjia.customer.activity;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.SelectModeFragment;
import com.bopinjia.customer.fragment.SelectModeFragment.IOnSelectModeDismissListner;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityFXSStoreInfo extends BaseActivity implements IOnSelectModeDismissListner {

	private TextView mTiTleBack;
	private TextView mTiTleName;
	private TextView mTitleSave;
	private EditText mEdtIntroducr;
	private TextView mNumber;

	private EditText mEtAddress;
	private EditText mPhone;
	private EditText mShopName;
	private ImageView mHead;
	private ImageView mChangeHead;

	/** 省 */
	private List<String> mProvincesCode;
	private List<String> mProvincesName;

	/** 市 */
	private List<String> mCitiesCode;
	private List<String> mCitiesName;
	private ArrayAdapter<String> mCityAdapter;

	/** 区 */
	private List<String> mCountiesCode;
	private List<String> mCountiesName;
	private ArrayAdapter<String> mCountyAdapter;

	private String mCurrProvince = "1";
	private String mCurrCity = "2";
	private String mCurrCounty = "3";

	private Spinner mProvinceSp;
	private Spinner mCitySp;
	private Spinner mCountySp;
	private JSONObject mDivisionData;

	private JSONObject mAddressData;

	private ArrayAdapter<String> mProvincesadapter;
	private EditText mName;
	private String shopLogo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_fxsstore_info);
		setTitle();

		mDivisionData = readDivisions();
		getDivisions();
		setupSpinners();
		init();
		getDistributionInfo();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTitleSave = (TextView) mTiTle.findViewById(R.id.btn_edit);
		mTitleSave.setText("保存");
		mTiTleName.setText("店铺资料");
		mTiTleBack.setOnClickListener(this);
		mTitleSave.setOnClickListener(this);
	}

	private void init() {
		mNumber = (TextView) findViewById(R.id.tv_number);
		// 店铺介绍
		mEdtIntroducr = (EditText) findViewById(R.id.edt_introduce);
		mEdtIntroducr.addTextChangedListener(myPhoneTextWatcher);

		mName = (EditText) findViewById(R.id.edt_name);

		mShopName = (EditText) findViewById(R.id.edt_shop_name);
		mPhone = (EditText) findViewById(R.id.edt_phone);
		mEtAddress = (EditText) findViewById(R.id.edt_address);

		mHead = (ImageView) findViewById(R.id.iv_head);
		mHead.setOnClickListener(this);
		mChangeHead = (ImageView) findViewById(R.id.iv_change_head);
		mChangeHead.setOnClickListener(this);
		findViewById(R.id.iv_clean).setOnClickListener(this);
		findViewById(R.id.ll_ewm).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			forward(ActivityFXGL.class);
			finish();
			break;
		case R.id.btn_edit:
			// 保存
			if (StringUtils.isNull(mShopName.getText().toString())
					|| StringUtils.isNull(mEdtIntroducr.getText().toString())
					|| StringUtils.isNull(mPhone.getText().toString())
					|| StringUtils.isNull(mEtAddress.getText().toString())
					|| StringUtils.isNull(mName.getText().toString())) {
				showToast("请确认所有信息都已经填写！");
				break;
			}
			if (mName.getText().toString().length() < 2) {
				showToast("亲，请输不少于两个字的店主姓名！");
				break;
			}
			if (mEdtIntroducr.getText().toString().length() < 8) {
				showToast("亲，店铺介绍最少十个字！");
				break;
			}
			if (mPhone.getText().toString().length() != 11) {
				showToast("亲，请输入11位手机号码！");
				break;
			}
			Save();
			break;
		case R.id.iv_change_head:
		case R.id.iv_head:
			// 更改头像
			// 更换头像
			SelectModeFragment fragment = new SelectModeFragment(getLoginPhone(), 0, Constants.IMAGE_TYPE_HEAD_PORTRAIT,
					this);
			fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
			fragment.show(getFragmentManager(), "dialog");
			break;

		case R.id.ll_ewm:
			// 我的二维码
			forward(ActivityFXQRcode.class);
			break;
		case R.id.iv_clean:
			// 删除店铺说明
			mEdtIntroducr.setText("");
			break;
		default:
			break;
		}

	}

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

			if (s.length() <= 30) {
				mNumber.setText(s.length() + "/30");
			} else {
				// showToast("亲最多输入30个字");
				mEdtIntroducr.setText(mEdtIntroducr.getText().subSequence(0, 30));
				mEdtIntroducr.setSelection(mEdtIntroducr.length());
			}
		}
	};

	private void setupSpinners() {
		mProvinceSp = (Spinner) findViewById(R.id.edt_province);
		mProvincesadapter = new ArrayAdapter<String>(this, R.layout.spinner_text, mProvincesName);
		mProvincesadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mProvinceSp.setAdapter(mProvincesadapter);
		mProvinceSp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mCurrProvince = mProvincesCode.get(position);

				try {

					Map<String, List<String>> cities = StringUtils.getCities(mDivisionData, mCurrProvince);

					mCitiesCode = cities.get("code");
					mCitiesName = cities.get("name");
					mCityAdapter.clear();
					mCityAdapter.addAll(mCitiesName);

					mCurrCity = mCitiesCode.get(0);
					Map<String, List<String>> counties = StringUtils.getCounties(mDivisionData, mCurrProvince,
							mCurrCity);
					mCountiesCode = counties.get("code");
					mCountiesName = counties.get("name");

					mCountyAdapter.clear();
					mCountyAdapter.addAll(mCountiesName);
				} catch (Exception e) {
					showSysErr(e);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mCitySp = (Spinner) findViewById(R.id.edt_city);
		mCityAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, mCitiesName);
		mCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCitySp.setAdapter(mCityAdapter);
		mCitySp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mCurrCity = mCitiesCode.get(position);

				try {
					Map<String, List<String>> counties = StringUtils.getCounties(mDivisionData, mCurrProvince,
							mCurrCity);
					mCountiesCode = counties.get("code");
					mCountiesName = counties.get("name");

					mCountyAdapter.clear();
					mCountyAdapter.addAll(mCountiesName);
				} catch (Exception e) {
					showSysErr(e);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mCountySp = (Spinner) findViewById(R.id.edt_district);
		mCountyAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text, mCountiesName);
		mCountyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCountySp.setAdapter(mCountyAdapter);
		mCountySp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mCurrCounty = mCountiesCode.get(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}

	/**
	 * 取得行政区划
	 */
	private void getDivisions() {

		try {

			Map<String, List<String>> provinces = StringUtils.getProvinces(mDivisionData);
			Map<String, List<String>> cities = StringUtils.getCities(mDivisionData, mCurrProvince);
			Map<String, List<String>> counties = StringUtils.getCounties(mDivisionData, mCurrProvince, mCurrCity);

			mProvincesCode = provinces.get("code");
			mProvincesName = provinces.get("name");

			mCitiesCode = cities.get("code");
			mCitiesName = cities.get("name");

			mCountiesCode = counties.get("code");
			mCountiesName = counties.get("name");

		} catch (Exception e) {
			showSysErr(e);
		}
	}

	private JSONObject readDivisions() {
		InputStreamReader reader = null;
		try {

			// 创建字符输入流
			reader = new InputStreamReader(getAssets().open(Constants.FILE_PATH_DIVISIONS));

			char[] cbuf = new char[1024];

			int hasRead = 0;

			StringBuffer buffer = new StringBuffer();

			while ((hasRead = reader.read(cbuf)) > 0) {
				buffer.append(new String(cbuf, 0, hasRead));
			}

			return new JSONObject(buffer.toString());
		} catch (Exception e) {
			showSysErr(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e2) {
				showSysErr(e2);
			}
		}

		return null;
	}

	private void Save() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		String name = mShopName.getText().toString().trim();
		String Contact = mEdtIntroducr.getText().toString().trim();
		String phone = mPhone.getText().toString().trim();
		String address = mEtAddress.getText().toString().trim();

		String Aname = mName.getText().toString().trim();

		map.put("MDGDSM_UserId", getLoginUserId());
		map.put("MDGDSM_ShopName", name);
		map.put("MDGDSM_ShopContact", Aname);
		map.put("MDGDSM_ShopMobile", phone);
		map.put("MDGDSM_ShopLogo", shopLogo);
		map.put("MDGDSM_Province", mCurrProvince);
		map.put("MDGDSM_City", mCurrCity);

		map.put("MDGDSM_County", mCurrCounty);

		map.put("MDGDSM_Address", address);
		map.put("MDGDSM_PrimaryBusiness", Contact);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/GDSUser/Update_Android");

		params.addBodyParameter("MDGDSM_UserId", getLoginUserId());
		params.addBodyParameter("MDGDSM_ShopName", name);
		params.addBodyParameter("MDGDSM_ShopContact", Aname);
		params.addBodyParameter("MDGDSM_ShopMobile", phone);
		params.addBodyParameter("MDGDSM_ShopLogo", shopLogo);
		params.addBodyParameter("MDGDSM_Province", mCurrProvince);
		params.addBodyParameter("MDGDSM_City", mCurrCity);

		params.addBodyParameter("MDGDSM_County", mCurrCounty);

		params.addBodyParameter("MDGDSM_Address", address);
		params.addBodyParameter("MDGDSM_PrimaryBusiness", Contact);

		params.addBodyParameter("Sign", Sign);
		params.addBodyParameter("Ts", Ts);
		params.setAsJsonContent(true);

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {

						showToast("保存成功");
						forward(ActivityFXGL.class);
						finish();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				showToast("申请失败~");
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

	private Bitmap mHeadPortraitBitmap;
	private String mHeadPortraitStr;

	@Override
	public void onSelectModeDismiss(Bitmap bitmap, int code) {

	}

	@Override
	public void onSelectModeDismissGetString(Bitmap bitmaps, String bitmap, int code) {
		mHeadPortraitBitmap = bitmaps;
		mHeadPortraitStr = bitmap;
		// ((ImageView)
		// findViewById(R.id.iv_head)).setImageBitmap(mHeadPortraitBitmap);
		setHeard(mHeadPortraitStr);
	}

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

	class getDistributionInfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");
					// 分销商绑定的店铺
					String MDGDSR_MDUserID = Data.getString("MDGDSR_MDUserID");
					shopLogo = Data.getString("MDGDSM_ShopLogo");
					ImageFromUrl(shopLogo, R.id.iv_head);

					mEdtIntroducr.setText(Data.getString("MDGDSM_PrimaryBusiness"));
					mEdtIntroducr.setSelection(Data.getString("MDGDSM_PrimaryBusiness").length());
					mName.setText(Data.getString("MDGDSM_ShopContact"));
					mName.setSelection(Data.getString("MDGDSM_ShopContact").length());

					mShopName.setText(Data.getString("MDGDSM_ShopName"));
					mShopName.setSelection(Data.getString("MDGDSM_ShopName").length());

					mPhone.setText(Data.getString("MDGDSM_ShopMobile"));
					mEtAddress.setText(Data.getString("MDGDSM_Address"));

					mCurrProvince = Data.getString("MDGDSM_Province");
					mCurrCity = Data.getString("MDGDSM_City");
					mCurrCounty = Data.getString("MDGDSM_County");
					mProvinceSp.setSelection(mProvincesCode.indexOf(mCurrProvince));

					Map<String, List<String>> cities = StringUtils.getCities(mDivisionData, mCurrProvince);
					Map<String, List<String>> counties = StringUtils.getCounties(mDivisionData, mCurrProvince,
							mCurrCity);

					mCitiesCode = cities.get("code");
					mCitiesName = cities.get("name");

					mCityAdapter.clear();
					mCityAdapter.addAll(mCitiesName);

					mCitySp.setSelection(mCitiesCode.indexOf(mCurrCity));
					mCountiesCode = counties.get("code");
					mCountiesName = counties.get("name");

					mCountyAdapter.clear();

					mCountyAdapter.addAll(mCountiesName);

					mCountySp.setSelection(mCountiesCode.indexOf(mCurrCounty));

				}

			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void setHeard(String str) {

		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();
		map.put("MDGDSM_UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/Update_AndroidPic?MDGDSM_UserId=" + getLoginUserId()
				+ "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;
		Map<String, File> file = new HashMap<String, File>();

		file.put("Filedata", new File(str));
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
				} else {
					showToast("头像上传失败");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			forward(ActivityFXGL.class);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
