package com.bopinjia.customer.activityhome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.ActivityThreeClassification;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.adapter.AdapterBottomProductList;
import com.bopinjia.customer.adapter.AdapterBottomProductList.AddCartOnclick;
import com.bopinjia.customer.adapter.AdapterHomeCategoryGrid;
import com.bopinjia.customer.adapter.MyImageViewPagerAdapter;
import com.bopinjia.customer.bean.HomeCategoryBean;
import com.bopinjia.customer.bean.ImageViewListBean;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.NoScrollGridView;
import com.bopinjia.customer.view.NoScrollListview;
import com.bopinjia.customer.view.XCRoundRectImageView;
import com.bopinjia.customer.view.MyScrollView;
import com.bopinjia.customer.view.MyScrollView.OnScrollToBottomListener;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ActivityPersonalCare extends BaseActivity {

	/**
	 * 活动特惠主布局 动态添加布局
	 */
	@ViewInject(R.id.ll_special)
	private LinearLayout mSpecialLayout;

	@ViewInject(R.id._indicator)
	private CirclePageIndicator mIndicator;

	@ViewInject(R.id._pager)
	private ViewPager mPager;

	@ViewInject(R.id.category_gridview)
	private NoScrollGridView mCategoryGridView;

	@ViewInject(R.id.list)
	private NoScrollListview mList;

	@ViewInject(R.id.scrollview)
	private MyScrollView mScrollview;

	@ViewInject(R.id.tvmore)
	private TextView tvmore;

	private List<ImageViewListBean> piclists;

	private List<ProductGridviewClassSubBean> list;
	private AdapterBottomProductList mAdapter;
	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;

	private String IsFreeShipping;

	private String userid;
	private List<HomeCategoryBean> lista;

	private RelativeLayout mViewPagerContainer;
	/** 美食 活动界面 滑动宽度 */
	private int pagerWidth = 0;

	private String type;

	private String code;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_personal_care);
		x.view().inject(this);
		// code = getIntent().getStringExtra("")
		code = "003";
		init();
	}

	private void init() {
		IsFreeShipping = String.valueOf(getIntent().getIntExtra("type", 1));
		type = getIntent().getStringExtra("id");
		View mTiTle = findViewById(R.id.include_title);
		TextView mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);

		if (type.equals("0")) {
			// 添加个护活动
			addPersonCare();
			mTiTleName.setText("个护美妆");
		} else if (type.equals("1")) {
			// 添加母婴
			addBabyToys();
			mTiTleName.setText("母婴玩具");
		} else if (type.equals("2")) {
			// 添加进口美食
			mTiTleName.setText("进口美食");
			FoodProductList();
		} else if (type.equals("3")) {
			mTiTleName.setText("家居用品");
			FoodProductList();
		} else if (type.equals("4")) {
			mTiTleName.setText("营养保健");
			addHealthCare();
		}

		// 获取底部商品列表
		getProductList(0, "0");

		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				Intent i = new Intent();
				i.putExtra("IsFreeShipping", "1");
				i.putExtra("ProductSKUId", list.get(arg2).getId());
				i.setClass(ActivityPersonalCare.this, ActivityProductDetailsNew.class);
				startActivity(i);
			}
		});
		mScrollview.setOnScrollToBottomLintener(new OnScrollToBottomListener() {

			@Override
			public void onScrollBottomListener(boolean isBottom) {
				if (isBottom) {
					if (PageIndex < Integer.parseInt(mAllPages)) {
						PageIndex += 1;
						getProductList(1, "0");
						tvmore.setText("加载更多");
					} else if (PageIndex >= Integer.parseInt(mAllPages)) {
						tvmore.setText("没有更多了");
					}
				}

			}
		});
		mList.setFocusable(false);
		mCategoryGridView.setFocusable(false);

	}

	/**
	 * 添加 个护美妆 活动布局
	 */
	private void addPersonCare() {
		LinearLayout productView = new LinearLayout(this);
		View.inflate(this, R.layout.wj_category_personal_care, productView);
		mSpecialLayout.addView(productView);
		getCategory("8");

	}

	/**
	 * 添加 营养保健 活动布局
	 */
	private void addHealthCare() {
		LinearLayout productView = new LinearLayout(this);
		View.inflate(this, R.layout.wj_category_health_care_, productView);
		mSpecialLayout.addView(productView);
		getCategory("8");

	}

	/**
	 * 添加母婴玩具 活动布局
	 */
	private void addBabyToys() {
		LinearLayout productView = new LinearLayout(this);
		View.inflate(this, R.layout.wj_category_baby_toys_, productView);
		mSpecialLayout.addView(productView);
		getCategory("8");

	}

	/**
	 * 添加进口美食 活动布局
	 */
	private void addImportedFood() {
		LinearLayout productView = new LinearLayout(this);
		View.inflate(this, R.layout.wj_category_imported_food, productView);
		ViewPager vp = (ViewPager) productView.findViewById(R.id.view_pager);
		mViewPagerContainer = (RelativeLayout) productView.findViewById(R.id.gallery_viewpager_layout);

		pagerWidth = (int) (getResources().getDisplayMetrics().widthPixels * 3.0f / 6.0f);

		List<View> images = new ArrayList<View>();
		for (int i = 0; i < 5; i++) {
			String pic = piclists.get(0).getImg();
			XCRoundRectImageView imageView = new XCRoundRectImageView(ActivityPersonalCare.this);
			LayoutParams lp = imageView.getLayoutParams();
			if (lp == null) {
				lp = new LayoutParams(pagerWidth, LayoutParams.MATCH_PARENT);
			} else {
				lp.width = pagerWidth;
				lp.height = LayoutParams.MATCH_PARENT;
			}
			imageView.setScaleType(ScaleType.FIT_XY);

			imageView.setLayoutParams(lp);

			setImageFromUrl(pic, imageView);
			images.add(imageView);

		}

		vp.setAdapter(new MyImageViewPagerAdapter(ActivityPersonalCare.this, images, piclists));
		vp.setOffscreenPageLimit(3);
		vp.setPageMargin(getResources().getDimensionPixelSize(R.dimen.base_padding));
		vp.setCurrentItem(1);
		vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
				if (mViewPagerContainer != null) {
					mViewPagerContainer.invalidate(); // 更新超出区域页面，否则会出现页面缓存，导致页面效果不佳
				}
			}
		});

		mSpecialLayout.addView(productView);
		getCategory("8");

	}

	/**
	 * 添加家居用品 活动布局
	 */
	private void addHouseWear() {
		LinearLayout productView = new LinearLayout(this);
		View.inflate(this, R.layout.wj_category_imported_food, productView);
		ViewPager vp = (ViewPager) productView.findViewById(R.id.view_pager);
		mViewPagerContainer = (RelativeLayout) productView.findViewById(R.id.gallery_viewpager_layout);

		pagerWidth = (int) (getResources().getDisplayMetrics().widthPixels * 3.0f / 6.0f);

		List<View> images = new ArrayList<View>();
		for (int i = 0; i < 1; i++) {
			String pic = piclists.get(0).getImg();
			XCRoundRectImageView imageView = new XCRoundRectImageView(ActivityPersonalCare.this);
			LayoutParams lp = imageView.getLayoutParams();
			if (lp == null) {
				lp = new LayoutParams(pagerWidth, LayoutParams.MATCH_PARENT);
			} else {
				lp.width = pagerWidth;
				lp.height = LayoutParams.MATCH_PARENT;
			}
			imageView.setScaleType(ScaleType.FIT_XY);

			imageView.setLayoutParams(lp);

			setImageFromUrl(pic, imageView);
			images.add(imageView);

		}

		vp.setAdapter(new MyImageViewPagerAdapter(ActivityPersonalCare.this, images, piclists));
		vp.setOffscreenPageLimit(3);
		vp.setPageMargin(getResources().getDimensionPixelSize(R.dimen.base_padding));
		vp.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				super.onPageScrolled(position, positionOffset, positionOffsetPixels);
				if (mViewPagerContainer != null) {
					mViewPagerContainer.invalidate(); // 更新超出区域页面，否则会出现页面缓存，导致页面效果不佳
				}
			}
		});

		mSpecialLayout.addView(productView);
		getCategory("2");

	}

	@Event(value = { R.id.btn_return })
	private void getEvent(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			finish();
			break;

		default:
			break;
		}
	}

	private void setBanner(JSONArray piclist) {
		try {
			piclists = new ArrayList<ImageViewListBean>();
			for (int i = 0; i < piclist.length(); i++) {
				ImageViewListBean m = new ImageViewListBean();
				m.setImg(piclist.getJSONObject(i).getString("ModuleImg"));
				m.setName(piclist.getJSONObject(i).getString("ModuleName"));
				m.setUrl(piclist.getJSONObject(i).getString("ModuleURL"));
				piclists.add(m);
			}

			if (piclists.size() == 1) {
				mIndicator.setVisibility(View.GONE);
			}

			List<View> images = new ArrayList<View>();
			for (int i = 0; i < piclists.size(); i++) {
				String pic = piclists.get(i).getImg();
				ImageView imageView = new ImageView(ActivityPersonalCare.this);
				imageView.setScaleType(ImageView.ScaleType.FIT_XY);

				setImageFromUrl(pic, imageView);
				images.add(imageView);

			}
			mPager.setAdapter(new MyImageViewPagerAdapter(ActivityPersonalCare.this, images, piclists));
			mIndicator.setViewPager(mPager);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取底部商品列表
	 * 
	 * @param id
	 * @param OrderBy
	 */
	private void getProductList(final int id, String OrderBy) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getBindingShop());
		map.put("WhereCode", "003");
		map.put("OrderBy", "0");
		map.put("ZY", IsFreeShipping);
		map.put("PageIndex", String.valueOf(PageIndex));
		map.put("pageSize", "20");
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

		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListCodeBpw_New?UserId=" + getBindingShop()
				+ "&WhereCode=" + "003" + "&OrderBy=" + "0" + "&ZY=" + IsFreeShipping + "&PageIndex="
				+ String.valueOf(PageIndex) + "&pageSize=" + "20" + "&Sign=" + Sign + "&Ts=" + Ts;

		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						parseList(result, id);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

	/**
	 * 解析商品列表
	 * 
	 * @param jsonarray
	 * @param id
	 */
	private void parseList(String jsonarray, int id) {
		try {

			JSONObject jo = new JSONObject(jsonarray);
			JSONObject jsondata = jo.getJSONObject("Data");

			JSONArray dataArray = jsondata.getJSONArray("Records");
			JSONObject Paging = jsondata.getJSONObject("Paging");
			mAllPages = Paging.getString("Pages");
			if (dataArray != null && dataArray.length() > 0) {
				List<ProductGridviewClassSubBean> dataList = new ArrayList<ProductGridviewClassSubBean>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					ProductGridviewClassSubBean m = new ProductGridviewClassSubBean();

					m.setImg(data.getString("ProductThumbnail"));
					m.setMarketprice(data.getString("MarketPrice"));
					m.setIsshiping("1");
					m.setNumber(data.getString("CumulativeSales"));
					m.setName(data.getString("ProductSKUName"));
					m.setPrice(data.getString("ScanPrice"));
					m.setId(data.getString("ProductSKUId"));
					m.setCountry(data.getString("CountryName"));
					m.setCountryimg(data.getString("CountryImageUrl"));
					m.setRealStock(data.getString("RealStock"));
					dataList.add(m);
				}
				switch (id) {
				case 0:
					list = dataList;
					mAdapter = new AdapterBottomProductList(list, this, R.layout.wj_item_home_product,
							new addCartOnclick());
					mList.setAdapter(mAdapter);

					break;
				case 1:
					if (dataList != null && !dataList.isEmpty()) {
						list.addAll(dataList);
						mAdapter.notifyDataSetChanged();
					}
					break;

				default:
					break;
				}

			} else {
			}
		} catch (Exception e) {

		}

	}

	class addCartOnclick implements AddCartOnclick {

		@Override
		public void addCart(View v) {
			AddCart(list.get((Integer) v.getTag()).getId());
		}
	}

	/**
	 * 添加购物车
	 * 
	 * @param Quantity
	 * @param SkuId
	 * @param ProductType
	 */
	private void AddCart(String SkuId) {
		if (isLogged()) {
			userid = getLoginUserId();
		} else {
			userid = "0";
		}

		String shopid = getBindingShop();

		String Ts = MD5.getTimeStamp();
		String str = "Key=" + Constants.WEBAPI_KEY + "&MUserId=" + shopid + "&Paystate=0" + "&ProductType="
				+ IsFreeShipping + "&Quantity=" + "1" + "&SkuId=" + SkuId + "&Ts=" + Ts + "&UserId=" + userid + "&UUID="
				+ getIMEI1() + "&WarehouseId=0";

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwAdd_New");
		params.addBodyParameter("ProductType", IsFreeShipping);
		params.addBodyParameter("SkuId", SkuId);
		params.addBodyParameter("MUserId", shopid);
		params.addBodyParameter("Paystate", "0");
		params.addBodyParameter("WarehouseId", "0");
		params.addBodyParameter("UUID", getIMEI1());
		params.addBodyParameter("Quantity", "1");
		params.addBodyParameter("UserId", userid);

		params.addBodyParameter("Sign", MD5.Md5(str));
		params.addBodyParameter("Ts", Ts);
		params.setAsJsonContent(true);

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						showToast("成功加入购物车");
					} else {
						showToast("很遗憾，添加购物车失败");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				showErr();
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});

	}

	/**
	 * 获取三级分类
	 * 
	 * @param size
	 *            分类的个数
	 * @param code
	 *            分类的code值
	 */
	private void getCategory(String size) {
		String MuserId = getBindingShop();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("MdUserId", MuserId);
		map.put("ZY", IsFreeShipping);
		map.put("Code", code);
		map.put("Size", size);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/GetHomeAdsClassIndex?MdUserId=" + MuserId + "&Code="
				+ code + "&ZY=" + IsFreeShipping + "&Size=" + size + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getCategoryList(), this);
	}

	class getCategoryList implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONArray jsonarray = jo.getJSONObject("Data").getJSONArray("RecommendProductCategory");
					lista = new ArrayList<HomeCategoryBean>();
					if (jsonarray != null && jsonarray.length() > 0) {

						for (int i = 0; i < jsonarray.length(); i++) {
							HomeCategoryBean m = new HomeCategoryBean();
							JSONObject data = jsonarray.getJSONObject(i);
							m.setImg(data.getString("Picture"));
							m.setName(data.getString("CName"));
							m.setCcode(data.getString("CCode"));
							lista.add(m);
						}
					}

					JSONArray piclist = jo.getJSONObject("Data").getJSONArray("ProductChannelAD");
					setBanner(piclist);

					if (lista.size() == 2) {
						mCategoryGridView.setNumColumns(2);
						mCategoryGridView.setAdapter(new AdapterHomeCategoryGrid(lista, ActivityPersonalCare.this,
								R.layout.wj_item_home_category_, true));
						findViewById(R.id.bg_view).setVisibility(View.GONE);
					} else {
						mCategoryGridView.setNumColumns(4);
						mCategoryGridView.setAdapter(new AdapterHomeCategoryGrid(lista, ActivityPersonalCare.this,
								R.layout.wj_item_home_category, true));
					}

					mCategoryGridView.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							Intent i = new Intent();
							i.putExtra("ccode", lista.get(arg2).getCcode());
							i.putExtra("IsFreeShipping", IsFreeShipping);
							forward(ActivityThreeClassification.class, i);
						}
					});

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	private void FoodProductList() {
		String MuserId = getBindingShop();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("SkuId", MuserId);
		map.put("ZY", IsFreeShipping);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Product/GetIndexadlist?UserId=" + MuserId + "&ZY=" + IsFreeShipping
				+ "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new FoodProductListBack(), this);

	}

	class FoodProductListBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					JSONArray piclist = jo.getJSONObject("Data").getJSONArray("indexChannelAD");
					piclists = new ArrayList<ImageViewListBean>();
					for (int i = 0; i < piclist.length(); i++) {
						ImageViewListBean m = new ImageViewListBean();
						m.setImg(piclist.getJSONObject(i).getString("ModuleImg"));
						m.setName(piclist.getJSONObject(i).getString("ModuleName"));
						m.setUrl(piclist.getJSONObject(i).getString("ModuleURL"));
						piclists.add(m);
					}

					if (type.equals("2")) {
						addImportedFood();
					} else if (type.equals("3")) {
						addHouseWear();
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
