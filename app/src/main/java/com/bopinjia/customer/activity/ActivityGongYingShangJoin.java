package com.bopinjia.customer.activity;

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

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ActivityGongYingShangJoin extends BaseActivity {

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
		setContentView(R.layout.wj_activity_gong_ying_shang_join);

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
			backward();
			break;
		case R.id.btn_submit:
			// 输入项目校验
			if (StringUtils.isNull(((EditText) findViewById(R.id.edt_contact)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_mobile)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_business_type)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_address)).getText().toString())
					|| StringUtils.isNull(((EditText) findViewById(R.id.edt_business_band)).getText().toString())) {
				showToast("请填写完整信息");
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
		String edt_name = null;
		String edt_address = null;
		String edt_business_type = null;
		String edt_business_band = null;
		try {
			edt_name = URLEncoder.encode(((EditText) findViewById(R.id.edt_contact)).getText().toString(), "utf-8");
			edt_address = URLEncoder.encode(((EditText) findViewById(R.id.edt_address)).getText().toString(), "utf-8");
			edt_business_type = URLEncoder
					.encode(((EditText) findViewById(R.id.edt_business_type)).getText().toString(), "utf-8");
			edt_business_band = URLEncoder
					.encode(((EditText) findViewById(R.id.edt_business_band)).getText().toString(), "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String Ts = MD5.getTimeStamp();

		map.put("UserId", getLoginUserId());
		map.put("Mobile", ((EditText) findViewById(R.id.edt_mobile)).getText().toString());
		// 申请人
		map.put("ContactPerson", edt_name);
		// 详细地址
		map.put("DetailAddress", edt_address);
		// 商品类别
		map.put("PrimaryBusiness", edt_business_type);
		// 商品品牌
		map.put("PrimaryBrand", edt_business_band);

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

		Map<String, String> maps = new HashMap<String, String>();
		maps.put("UserId", getLoginUserId());
		maps.put("Mobile", ((EditText) findViewById(R.id.edt_mobile)).getText().toString());

		maps.put("ContactPerson", edt_name);
		maps.put("DetailAddress", edt_address);
		maps.put("PrimaryBusiness", edt_business_type);

		maps.put("Province", mCurrProvince);
		maps.put("City", mCurrCity);
		maps.put("PrimaryBrand", edt_business_band);
		maps.put("County", mCurrCounty);
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/UserSupplier/Add", maps,
				new sendStoreContentCallBack(),this);

	}

	class sendStoreContentCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					showToast("提交申请成功");
					backward();

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
