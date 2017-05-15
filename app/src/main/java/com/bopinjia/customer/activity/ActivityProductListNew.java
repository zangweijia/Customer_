package com.bopinjia.customer.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.adapter.GoodsAttrListAdapter;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.bean.SaleAttributeNameVo;
import com.bopinjia.customer.bean.SaleAttributeVo;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;

import net.simonvt.menudrawer.MenuDrawer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityProductListNew extends BaseActivity {

	/**
	 * 布局切换
	 */
	@ViewInject(R.id.tv_layout)
	private TextView mSwitchLayout;
	private boolean isGrid = true;
	/**
	 * 综合排序
	 */
	@ViewInject(R.id.tv_comprehensive)
	private TextView mComprehensive;

	/**
	 * 销量排序
	 */
	@ViewInject(R.id.tv_sales)
	private TextView mSales;

	/**
	 * 价格排序
	 */
	@ViewInject(R.id.tv_price)
	private TextView mPrice;

	/**
	 * 筛选按钮
	 */
	@ViewInject(R.id.tv_sort)
	private TextView mSort;

	private ListView mListView;
	private GridView mGridView;

	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;

	private List<ProductGridviewClassSubBean> mList;
	private AdapterProductGridViewClassSub mAdapter;
	private AdapterProductGridViewClassSub mListAdapter;
	private XRefreshView outViewGrid, outViewList;
	public static long lastRefreshTime;

	/**
	 * slidingmeun列表
	 */
	private ListView selectionList;

	private List<SaleAttributeNameVo> itemData;
	private GoodsAttrListAdapter mGoodsAttrListAdapter;

	/**
	 * 上个界面传值 是否直邮
	 */
	private String IsFreeShipping;
	/**
	 * 分类界面传值 分类id
	 */
	private String ProductCategoryId;
	/**
	 * 搜索界面传值 搜索内容
	 */
	private String U8SearchWord;

	/**
	 * 品牌界面传值 品牌ID
	 */
	private String BrandId;
	/**
	 * 国家ID
	 */
	private String SAValuesId;

	private String mBrand;
 	private MenuDrawer menuDrawer;
	private JSONObject searchCond;
	/**
	 * 返回国家数据
	 */
	private String countryResult;
	private String OrderBy = "0";

    @ViewInject(R.id.testRelative)
	private DrawerLayout mDrawerLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 		setContentView(R.layout.wj_act_product_list_new);

		x.view().inject(this);
		init();
		initgrid();
		initList();
		getProductList(0);
	}

	private String categoryId;

	/**
	 * 初始化控件
	 */
	private void init() {
		// 搜索内容
		String SearchWord = getIntent().getStringExtra("SearchWord");
		if (SearchWord == null || SearchWord.equals("")) {
			U8SearchWord = "";
		} else {
			try {
				U8SearchWord = URLEncoder.encode(SearchWord, "utf-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// 从分类里传递的值
		ProductCategoryId = getIntent().getStringExtra("ProductCategoryId");
		if (ProductCategoryId == null || ProductCategoryId.equals("")) {
			ProductCategoryId = "";
		}

		// 是否是直邮
		IsFreeShipping = getIntent().getStringExtra("IsFreeShipping");

		// 品牌ID
		BrandId = getIntent().getStringExtra("BrandId");
		if (BrandId == null || BrandId.equals("")) {
			BrandId = "";
		}

		mGridView = (GridView) findViewById(R.id.grid);
		mListView = (ListView) findViewById(R.id.list);
		outViewGrid = (XRefreshView) findViewById(R.id.custom_view_grid);
		outViewList = (XRefreshView) findViewById(R.id.custom_view_list);
		getCountryList();

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent i = new Intent();
				i.putExtra("IsFreeShipping", IsFreeShipping);
				i.putExtra("ProductSKUId", mList.get(arg2).getId());
				i.setClass(ActivityProductListNew.this, ActivityProductDetailsNew.class);
				startActivity(i);

			}
		});
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent i = new Intent();
				i.putExtra("IsFreeShipping", IsFreeShipping);
				i.putExtra("ProductSKUId", mList.get(arg2).getId());
				i.setClass(ActivityProductListNew.this, ActivityProductDetailsNew.class);
				startActivity(i);

			}
		});
	}

	/**
	 * 显示Slidingmeun(筛选)
	 */
	private void showSlidingMeun() {

		selectionList = (ListView) findViewById(R.id.selection_list);

		itemData = new ArrayList<SaleAttributeNameVo>();
		mGoodsAttrListAdapter = new GoodsAttrListAdapter(this, itemData);
		selectionList.setAdapter(mGoodsAttrListAdapter);

		refreshAttrs();

		// 重置的点击监听，将所有选项全设为false
		findViewById(R.id.filter_reset).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				for (int i = 0; i < itemData.size(); i++) {
					for (int j = 0; j < itemData.get(i).getSaleVo().size(); j++) {
						itemData.get(i).getSaleVo().get(j).setChecked(false);
					}
					itemData.get(i).getSaleVo().get(0).setChecked(true);
				}
				mGoodsAttrListAdapter.notifyDataSetChanged();
			}
		});
		// 确定的点击监听，将所有已选中项列出
		findViewById(R.id.filter_sure).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				for (int j = 0; j < itemData.get(0).getSaleVo().size(); j++) {
					if (itemData.get(0).getSaleVo().get(j).isChecked()) {
						// 选中的品牌
						String checkbrandid = itemData.get(0).getSaleVo().get(j).getGoodsAndValId();
						if (!checkbrandid.equals("0")) {
							BrandId = checkbrandid;
						}

					}
				}

				for (int j = 0; j < itemData.get(1).getSaleVo().size(); j++) {
					if (itemData.get(1).getSaleVo().get(j).isChecked()) {
						// 选中的国家
						String checkSAValuesId = itemData.get(1).getSaleVo().get(j).getGoodsAndValId();
						if (!checkSAValuesId.equals("0")) {
							SAValuesId = checkSAValuesId;
						} else {
							SAValuesId = "";
						}

					}
				}
				getProductList(2);
				mDrawerLayout.closeDrawers();
			}
		});

	}

	/**
	 * 刷新商品属性
	 *
	 * @throws JSONException
	 */
	public void refreshAttrs() {
		itemData.clear();
		try {
			SaleAttributeNameVo saleName = new SaleAttributeNameVo();
			saleName.setName("品牌");
			List<SaleAttributeVo> list = new ArrayList<SaleAttributeVo>();
			JSONObject jo = new JSONObject(mBrand);
			JSONArray array = jo.getJSONArray("Data");

			SaleAttributeVo voAll = new SaleAttributeVo();
			voAll.setValue("全部");
			voAll.setGoodsAndValId("0");
			voAll.setChecked(true);
			list.add(voAll);
			for (int j = 0; j < array.length(); j++) {
				JSONObject object = array.getJSONObject(j);
				SaleAttributeVo vo = new SaleAttributeVo();
				vo.setGoods("00");
				vo.setValue(object.getString("BrandName"));
				vo.setGoodsAndValId(object.getString("BrandId"));
				vo.setChecked(false);
				list.add(vo);
			}
			saleName.setSaleVo(list);
			// 是否展开
			saleName.setNameIsChecked(false);
			itemData.add(saleName);

			SaleAttributeNameVo saleCountry = new SaleAttributeNameVo();
			saleCountry.setName("国家");
			List<SaleAttributeVo> listCountry = new ArrayList<SaleAttributeVo>();
			JSONObject jo1 = new JSONObject(countryResult);
			JSONArray array1 = jo1.getJSONArray("Data");

			SaleAttributeVo mALl = new SaleAttributeVo();
			mALl.setGoods("00");
			mALl.setValue("全部");
			mALl.setGoodsAndValId("0");
			mALl.setChecked(true);
			listCountry.add(mALl);

			for (int j = 0; j < array1.length(); j++) {
				JSONObject object = array1.getJSONObject(j);
				SaleAttributeVo vo = new SaleAttributeVo();
				vo.setGoods("00");
				vo.setValue(object.getString("ValueStr"));
				vo.setGoodsAndValId(object.getString("ShopAttributeValuesId"));
				vo.setChecked(false);
				listCountry.add(vo);
			}
			saleCountry.setSaleVo(listCountry);
			// 是否展开
			saleCountry.setNameIsChecked(false);
			itemData.add(saleCountry);

			mGoodsAttrListAdapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initgrid() {
		outViewGrid.setPullLoadEnable(true);
//		outViewGrid.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		outViewGrid.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						outViewGrid.stopRefresh();
						lastRefreshTime = outViewGrid.getLastRefreshTime();
					}
				}, 2000);

			}

			@Override
			public void onLoadMore(boolean isSilence) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;
							getProductList(1);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mGridView.postDelayed(new Runnable() {
								@Override
								public void run() {
									showToast("没有更多了~");
									outViewGrid.stopLoadMore();
								}
							}, 500);
						}

					}
				}, 2000);
			}


			@Override
			public void onRelease(float direction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onHeaderMove(double offset, int offsetY) {

			}
		});
		outViewGrid.restoreLastRefreshTime(lastRefreshTime);
	}

	private void initList() {

		outViewList.setPullLoadEnable(true);
//		outViewList.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		outViewList.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						outViewList.stopRefresh();
						lastRefreshTime = outViewList.getLastRefreshTime();
					}
				}, 2000);

			}

			@Override
			public void onLoadMore(boolean isSilence) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;

							getProductList(1);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									showToast("没有更多了~");
									outViewList.stopLoadMore();
								}
							}, 500);
						}

					}
				}, 2000);
			}


			@Override
			public void onRelease(float direction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onHeaderMove(double offset, int offsetY) {

			}
		});
		outViewList.restoreLastRefreshTime(lastRefreshTime);

	}

	private void getProductList(final int id) {
		// Orderby 0-全部 1-高销量排序 2-低销量排序 3-高价格排序 4-低价格排序",
		try {
			searchCond = new JSONObject();
			searchCond.put("PName", StringUtils.isNull(U8SearchWord) ? "" : U8SearchWord);
			searchCond.put("Orderby", StringUtils.isNull(OrderBy) ? "" : OrderBy);
			searchCond.put("BrandId", StringUtils.isNull(BrandId) ? "" : BrandId);
			searchCond.put("CategoryId", StringUtils.isNull(ProductCategoryId) ? "" : ProductCategoryId);
			searchCond.put("SAValuesId", StringUtils.isNull(SAValuesId) ? "" : SAValuesId);
		} catch (JSONException e1) {
		}

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("MDUserId", getBindingShop());
		map.put("ZY", IsFreeShipping);
		map.put("PageIndex", String.valueOf(PageIndex));
		map.put("pageSize", "20");
		map.put("SearchCondition", searchCond.toString());

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

		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListCodeBpw_SearchList?MDUserId="
				+ getBindingShop() + "&ZY=" + IsFreeShipping + "&PageIndex=" + String.valueOf(PageIndex) + "&pageSize="
				+ "20" + "&SearchCondition=" + searchCond.toString() + "&Sign=" + Sign + "&Ts=" + Ts;

		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						findViewById(R.id.include_null).setVisibility(View.GONE);
						findViewById(R.id.ll_list).setVisibility(View.VISIBLE);

						parseList(result, id);
					} else {
						findViewById(R.id.include_null).setVisibility(View.VISIBLE);
						findViewById(R.id.ll_list).setVisibility(View.GONE);

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
					
					//起订量
					m.setNumber(data.getString("CustomerInitiaQuantity"));
					
					
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
					mList = dataList;
					mAdapter = new AdapterProductGridViewClassSub(mList, this, R.layout.wj_item_class_sub);
					mGridView.setAdapter(mAdapter);

					mListAdapter = new AdapterProductGridViewClassSub(mList, this, R.layout.wj_item_class_sub_list);
					mListView.setAdapter(mListAdapter);
					break;
				case 1:
					if (dataList != null && !dataList.isEmpty()) {
						mList.addAll(dataList);
						mAdapter.notifyDataSetChanged();
						mListAdapter.notifyDataSetChanged();
						outViewGrid.stopLoadMore();
						outViewList.stopLoadMore();
					}
					break;
				case 2:
					mList.clear();
					mList = dataList;
					mAdapter = new AdapterProductGridViewClassSub(mList, this, R.layout.wj_item_class_sub);
					mGridView.setAdapter(mAdapter);

					mListAdapter = new AdapterProductGridViewClassSub(mList, this, R.layout.wj_item_class_sub_list);
					mListView.setAdapter(mListAdapter);

					break;
				default:
					break;
				}

			} else {
				findViewById(R.id.include_null).setVisibility(View.VISIBLE);
				findViewById(R.id.ll_list).setVisibility(View.GONE);

			}

		} catch (Exception e) {

		}

	}

	private boolean saletype = true;
	private boolean pricetype = true;

	@Event(value = { R.id.btn_return, R.id._search, R.id.tv_layout, R.id.tv_comprehensive, R.id.tv_sales, R.id.tv_price,
			R.id.tv_sort })
	private void getEvent(View view) {
		switch (view.getId()) {
		case R.id.btn_return:
			finish();
			break;
		case R.id._search:
			Intent toSearch = new Intent();
			toSearch.putExtra("type", Integer.parseInt(IsFreeShipping));
			forward(ActivitySearch.class,toSearch);
			finish();
			break;
		case R.id.tv_layout:
			if (isGrid) {
				outViewGrid.setVisibility(View.VISIBLE);
				outViewList.setVisibility(View.GONE);
				mSwitchLayout.setBackgroundResource(R.drawable.ic_grid);
				isGrid = false;
			} else {
				outViewList.setVisibility(View.VISIBLE);
				outViewGrid.setVisibility(View.GONE);
				mSwitchLayout.setBackgroundResource(R.drawable.ic_list);
				isGrid = true;
			}
			break;

		case R.id.tv_comprehensive:

			OrderBy = "0";
			getProductList(2);
			setcolor(1);

			break;
		case R.id.tv_sales:
			setcolor(2);
			setTextViewDrawable(R.id.tv_price, 0);
			if (saletype) {
				// 高销量排序
				OrderBy = "1";
				getProductList(2);
				saletype = false;
				setTextViewDrawable(R.id.tv_sales, 2);
			} else {
				// 低销量排序
				OrderBy = "2";
				getProductList(2);
				saletype = true;
				setTextViewDrawable(R.id.tv_sales, 1);
			}
			break;

		case R.id.tv_price:
			setcolor(3);
			setTextViewDrawable(R.id.tv_sales, 0);
			if (pricetype) {
				// 高销量排序

				OrderBy = "3";
				getProductList(2);
				pricetype = false;
				setTextViewDrawable(R.id.tv_price, 2);
			} else {
				// 低销量排序
				OrderBy = "4";
				getProductList(2);
				pricetype = true;
				setTextViewDrawable(R.id.tv_price, 1);
			}
			break;

		case R.id.tv_sort:
			setcolor(4);
			mDrawerLayout.openDrawer(R.id.ll_right);
			break;

		default:
			break;
		}
	}

	/**
	 * 设置颜色
	 * 
	 * @param i
	 *            i= 1 综合 i= 2 销量 i= 3 价格 i= 4 筛选
	 */
	private void setcolor(int i) {
		switch (i) {
		case 1:
			setTextViewDrawable(R.id.tv_sales, 0);
			setTextViewDrawable(R.id.tv_price, 0);
			mComprehensive.setTextColor(getResources().getColor(R.color.main_color));
			mSales.setTextColor(getResources().getColor(R.color.bg_666666));
			mPrice.setTextColor(getResources().getColor(R.color.bg_666666));
			mSort.setTextColor(getResources().getColor(R.color.bg_666666));
			break;

		case 2:
			mComprehensive.setTextColor(getResources().getColor(R.color.bg_666666));
			mSales.setTextColor(getResources().getColor(R.color.main_color));
			mPrice.setTextColor(getResources().getColor(R.color.bg_666666));
			mSort.setTextColor(getResources().getColor(R.color.bg_666666));
			break;

		case 3:
			mComprehensive.setTextColor(getResources().getColor(R.color.bg_666666));
			mSales.setTextColor(getResources().getColor(R.color.bg_666666));
			mPrice.setTextColor(getResources().getColor(R.color.main_color));
			mSort.setTextColor(getResources().getColor(R.color.bg_666666));
			break;

		case 4:
			setTextViewDrawable(R.id.tv_sales, 0);
			setTextViewDrawable(R.id.tv_price, 0);
			mComprehensive.setTextColor(getResources().getColor(R.color.bg_666666));
			mSales.setTextColor(getResources().getColor(R.color.bg_666666));
			mPrice.setTextColor(getResources().getColor(R.color.bg_666666));
			mSort.setTextColor(getResources().getColor(R.color.main_color));
			break;

		default:
			break;
		}

	}

	/**
	 * 设置排序旁边的按钮
	 * 
	 * @param i
	 *            i=0 默认 i= 1显示向上 i=2显示向下
	 */
	private void setTextViewDrawable(int id, int i) {

		TextView tv = (TextView) findViewById(id);

		Drawable drawable = null;
		if (i == 1) {
			drawable = getResources().getDrawable(R.drawable.ic_sort_up);
		} else if (i == 2) {
			drawable = getResources().getDrawable(R.drawable.ic_sort_down);
		} else if (i == 0) {
			drawable = getResources().getDrawable(R.drawable.ic_sort_defult);
		}
		/// 这一步必须要做,否则不会显示.
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		tv.setCompoundDrawables(null, null, drawable, null);
	}

	/**
	 * 获取品牌列表
	 */
	private void getBrandList() {

		String url = Constants.WEBAPI_ADDRESS + "api/Brand/ListSearch_New?MdUserid=" + getBindingShop() + "&BrandID="
				+ BrandId + "&CodeID=" + ProductCategoryId + "&StrKey=" + U8SearchWord;

		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						mBrand = result;
						showSlidingMeun();
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
	 * 获取国家列表
	 */
	private void getCountryList() {
		if (getIntent().hasExtra("ccode")) {
			categoryId = getIntent().getStringExtra("ccode");
		} else {
			categoryId = ProductCategoryId;
		}

		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/GetSkuAttributesListSearch?MdUserid=" + getBindingShop()
				+ "&Code=" + categoryId + "&Brand=" + BrandId + "&StrKey=" + U8SearchWord;

		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						countryResult = result;
						getBrandList();
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

}
