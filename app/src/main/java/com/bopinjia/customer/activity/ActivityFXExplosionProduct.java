package com.bopinjia.customer.activity;

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
import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterCommissionProductList;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ActivityFXExplosionProduct extends BaseActivity {

	private TextView mTiTleBack;
	private TextView mTiTleName;
	private TextView mTitleSave;
	private ListView mProduct;
	private View headerView;

	List<CommissionProductBean> mList;
	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;
	private TextView mDefult;
	private TextView mPrice;
	private TextView mSaleNumber;
	private TextView mCommissio;

	private AdapterCommissionProductList mAdapter;

	private String typeid = "0";
	private ImageView mHeadImg;
	private View mIncludeNull;
	private String level;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_bao_kuan_cx);

		level = getIntent().getStringExtra("level");
		setTitle();
		init();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTitleSave = (TextView) mTiTle.findViewById(R.id.btn_edit);

		mTitleSave.setText("");
		mTiTleName.setText("爆款促销区");
		mTiTleBack.setOnClickListener(this);
		mTitleSave.setOnClickListener(this);

		mIncludeNull = findViewById(R.id.include_null);
		((TextView) mIncludeNull.findViewById(R.id.txt_null)).setText("抱歉 ，暂无相关商品");

		headerView = View.inflate(this, R.layout.wj_layout_comissiion_prt, null);

		mHeadImg = (ImageView) headerView.findViewById(R.id.iv_title);

		MdAdvertising();
		mDefult = (TextView) headerView.findViewById(R.id.tv_defult);
		mPrice = (TextView) headerView.findViewById(R.id.tv_price);
		mSaleNumber = (TextView) headerView.findViewById(R.id.tv_salenumbei);
		mCommissio = (TextView) headerView.findViewById(R.id.tv_comission);
		mDefult.setTextColor(getResources().getColor(R.color.main_color));
		mDefult.setOnClickListener(this);
		mPrice.setOnClickListener(this);
		mSaleNumber.setOnClickListener(this);
		mCommissio.setOnClickListener(this);
	}

	private void init() {
		mProduct = (ListView) findViewById(R.id.list_baokuan);

		getCommissiomProductList(PageIndex, 0);

		mProduct.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
				if (position == 0) {

				} else {
					Intent ii = new Intent();
					ii.putExtra("IsFreeShipping", mList.get(position - 1).getIsship());
					ii.putExtra("ProductSKUId", mList.get(position - 1).getId());
					ii.setClass(ActivityFXExplosionProduct.this, ActivityProductDetailsNew.class);
					startActivity(ii);
				}
			}
		});

		mProduct.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 当不滚动时
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						// 加载更多功能的代码
						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;
							getCommissiomProductList(PageIndex, 1);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

						}

					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.btn_return:
			finish();
			break;

		case R.id.tv_defult:
			// 默认
			SetContent(0);
			break;
		case R.id.tv_price:
			// 价格排序
			SetContent(1);
			break;

		case R.id.tv_salenumbei:
			// 销量
			SetContent(2);
			break;

		case R.id.tv_comission:
			// 佣金
			SetContent(3);
			break;

		default:
			break;
		}
	}

	private void SetContent(int i) {
		switch (i) {
		case 0:
			typeid = "0";
			PageIndex = 1;
			mDefult.setTextColor(getResources().getColor(R.color.main_color));
			mPrice.setTextColor(getResources().getColor(R.color.bg_0000));
			mSaleNumber.setTextColor(getResources().getColor(R.color.bg_0000));
			mCommissio.setTextColor(getResources().getColor(R.color.bg_0000));
			getCommissiomProductList(1, 3);
			break;
		case 1:
			typeid = "1";
			PageIndex = 1;
			mDefult.setTextColor(getResources().getColor(R.color.bg_0000));
			mPrice.setTextColor(getResources().getColor(R.color.main_color));
			mSaleNumber.setTextColor(getResources().getColor(R.color.bg_0000));
			mCommissio.setTextColor(getResources().getColor(R.color.bg_0000));
			getCommissiomProductList(1, 3);
			break;

		case 2:
			typeid = "2";
			PageIndex = 1;
			mDefult.setTextColor(getResources().getColor(R.color.bg_0000));
			mPrice.setTextColor(getResources().getColor(R.color.bg_0000));
			mSaleNumber.setTextColor(getResources().getColor(R.color.main_color));
			mCommissio.setTextColor(getResources().getColor(R.color.bg_0000));
			getCommissiomProductList(1, 3);
			break;

		case 3:
			typeid = "3";
			PageIndex = 1;
			mDefult.setTextColor(getResources().getColor(R.color.bg_0000));
			mPrice.setTextColor(getResources().getColor(R.color.bg_0000));
			mSaleNumber.setTextColor(getResources().getColor(R.color.bg_0000));
			mCommissio.setTextColor(getResources().getColor(R.color.main_color));
			getCommissiomProductList(1, 3);

			break;

		default:
			break;
		}

	}

	/**
	 * 获取高佣金商品列表
	 */
	private void getCommissiomProductList(int index, final int id) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		map.put("UserId", getLoginUserId());
		map.put("StateId", "2");
		map.put("PageIndex", String.valueOf(index));
		map.put("PageSize", "3");
		map.put("TypeId", typeid);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Commission/List_GDS?UserId=" + getLoginUserId() + "&StateId=2"
				+ "&TypeId=" + typeid + "&PageIndex=" + String.valueOf(index) + "&PageSize=" + "3" + "&Sign="

				+ Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getCommissiomProductListCallBack(), id, null, this);
	}

	class getCommissiomProductListCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					// ((LinearLayout)
					// findViewById(R.id.ll)).removeView(headerView);
					parseproductList(result, id);
				} else {
					showToast("暂无商品信息");
//					((LinearLayout) findViewById(R.id.ll)).addView(headerView);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	private void parseproductList(String jsonarray, int id) {
		try {

			JSONObject jo = new JSONObject(jsonarray);

			JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");

			JSONObject Paging = jo.getJSONObject("Data").getJSONObject("Paging");

			mAllPages = Paging.getString("Pages");

			if (dataArray != null && dataArray.length() > 0) {
				List<CommissionProductBean> dataList = new ArrayList<CommissionProductBean>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					CommissionProductBean m = new CommissionProductBean();

					m.setImg(data.getString("SkuPicture"));

					m.setCommission(data.getString("UserCommissionPrice"));

					m.setCountryimg(data.getString("CountryImageUrl"));

					m.setName(data.getString("SkuName"));

					m.setPrice(data.getString("ScanPrice"));

					m.setNumber(data.getString("CumulativeSales"));

					m.setId(data.getString("SkuId"));

					m.setCountry(data.getString("CountryName"));

					// 金牌佣金
					m.setGold_comission(data.getString("CommissionPrice"));
					m.setLevel(level);
					m.setIsship(data.getString("IsFreeShipping"));
					m.setRealStock(data.getString("RealStock"));
					dataList.add(m);
				}

				switch (id) {
				case 0:
					mList = dataList;
					mAdapter = new AdapterCommissionProductList(mList, this, R.layout.wj_item_gold_comision_prod);
					mProduct.setAdapter(mAdapter);
					mProduct.addHeaderView(headerView);
					break;
				case 1:
					if (dataList != null && !dataList.isEmpty()) {
						mList.addAll(dataList);
						mAdapter.notifyDataSetChanged();
					}
					break;
				case 3:
					mList = dataList;
					mAdapter = new AdapterCommissionProductList(mList, this, R.layout.wj_item_gold_comision_prod);
					mProduct.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();

					break;
				default:
					break;
				}

			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

	/**
	 * 获取banner图
	 */
	private void MdAdvertising() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		map.put("ModuleId", "78");

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

		String url = Constants.WEBAPI_ADDRESS + "api/MdAdvertising/Detail?ModuleId=" + "78" + "&Sign="

				+ Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new MdAdvertisingCallBack(), this);

	}

	class MdAdvertisingCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					ImageOptions imageOptions = new ImageOptions.Builder()
							.setImageScaleType(ImageView.ScaleType.CENTER_CROP).build();

					x.image().bind(mHeadImg, jo.getJSONObject("Data").getString("ModuleImg"), imageOptions);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}
}
