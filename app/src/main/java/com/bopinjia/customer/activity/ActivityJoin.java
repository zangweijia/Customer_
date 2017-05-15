package com.bopinjia.customer.activity;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ActivityJoin extends BaseActivity {
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

	private String mMode;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_join);

		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 提交申請
		findViewById(R.id.btn_submit).setOnClickListener(this);

		mDivisionData = readDivisions();

		getDivisions();
		setupSpinners();
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

	private void setupSpinners() {
		mProvinceSp = (Spinner) findViewById(R.id.edt_store_province);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_text, mProvincesName);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mProvinceSp.setAdapter(adapter);
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

		mCitySp = (Spinner) findViewById(R.id.edt_store_city);
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

		mCountySp = (Spinner) findViewById(R.id.edt_store_district);
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

		Spinner mModeSp = (Spinner) findViewById(R.id.edt_mode);
		ArrayAdapter<String> mModeAdapter = new ArrayAdapter<String>(this, R.layout.spinner_text_left_align,
				new String[] { "批发", "零售" });
		final String[] modeCode = new String[] { "1", "2" };
		mModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mModeSp.setAdapter(mModeAdapter);
		mModeSp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mMode = modeCode[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}


	private JSONObject getData() {
		JSONObject addressData = new JSONObject();

		try {
			addressData.put("Province", mCurrProvince);
			addressData.put("City", mCurrCity);
			addressData.put("County", mCurrCounty);

			addressData.put("ManagementMode", mMode);
			addressData.put("ShopCount", ((EditText) findViewById(R.id.edt_shop_count)).getText().toString());
			addressData.put("ContactPerson", ((EditText) findViewById(R.id.edt_contact)).getText().toString());
			addressData.put("Mobile", ((EditText) findViewById(R.id.edt_mobile)).getText().toString());
			addressData.put("AnnualTurnover", ((EditText) findViewById(R.id.edt_turnover)).getText().toString());
			addressData.put("PrimaryBusiness", ((EditText) findViewById(R.id.edt_business)).getText().toString());
			addressData.put("DetailAddress", ((EditText) findViewById(R.id.edt_address)).getText().toString());

		} catch (Exception e) {
			showSysErr(e);
		}

		return addressData;
	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		// 接口参数
		switch (viewId) {
		case R.id.btn_return:
			// 返回
			finish();
			break;
		case R.id.btn_submit:
			// 输入项目校验
			if (StringUtils.isNull(((EditText) findViewById(R.id.edt_contact)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_mobile)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_business)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_shop_count)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_turnover)).getText().toString())) {
				showToast("请填写完整的店铺信息。");
				break;
			}

			// 提交申请
			sendStoreContent();

			// }
			break;
		default:
			break;
		}
	}

	private void sendStoreContent() {
		// TODO Auto-generated method stub
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String edt_contact = null;
		String edt_address = null;
		String edt_business = null;
		try {
			edt_contact = URLEncoder.encode(((EditText) findViewById(R.id.edt_contact)).getText().toString(), "utf-8");
			edt_address = URLEncoder.encode(((EditText) findViewById(R.id.edt_address)).getText().toString(), "utf-8");
			edt_business = URLEncoder.encode(((EditText) findViewById(R.id.edt_business)).getText().toString(),
					"utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String Ts = MD5.getTimeStamp();

		map.put("UserId", getLoginUserId());
		map.put("ManagementMode", mMode);
		map.put("Mobile", ((EditText) findViewById(R.id.edt_mobile)).getText().toString());
		map.put("ShopCount", ((EditText) findViewById(R.id.edt_shop_count)).getText().toString());
		map.put("AnnualTurnover", ((EditText) findViewById(R.id.edt_turnover)).getText().toString());
		// 负责人
		map.put("ContactPerson", edt_contact);
		// 详细地址
		map.put("DetailAddress", edt_address);
		// 主营业务
		map.put("PrimaryBusiness", edt_business);

		map.put("Province", mCurrProvince);
		map.put("City", mCurrCity);
		map.put("County", mCurrCounty);

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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/Franchise/Add");
		params.addBodyParameter("UserId", getLoginUserId());
		params.addBodyParameter("ManagementMode", mMode);
		params.addBodyParameter("Mobile", ((EditText) findViewById(R.id.edt_mobile)).getText().toString());
		params.addBodyParameter("ShopCount", ((EditText) findViewById(R.id.edt_shop_count)).getText().toString());

		params.addBodyParameter("ContactPerson", edt_contact);
		params.addBodyParameter("DetailAddress", edt_address);
		params.addBodyParameter("PrimaryBusiness", edt_business);

		params.addBodyParameter("AnnualTurnover", ((EditText) findViewById(R.id.edt_turnover)).getText().toString());
		params.addBodyParameter("Province", mCurrProvince);
		params.addBodyParameter("City", mCurrCity);
		params.addBodyParameter("County", mCurrCounty);
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
						String message = getString(R.string.msg_info_join_req_success);
						sendSMS(message);
						showToast("提交申请成功");
						finish();

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

	private void sendSMS(String message) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("Phone", getLoginUserId());
		map.put("Message", message);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/SMS/Send");
		params.addBodyParameter("Phone", getLoginUserId());
		params.addBodyParameter("Message", message);

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

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}
	
	
}
