package com.bopinjia.customer.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityAddressDetails extends BaseActivity {

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
	private String mode;
	private ToggleButton mTogBtn;
	private TextView mtitle;
	private TextView mDeleteAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_address_details);

		mtitle = (TextView) findViewById(R.id.tv_title);
		mDeleteAddress = (TextView) findViewById(R.id.tv_delete_address);
		mDeleteAddress.setOnClickListener(this);
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 保存
		findViewById(R.id.btn_save).setOnClickListener(this);
		mTogBtn = (ToggleButton) findViewById(R.id.mTogBtn);
		mDivisionData = readDivisions();

		getDivisions();
		setupSpinners();

		mode = getIntent().getStringExtra("mode");

		try {
			if (mode.equals("1")) {
				// 通过编辑地址进入
				mtitle.setText("修改通关信息");
				mDeleteAddress.setVisibility(View.VISIBLE);
				mAddressData = new JSONObject(getIntent().getStringExtra("AddressData"));
				display();
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tv_delete_address:
			final Dialog mDialog = new Dialog(this, R.style.CustomDialogTheme);
			View dialogView = LayoutInflater.from(this).inflate(R.layout.send_tel_dailog, null);
			// 设置自定义的dialog布局
			mDialog.setContentView(dialogView);
			// false表示点击对话框以外的区域对话框不消失，true则相反
			mDialog.setCanceledOnTouchOutside(false);
			// -----------------------------------

			mDialog.show();
			// 获取自定义dialog布局控件
			((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否删除该地址?");
			Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
			Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
			// 确定按钮点击事件
			confirmBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 删除
					try {
						deleteAddress(mAddressData.getString("Id"));
						mDialog.dismiss();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			// 取消按钮点击事件
			cancelBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			break;
		case R.id.btn_return:
			// 返回
			backward();
			finish();
			break;
		case R.id.btn_save:
			// 保存
			if (StringUtils.isNull(((EditText) findViewById(R.id.edt_receiver_name)).getText().toString())
					// || StringUtils.isNull(((EditText)
					// findViewById(R.id.et_real_name)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_phone)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_address)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.et_idcard)).getText().toString())) {
				showToast("请确认所有信息都已经填写！");
				break;
			}
			String receivername = ((EditText) findViewById(R.id.edt_receiver_name)).getText().toString();
			if (receivername.trim().length() == 1) {
				showToast("请输入完整的收货人姓名！");
				break;
			}
			// String realname = ((EditText)
			// findViewById(R.id.et_real_name)).getText().toString();
			// if (realname.trim().length() == 1) {
			// showToast("请输入完整的姓名！");
			// break;
			// }
			if (!IsPersonNumber(((EditText) findViewById(R.id.et_idcard)).getText().toString())) {
				showToast("请输入正确身份证格式！");
				break;
			}
			if (((EditText) findViewById(R.id.edt_phone)).getText().toString().length() != 11) {
				showToast("请正确输入11位手机号码！");
				break;
			}

			try {
				if ("1".equals(getIntent().getStringExtra("mode"))) {
					if (mTogBtn.isChecked()) {
						updataAddress(mAddressData.getString("Id"), "1");
					} else {
						updataAddress(mAddressData.getString("Id"), "0");
					}
				} else {
					if (mTogBtn.isChecked()) {
						// 新增地址并设为默认
						addAddress("1");
					} else {
						// 新增地址不设为默认
						addAddress("0");
					}
				}
			} catch (Exception e) {
				showSysErr(e);
			}
			break;
		default:
			break;
		}

	}

	/**
	 * 展示数据
	 */
	private void display() {
		try {

			// 收货人
			((EditText) findViewById(R.id.edt_receiver_name)).setText(mAddressData.getString("Consignee"));

			// 手机号
			((EditText) findViewById(R.id.edt_phone)).setText(mAddressData.getString("Mobile"));

			// 收货地址（省市区）
			mCurrProvince = mAddressData.getString("Province");
			mCurrCity = mAddressData.getString("City");
			mCurrCounty = mAddressData.getString("County");
			mProvinceSp.setSelection(mProvincesCode.indexOf(mCurrProvince));

			Map<String, List<String>> cities = StringUtils.getCities(mDivisionData, mCurrProvince);
			Map<String, List<String>> counties = StringUtils.getCounties(mDivisionData, mCurrProvince, mCurrCity);

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

			// 详细地址
			((EditText) findViewById(R.id.edt_address)).setText(mAddressData.getString("DetailAddress"));

			String idCard = mAddressData.getString("IDCard");
			// 身份证号
			((EditText) findViewById(R.id.et_idcard)).setText(idCard);

			String ss = mAddressData.getString("IsDefault");
			if (ss.equals("1")) {
				// 默认地址
				mTogBtn.setChecked(true);
			} else {
				mTogBtn.setChecked(false);
			}

			// ((EditText)
			// findViewById(R.id.et_real_name)).setText(mAddressData.getString("RealName"));
		} catch (Exception e) {
			showSysErr(e);
		}
	}

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

	/***
	 * 删除地址
	 * 
	 * @param addressId
	 */
	private void deleteAddress(String addressId) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("AddressId", addressId);
		maps.put("Key", Constants.WEBAPI_KEY);
		maps.put("Ts", Ts);
		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/Address/Delete", maps,
				new deleteAddressCallBack(),this);
	}

	/***
	 * 删除地址回调类
	 */
	class deleteAddressCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					showToast("地址已删除");
					backward();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 添加收货地址
	 */
	private void addAddress(String defult) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		String consigeneeUTF8 = null;
		String detailAddressUTF8 = null;
		String realNameUTF8 = null;
		try {
			consigeneeUTF8 = URLEncoder.encode(((EditText) findViewById(R.id.edt_receiver_name)).getText().toString(),
					"utf-8");
			detailAddressUTF8 = URLEncoder.encode(((EditText) findViewById(R.id.edt_address)).getText().toString(),
					"utf-8");
			realNameUTF8 = URLEncoder.encode(((EditText) findViewById(R.id.et_real_name)).getText().toString(),
					"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		map.put("UserID", getLoginUserId());
		map.put("Consignee", consigeneeUTF8);
		map.put("Mobile", ((EditText) findViewById(R.id.edt_phone)).getText().toString());

		map.put("Province", mCurrProvince);
		map.put("City", mCurrCity);
		map.put("County", mCurrCounty);

		map.put("IsDefault", defult);
		map.put("DetailAddress", detailAddressUTF8);
		map.put("RealName", "0");
		map.put("IDCard", ((EditText) findViewById(R.id.et_idcard)).getText().toString());

		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", MD5.getTimeStamp());
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
		maps.put("IsDefault", defult);
		maps.put("UserID", getLoginUserId());
		maps.put("Consignee", consigeneeUTF8);
		maps.put("Mobile", ((EditText) findViewById(R.id.edt_phone)).getText().toString());

		maps.put("Province", mCurrProvince);
		maps.put("City", mCurrCity);
		maps.put("County", mCurrCounty);

		maps.put("DetailAddress", detailAddressUTF8);
		maps.put("RealName", "0");
		maps.put("IDCard", ((EditText) findViewById(R.id.et_idcard)).getText().toString());

		maps.put("Sign", Sign);
		maps.put("Ts", MD5.getTimeStamp());

		XutilsHttp.getInstance().post(Constants.WEBAPI_BOPINWANG + "api/Address/BpwAddNew", maps,
				new AddressAddCallback(),this);

	}

	class AddressAddCallback implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					if (mode.equals("5")) {
						backward();
					} else {
						backward();
						finish();
						showToast("保存成功！");
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * 更新地址信息
	 */
	private void updataAddress(String addressId, String defult) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		String consigeneeUTF8 = null;
		String detailAddressUTF8 = null;
		String realNameUTF8 = null;
		try {
			consigeneeUTF8 = URLEncoder.encode(((EditText) findViewById(R.id.edt_receiver_name)).getText().toString(),
					"utf-8");
			detailAddressUTF8 = URLEncoder.encode(((EditText) findViewById(R.id.edt_address)).getText().toString(),
					"utf-8");
			realNameUTF8 = URLEncoder.encode(((EditText) findViewById(R.id.et_real_name)).getText().toString(),
					"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		map.put("AddressId", addressId);
		map.put("UserID", getLoginUserId());
		map.put("Consignee", consigeneeUTF8);
		map.put("Mobile", ((EditText) findViewById(R.id.edt_phone)).getText().toString());

		map.put("Province", mCurrProvince);
		map.put("City", mCurrCity);
		map.put("County", mCurrCounty);

		map.put("DetailAddress", detailAddressUTF8);
		map.put("RealName", "0");
		map.put("IDCard", ((EditText) findViewById(R.id.et_idcard)).getText().toString());

		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("IsDefault", defult);
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

		maps.put("IsDefault", defult);

		maps.put("AddressId", addressId);
		maps.put("UserID", getLoginUserId());
		maps.put("Consignee", consigeneeUTF8);
		maps.put("Mobile", ((EditText) findViewById(R.id.edt_phone)).getText().toString());

		maps.put("Province", mCurrProvince);
		maps.put("City", mCurrCity);
		maps.put("County", mCurrCounty);

		maps.put("DetailAddress", detailAddressUTF8);
		maps.put("RealName", "0");
		maps.put("IDCard", ((EditText) findViewById(R.id.et_idcard)).getText().toString());

		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_BOPINWANG + "api/Address/BpwUpdateNew", maps,
				new updateAddress(),this);

	}

	/**
	 * 更新地址回调
	 * 
	 * @author ZWJ
	 *
	 */
	class updateAddress implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					backward();
					finish();
					showToast("保存成功！");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 验证身份证号是否符合规则
	 * 
	 * @param text
	 *            身份证号
	 * @return
	 */
	public boolean IsPersonNumber(String text) {
		String regx = "[0-9]{17}x";
		String reg1 = "[0-9]{15}";
		String regex = "[0-9]{18}";
		return text.matches(regx) || text.matches(reg1) || text.matches(regex);
	}

}
