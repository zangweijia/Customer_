package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.popupwindow.NoProduct;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.Callback.CommonCallback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityConfirmOrder extends BaseActivity {
	private String orderResult;

	private JSONObject mDivisionData;
	private String province;
	private String city;
	private String county;
	private String mDetail;
	private String addressId;

	private LayoutInflater mInflater;
	private TextView mAllCount;
	private LinearLayout mGallery;

	private String mResult;
	// 商品总数量
	private int mProducts;
	private TextView mSelectShip, mSelectShipPS;

	private String tyId = "3";
	// 购物车传参
	private String productName;

	private String mRedId = "0";

	private NoProduct mNOProduct;
	private int index = 0;
	private int productsize;
	JSONArray mArray;

	private boolean isUpdata = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_confirm_order);
		mDivisionData = readDivisions();
		init();
		GetDefultAddress();
	}

	private void init() {
		// 购物车界面传递过来的金额以及运费
		mResult = getIntent().getStringExtra("result");
		productName = getIntent().getStringExtra("JsonSkuName");
		mAllCount = (TextView) findViewById(R.id.tv_all_count);
		mAllCount.setOnClickListener(this);
		initData(mResult);
		mInflater = LayoutInflater.from(this);
		mSelectShip = (TextView) findViewById(R.id.tv_ship_type);
		mSelectShipPS = (TextView) findViewById(R.id.tv_ship_shangjiapeisong);
		mSelectShip.setOnClickListener(this);
		mSelectShipPS.setOnClickListener(this);

		findViewById(R.id.btn_return).setOnClickListener(this);
		findViewById(R.id.btn_go_pay).setOnClickListener(this);
		//
		findViewById(R.id.tv_go_address).setOnClickListener(this);

		// 选择红包
		findViewById(R.id.ll_red).setOnClickListener(this);
		// 更换收货地址
		findViewById(R.id.btn_change_address).setOnClickListener(this);
		GetProductImage();

	}

	/**
	 * 赋值运费总金额
	 * 
	 * @return
	 */
	private void initData(String str) {

		DecimalFormat df = new DecimalFormat("##0.00");

		try {
			JSONObject json = new JSONObject(str);
			JSONObject data = json.getJSONObject("Data");
			JSONObject devfree = data.getJSONObject("DevFree");

			((TextView) findViewById(R.id.zy_count)).setText("共" + devfree.getString("ZYcount") + "件");
			((TextView) findViewById(R.id.xh_count)).setText("共" + devfree.getString("XHcount") + "件");

			((TextView) findViewById(R.id.tv_zy_ship)).setText("¥" + devfree.getString("ZYFree"));
			((TextView) findViewById(R.id.tv_xh_ship)).setText("¥" + devfree.getString("XHFree"));

			int i = Integer.parseInt(devfree.getString("ZYcount")) + Integer.parseInt(devfree.getString("XHcount"));

			mAllCount.setText("共" + i + "件");
			mProducts = i;

			if (devfree.getString("XHcount").equals("0")) {
				findViewById(R.id.ll_xh).setVisibility(View.GONE);
			} else {
				findViewById(R.id.ll_xh).setVisibility(View.VISIBLE);
			}

			if (devfree.getString("ZYcount").equals("0")) {
				findViewById(R.id.ll_zy).setVisibility(View.GONE);
			} else {
				findViewById(R.id.ll_zy).setVisibility(View.VISIBLE);
			}

			double dbSellPrice = new BigDecimal(data.getString("TotalAmount")).doubleValue();
			((TextView) findViewById(R.id.txt_product_amount)).setText("¥" + df.format(dbSellPrice));
			((TextView) findViewById(R.id.txt_ship)).setText("¥" + data.getString("TotalShipFee"));
			((TextView) findViewById(R.id.txt_all_amount)).setText(data.getString("TotalPrice"));

			// 几个红包可用
			((TextView) findViewById(R.id.txt_red_use)).setText(data.getString("HbCount") + "张可用");
			// 红包金额
			((TextView) findViewById(R.id.txt_redpagket1))
					.setText(MessageFormat.format(getString(R.string.txt_order_red1), data.getString("HbAmount")));
			// 最下方展示红包金额
			((TextView) findViewById(R.id.txt_redpagket))
					.setText(MessageFormat.format(getString(R.string.txt_order_red), data.getString("HbAmount")));
			mRedId = data.getString("HbId");
			// 当红包不可用时最下方展示 隐藏
			if (data.getString("HbCount").equals("0")) {
				findViewById(R.id.ll_bottom_red_number).setVisibility(View.GONE);
				((TextView) findViewById(R.id.txt_redpagket1)).setText("暂无可用");
			} else {
				findViewById(R.id.ll_bottom_red_number).setVisibility(View.VISIBLE);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_all_count:
			// 购买商品列表
			Intent goList = new Intent();
			goList.putExtra("count", mProducts);
			forward(ActivityConfirmOrderProductList.class, goList);
			break;

		case R.id.tv_ship_shangjiapeisong:
		case R.id.tv_ship_type:
			// 配送方式
			Intent toSelect = new Intent(ActivityConfirmOrder.this, ActivitySelectShip.class);
			toSelect.putExtra("tyId", tyId);
			startActivityForResult(toSelect, 3);
			break;
		case R.id.btn_return:
			// 返回
			finish();
			break;
		case R.id.btn_go_pay:
			// 确认订单
			if (addressId == null) {
				showToast("请先填写地址信息~");
			} else {
				if (isUpdata) {
					index = 1;
					showUpdateDialog(mArray);
				} else {
					Submit();
				}
			}
			break;
		case R.id.btn_change_address:
			// 更换收货地址
			Intent intent = new Intent(ActivityConfirmOrder.this, ActivityAddressList.class);
			intent.putExtra("status", "1");
			startActivityForResult(intent, 1);
			break;
		case R.id.tv_go_address:
			// 添加地址
			Intent i = new Intent(ActivityConfirmOrder.this, ActivityAddressDetails.class);
			i.putExtra("mode", "5");
			startActivityForResult(i, 2);
			break;
		case R.id.ll_red:
			Intent redintent = new Intent(ActivityConfirmOrder.this, ActivityUseRedList.class);
			startActivityForResult(redintent, 4);

			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {
			if (requestCode == 1) {
				if (data != null) {
					if (data.hasExtra("isdef")) {
						GetDefultAddress();
					} else {
						String addressData = data.getStringExtra("AddressData");

						JSONObject addressObj = new JSONObject(addressData);

						// 默认
						if ("1".equals(addressObj.getString("IsDefault"))) {
							findViewById(R.id.iv_default).setVisibility(View.VISIBLE);
						} else {
							findViewById(R.id.iv_default).setVisibility(View.INVISIBLE);
						}

						// 收货人
						((TextView) findViewById(R.id.txt_name)).setText(addressObj.getString("Consignee"));
						// 手机
						((TextView) findViewById(R.id.txt_phone)).setText(addressObj.getString("Mobile"));
						addressId = addressObj.getString("Id");
						setAddress(addressObj, "DetailAddress");
					}
				}
			} else if (requestCode == 2) {
				// 新增联系人地址后 回调
				GetDefultAddress();
				// CheckOut();
			} else if (requestCode == 3) {
				// 选择配送方式
				if (data != null) {
					tyId = data.getStringExtra("tyId");
					if (tyId.equals("3")) {

						mSelectShipPS.setVisibility(View.VISIBLE);
						mSelectShip.setVisibility(View.GONE);

					} else if (tyId.equals("1")) {

						mSelectShipPS.setVisibility(View.GONE);
						mSelectShip.setVisibility(View.VISIBLE);
					}
					CheckOut();
				}
			} else if (requestCode == 4) {
				// 选择红包
				if (data != null) {
					mRedId = data.getStringExtra("redId");
					CheckOut();
				}

			}
		} catch (Exception e) {
			showSysErr(e);
		}
	}

	private void GetDefultAddress() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Address/BpwDefault?UserId=" + getLoginUserId() + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new GetDefultAddressCallBack(), this);
	}

	class GetDefultAddressCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {

				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					findViewById(R.id.ll_null_address).setVisibility(View.GONE);
					findViewById(R.id.btn_change_address).setVisibility(View.VISIBLE);

					JSONObject mJsonData = jo.getJSONObject("Data");
					setAddress(mJsonData, "DetailAddress");
				} else if (jsonresult.equals("0")) {
					findViewById(R.id.ll_null_address).setVisibility(View.VISIBLE);
					findViewById(R.id.btn_change_address).setVisibility(View.GONE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	private void setAddress(JSONObject addressObj, String detail) throws JSONException {
		addressId = addressObj.getString("Id");

		((TextView) findViewById(R.id.txt_name)).setText(addressObj.getString("Consignee"));
		((TextView) findViewById(R.id.txt_phone)).setText(addressObj.getString("Mobile"));
		((TextView) findViewById(R.id.txt_idcard)).setText(addressObj.getString("IDCard"));

		if ("1".equals(addressObj.getString("IsDefault"))) {
			findViewById(R.id.iv_default).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.iv_default).setVisibility(View.INVISIBLE);
		}

		if (mDivisionData.has(addressObj.getString("Province"))) {
			JSONObject provinceData = mDivisionData.getJSONObject(addressObj.getString("Province"));

			if (provinceData.has(addressObj.getString("City"))) {
				JSONObject cityData = provinceData.getJSONObject(addressObj.getString("City"));

				if (cityData.has(addressObj.getString("County"))) {
					JSONObject countyData = cityData.getJSONObject(addressObj.getString("County"));
					province = provinceData.getString("name");
					city = cityData.getString("name");
					county = countyData.getString("name");
					mDetail = addressObj.getString(detail);
					((TextView) findViewById(R.id.txt_address_detail))
							.setText(province + " " + city + " " + county + " " + mDetail);
				}
			}
		}
	}

	private void GetProductImage() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/CSC/BpwPayPicList?UserId=" + getLoginUserId() + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		RequestParams params = new RequestParams(url);
		x.http().get(params, new CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						List<String> mLists = new ArrayList<String>();
						List<String> mListRealStockState = new ArrayList<String>();
						JSONArray dataArray = jo.getJSONArray("Data");

						if (dataArray != null && dataArray.length() > 0) {
							for (int i = 0, length = dataArray.length(); i < length; i++) {
								JSONObject data = dataArray.getJSONObject(i);
								mLists.add(data.getString("ProductThumbnail"));
								mListRealStockState.add(data.getString("RealStockState"));
							}
						}
						productsize = mLists.size();
						getAllNoStockList();

						mGallery = (LinearLayout) findViewById(R.id.list);
						mGallery.removeAllViews();
						for (int i = 0; i < mLists.size(); i++) {
							View view = mInflater.inflate(R.layout.wj_item_recycleview, mGallery, false);
							ImageView img = (ImageView) view.findViewById(R.id.iv_picture);
							if (mListRealStockState.get(i).equals("0")) {
								// 有货
								((ImageView) view.findViewById(R.id.iv_no_product)).setVisibility(View.GONE);
							} else {
								// 无货
								((ImageView) view.findViewById(R.id.iv_no_product)).setVisibility(View.VISIBLE);
								((ImageView) view.findViewById(R.id.iv_no_product)).setAlpha(0.6f);
							}

							ImageOptions imageOptions = new ImageOptions.Builder()
									.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
									.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
									.build();
							x.image().bind(img, mLists.get(i), imageOptions);

							mGallery.addView(view);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(Throwable arg0, boolean arg1) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onFinished() {
				// TODO Auto-generated method stub

			}

		});
	}

	/**
	 * 去结算
	 */
	private void CheckOut() {
		Map<String, String> map = new HashMap<String, String>();
		String Ts = MD5.getTimeStamp();
		map.put("UserId", getLoginUserId());
		map.put("TypeId", tyId);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("AddressId", addressId);
		map.put("RPRID", mRedId);
		map.put("Ts", Ts);

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("UserId", getLoginUserId());
		maps.put("TypeId", tyId);
		maps.put("Sign", NetUtils.getSign(map));
		maps.put("AddressId", addressId);
		maps.put("RPRID", mRedId);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/CSC/BpwCheckOut_New", maps,
				new CheckOutCallBack(), this);

	}

	class CheckOutCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					initData(result);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 确认订单 跳到结算页面
	 */
	private void Submit() {
		Map<String, String> map = new HashMap<String, String>();
		String mGDSUserId = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_GDSUSERID);

		String Ts = MD5.getTimeStamp();
		map.put("UserId", getLoginUserId());
		map.put("TypeId", tyId);
		map.put("MDId", getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP));
		map.put("Source", "3");
		map.put("GDSUserId", mGDSUserId);
		map.put("HbId", mRedId);

		map.put("AddressId", addressId);

		map.put("Remark", ((EditText) findViewById(R.id.et_remarks)).getText().toString().trim());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		Map<String, String> maps = new HashMap<String, String>();

		maps.put("UserId", getLoginUserId());
		maps.put("TypeId", tyId);
		maps.put("MDId", getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP));
		maps.put("HbId", mRedId);
		maps.put("Source", "3");
		maps.put("GDSUserId", mGDSUserId);
		maps.put("AddressId", addressId);
		maps.put("Remark", ((EditText) findViewById(R.id.et_remarks)).getText().toString().trim());
		maps.put("Sign", NetUtils.getSign(map));
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/CSC/BpwSubmit_New", maps, new SubmitCallBack(),
				this);

	}

	class SubmitCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					String orderId = jo.getString("Data");
					Intent intent = new Intent();
					intent.putExtra("OrderId", orderId);

					String str = ((TextView) findViewById(R.id.txt_all_amount)).getText().toString();
					DecimalFormat format = new DecimalFormat("0.00");
					String p = format.format(new BigDecimal(str));

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String mNowTime = sdf.format(new Date());
					intent.putExtra("JsonSkuName", productName);
					intent.putExtra("CreateTime", mNowTime);
					intent.putExtra("payAmount", p);
					forward(ActivityPay.class, intent);
					finish();
				}else{
 					showToast(jo.getString("Message"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private void showUpdateDialog(JSONArray result) {
		mNOProduct = new NoProduct(this, itemsOnClick, index, result);
		mNOProduct.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

	}

	/**
	 * 对话框点击事件
	 */
	private View.OnClickListener itemsOnClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_send:// 返回购物车
				finish();
				break;
			case R.id.bt_cancel://
				if (index == 0) {
					// 全部     更换收货地址
					Intent intent = new Intent(ActivityConfirmOrder.this, ActivityAddressList.class);
					intent.putExtra("status", "1");
					startActivityForResult(intent, 1);
				} else {
					// 部分   移除无货商品
					mNOProduct.dismiss();
					UpdateNoStock();
				}
				break;
			}
		}
	};

	/**
	 * 获取全部没有商品的数据
	 */
	private void getAllNoStockList() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwNoStockList?UserId="
				+ getLoginUserId() + "&Sign=" + Sign + "&Ts=" + Ts);
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						JSONArray array = jo.getJSONArray("Data");
						if (array.length() >= productsize) {
							index = 0;
							showUpdateDialog(array);
						} else {
							index = 1;
							isUpdata = true;
							mArray = array;
						}
					} else if (jsonresult.equals("0")) {
						isUpdata = false;
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
				Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFinished() {

			}
		});
	}

	/**
	 * 移除无库存商品
	 */
	private void UpdateNoStock() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwUpdateC_NoStock?UserId="
				+ getLoginUserId() + "&Sign=" + Sign + "&Ts=" + Ts);
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						isUpdata = false;
						GetProductImage();
						CheckOut();
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
				Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFinished() {

			}
		});
	}

}
