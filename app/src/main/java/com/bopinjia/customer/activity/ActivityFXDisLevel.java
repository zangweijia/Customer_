package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterCommissionProductList;
import com.bopinjia.customer.adapter.AdapterDistrbutionLevel;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.bean.DistributionLevelBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.NoScrollListview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityFXDisLevel extends BaseActivity {

	private NoScrollListview mLvLevel, mProduct;

	public static ActivityFXDisLevel instance;

	private String type;

	private String fxslevel;

	private ImageView mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_distribution_pay);

		instance = this;
		mContent = (ImageView) findViewById(R.id.webView);
		type = getIntent().getStringExtra("type");
		if (type.equals("1")) {
			// 申请开通
			fxslevel = "";
		} else if (type.equals("3")) {
			fxslevel = getIntent().getStringExtra("fxslevel");
		}

		mLvLevel = (NoScrollListview) findViewById(R.id.ll_level);
		mProduct = (NoScrollListview) findViewById(R.id.ll_product);

		getFXFL();
		getGDLlist();

		getCommissiomProductList();

		findViewById(R.id.btn_return).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mProduct.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				
				Intent ii = new Intent();
				ii.putExtra("IsFreeShipping",mdataList.get(arg2).getIsship());
				ii.putExtra("ProductSKUId", mdataList.get(arg2).getId());
				ii.setClass(ActivityFXDisLevel.this, ActivityProductDetailsNew.class);
				startActivity(ii);
				
			}
		});

	}

	/**
	 * 获取会员级别列表
	 */
	private void getGDLlist() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
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

		String url = Constants.WEBAPI_ADDRESS + "api/GDSType/List?Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getGDLlistCallBack(),this);
	}

	class getGDLlistCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parseList(result);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 解析会员级别列表
	 * 
	 * @param jsonarray
	 */
	private void parseList(String jsonarray) {
		try {

			JSONObject jo = new JSONObject(jsonarray);
			JSONArray dataArray = jo.getJSONArray("Data");

			if (dataArray != null && dataArray.length() > 0) {
				List<DistributionLevelBean> dataList = new ArrayList<DistributionLevelBean>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					DistributionLevelBean m = new DistributionLevelBean();

					m.setImg(data.getString("GDSType_Img"));
					m.setMarket(data.getString("GDSType_OldFee"));
					m.setMonth(data.getString("GDSType_Month"));
					m.setName(data.getString("GDSType_Name"));
					m.setPrice(data.getString("GDSType_Fee"));
					m.setType(data.getString("GDSType_IsDiscount"));

					m.setId(data.getString("GDSType_ID"));

					m.setNowlevel(data.getString("GDSType_Level"));

					m.setLevel(fxslevel);
					m.setEntertype(type);
					dataList.add(m);
				}

				AdapterDistrbutionLevel mLevel = new AdapterDistrbutionLevel(dataList, this,
						R.layout.wj_item_distrbution_level);
				mLvLevel.setAdapter(mLevel);
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

	/**
	 * 获取分销返利图片
	 */
	private void getFXFL() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("NewsId", "10");
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

		String url = Constants.WEBAPI_ADDRESS + "api/News/Detail?NewsId=" + "10" + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getFXFLCallBack(),this);
	}

	class getFXFLCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					JSONObject data = jo.getJSONObject("Data");


					String contentText = data.getString("NewsPic");

					ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
							.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
							.build();

					x.image().bind(mContent, contentText, imageOptions);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * 获取高佣金商品列表
	 */
	private void getCommissiomProductList() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getBindingShop());
		map.put("StateId", "1");

		map.put("PageIndex", "1");
		map.put("PageSize", "100");

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

		String url = Constants.WEBAPI_ADDRESS + "api/Commission/List?UserId=" + getBindingShop() + "&StateId=1"
				+ "&PageIndex=" + "1" + "&PageSize=" + "100" + "&Sign="
				+ Sign + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new getCommissiomProductListCallBack(),this);
	}

	class getCommissiomProductListCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parseproductList(result);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
	List<CommissionProductBean> mdataList;
	private void parseproductList(String jsonarray) {
		try {

			JSONObject jo = new JSONObject(jsonarray);

			JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");

			if (dataArray != null && dataArray.length() > 0) {
				List<CommissionProductBean> dataList = new ArrayList<CommissionProductBean>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					CommissionProductBean m = new CommissionProductBean();

					m.setImg(data.getString("SkuPicture"));

					m.setCommission(data.getString("CommissionPrice"));

					m.setCountryimg(data.getString("CountryImageUrl"));

					m.setName(data.getString("SkuName"));

					m.setPrice(data.getString("ScanPrice"));

					m.setNumber(data.getString("CumulativeSales"));

					m.setId(data.getString("SkuId"));

					m.setCountry(data.getString("CountryName"));

					m.setGold_comission("0");
					
					m.setIsship(data.getString("IsFreeShipping"));
				 	m.setRealStock(data.getString("RealStock"));
					m.setLevel("10");
					dataList.add(m);
				}
				
				mdataList=dataList;

				AdapterCommissionProductList mAdapter = new AdapterCommissionProductList(mdataList, this,
						R.layout.wj_item_commission_product);
				mProduct.setAdapter(mAdapter);
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}
}
