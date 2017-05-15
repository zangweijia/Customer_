package com.bopinjia.customer.activity;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;

/**
 * @author Administrator 收货地址列表 界面
 */
public class ActivityAddressList extends BaseActivity {

	private List<JSONObject> mAddressList;
	private AddressListAdapter mAddressListAdapter;
	private ListView mList;

	private JSONObject mDivisionData;

	private boolean mIsSelect;

	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_address_list);

		mDivisionData = readDivisions();
		mAddressList = new ArrayList<JSONObject>();
		mIsSelect = getIntent().hasExtra("status");
		// search();
		mList = (ListView) findViewById(R.id.list);
		mProgressBar = (ProgressBar) findViewById(R.id.probar);
		// 返回
		findViewById(R.id.tv_back).setOnClickListener(this);

		// 追加收货地址
		findViewById(R.id.tv_add_address).setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		GetAddresslist();
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
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		switch (viewId) {
		case R.id.tv_back:
			// 返回
			if (mIsSelect) {
				Intent data = new Intent();
				data.putExtra("isdef", "1");
				setResult(RESULT_OK, data);
				finish();
			} else {
				finish();
			}
			break;
		case R.id.tv_add_address:
			// 追加收货地址
			Intent intent = new Intent();
			intent.putExtra("mode", "0");
			forward(ActivityAddressDetails.class, intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 收货地址列表适配器
	 * 
	 * @author yushen 2015/12/10
	 */
	class AddressListAdapter extends BaseAdapter {

		private Context mContext;
		private List<JSONObject> AddressList;

		public AddressListAdapter(Context context, List<JSONObject> mAddressList) {
			this.mContext = context;
			this.AddressList = mAddressList;
		}

		@Override
		public int getCount() {
			return AddressList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				// 数据模型
				final JSONObject model = AddressList.get(position);
				AddressListItem viewItem = new AddressListItem(mContext, model.toString());

				// viewItem.setDefault(model.getBoolean("IsDefault"));
				String ss = model.getString("IsDefault");
				if (ss.equals("1")) {
					viewItem.setDefault(true);
				} else {
					viewItem.setDefault(false);
				}
				viewItem.setId(model.getString("Id"));
				viewItem.setName(model.getString("Consignee"));
				viewItem.setPhone(model.getString("Mobile"));

				viewItem.setIdCard(model.getString("IDCard"));

				String province = "";
				String city = "";
				String county = "";
				JSONObject provinceData = mDivisionData.getJSONObject(model.getString("Province"));
				province = provinceData.getString("name");
				if (provinceData.isNull(model.getString("City"))) {
					// 省key不包含 model.getString("City")
				} else {
					JSONObject cityData = provinceData.getJSONObject(model.getString("City"));
					city = cityData.getString("name");
					if (cityData.isNull(model.getString("County"))) {
						// 城市key不包含 model.getString("County")
					} else {
						JSONObject countyData = cityData.getJSONObject(model.getString("County"));
						county = countyData.getString("name");
					}
				}
				String detailAddress = province + " " + city + " " + county + " " + model.getString("DetailAddress");
				viewItem.setAddress(detailAddress);

				LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, parent.getMeasuredHeight() / 4);
				viewItem.setLayoutParams(layoutParams);

				return viewItem;
			} catch (Exception e) {
				showSysErr(e);
			}

			return convertView;
		}
	}

	class AddressListItem extends LinearLayout {

		private String mAddressData;
		private String AddressId;

		public AddressListItem(Context context, String addressData) {
			super(context);
			mAddressData = addressData;
			View.inflate(getContext(), R.layout.wj_item_address, this);
			this.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mIsSelect) {
						Intent data = new Intent();
						data.putExtra("AddressData", mAddressData);
						setResult(RESULT_OK, data);
						finish();
					} else {

					}
				}
			});

			this.findViewById(R.id.ll_edit).setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("mode", "1");
					intent.putExtra("AddressData", mAddressData);
					forward(ActivityAddressDetails.class, intent);
				}
			});

			final RadioButton rb = (RadioButton) this.findViewById(R.id.rb_isdef);
			rb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					final Dialog mDialog = new Dialog(ActivityAddressList.this, R.style.CustomDialogTheme);
					View dialogView = LayoutInflater.from(ActivityAddressList.this).inflate(R.layout.send_tel_dailog,
							null);
					// 设置自定义的dialog布局
					mDialog.setContentView(dialogView);
					// false表示点击对话框以外的区域对话框不消失，true则相反
					mDialog.setCanceledOnTouchOutside(false);
					// -----------------------------------

					mDialog.show();
					// 获取自定义dialog布局控件
					((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否设置为默认地址?");
					Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
					Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
					// 确定按钮点击事件
					confirmBt.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							SetDefult(AddressId);
							mDialog.dismiss();

						}
					});
					// 取消按钮点击事件
					cancelBt.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							rb.setChecked(false);
							mDialog.dismiss();
						}
					});

				}
			});

		}

		// 设置地址ID
		public void setId(String id) {
			this.AddressId = id;
		}

		public void setIdCard(String idCard) {
			((TextView) findViewById(R.id.txt_shenfenzheng)).setText(idCard);
		}

		// 是否默认
		public void setDefault(boolean isDefault) {
			findViewById(R.id.iv_default).setVisibility(isDefault ? View.VISIBLE : View.GONE);
			if (isDefault) {
				RadioButton r = (RadioButton) findViewById(R.id.rb_isdef);
				r.setChecked(true);
				TextView t = (TextView) findViewById(R.id.tv_isdef);
				t.setText("默认地址");
			}

		}

		// 设置姓名
		public void setName(String name) {
			((TextView) findViewById(R.id.txt_name)).setText(name);
		}

		// 设置电话
		public void setPhone(String phone) {
			((TextView) findViewById(R.id.txt_phone)).setText(phone);
		}

		// 设置详细地址
		public void setAddress(String address) {
			((TextView) findViewById(R.id.txt_address_detail)).setText(address);
		}
	}

	/**
	 * 获取地址列表
	 */
	private void GetAddresslist() {
		// TODO Auto-generated method stub
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
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

		String url = Constants.WEBAPI_ADDRESS + "api/Address/BpwList?UserId=" + getLoginUserId() + "&Sign=" + Sign
				+ "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetAddressListCallBack(), this);

	}

	/**
	 * 获取地址列表回调
	 */
	class GetAddressListCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					mProgressBar.setVisibility(View.GONE);
					parseList(result);
				}
				if (jsonresult.equals("2")) {
					mAddressList = null;
					mAddressList = new ArrayList<JSONObject>();
					mAddressListAdapter = new AddressListAdapter(ActivityAddressList.this, mAddressList);
					mList.setAdapter(mAddressListAdapter);
					mAddressListAdapter.notifyDataSetChanged();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 设置默认地址
	 * 
	 * @param id
	 */
	private void SetDefult(String id) {
		// TODO Auto-generated method stub
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("UserAddressId", id);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Address/BpwUpdateDefault?UserId=" + getLoginUserId()
				+ "&UserAddressId=" + id + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new SetDefultCallBack(), this);

	}

	/**
	 * 设置默认地址回调
	 * 
	 * @author ZWJ
	 *
	 */
	class SetDefultCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					GetAddresslist();
				} else {
					showToast("设置失败");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	private void parseList(String jsonarray) {
		try {

			JSONObject jo = new JSONObject(jsonarray);
			JSONArray dataArray = jo.getJSONArray("Data");

			if (dataArray != null && dataArray.length() > 0) {
				List<JSONObject> dataList = new ArrayList<JSONObject>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					dataList.add(data);
				}
				mAddressList = dataList;
				// 画面初期化时 的检索

				mAddressListAdapter = new AddressListAdapter(ActivityAddressList.this, mAddressList);

				if (mAddressList.size() == 0) {
					((TextView) findViewById(R.id.tv_add_address)).setText("新建通关信息");
				} else {
					((TextView) findViewById(R.id.tv_add_address)).setText("新增通关信息");
				}

				mList.setAdapter(mAddressListAdapter);
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

}
