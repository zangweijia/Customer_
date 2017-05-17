package com.bopinjia.customer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.BroadCastManager;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.CustomProgressDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ThreeClassificationProduct extends LazyFragment {

	private String ccode;
	private String type;

	private String isFreeShipping;
	private ListView mListView;
	private GridView mGridView;
	private List<ProductGridviewClassSubBean> mList;
	private AdapterProductGridViewClassSub mAdapter;
	private AdapterProductGridViewClassSub mListAdapter;
	private String shopId;
	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;
	private String mType = "0";

	private XRefreshView outViewGrid, outViewList;
	public static long lastRefreshTime;

	// 标志位，标志已经初始化完成。
	private boolean isPrepared;
	private static ThreeClassificationProduct newFragment;
	private CustomProgressDialog dialog;

	public static ThreeClassificationProduct newInstance(String ccode, String type, String isFreeShipping) {
		ThreeClassificationProduct newFragment = new ThreeClassificationProduct();
		Bundle bundle = new Bundle();
		bundle.putString("ccode", ccode);
		bundle.putString("type", type);
		bundle.putString("isFreeShipping", isFreeShipping);
		newFragment.setArguments(bundle);
		return newFragment;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.wj_fragment_class_sub, null);
		mGridView = (GridView) view.findViewById(R.id.grid);
		mListView = (ListView) view.findViewById(R.id.list);
		outViewGrid = (XRefreshView) view.findViewById(R.id.custom_view_grid);
		outViewList = (XRefreshView) view.findViewById(R.id.custom_view_list);
		isPrepared = true;
		lazyLoad();
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			ccode = args.getString("ccode");
			type = args.getString("type");
			isFreeShipping = args.getString("isFreeShipping");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		init();
	}

	private void init() {
		dialog = new CustomProgressDialog(getActivity(), "正在加载中", R.anim.frame);
		shopId = ((BaseActivity) getActivity()).getBindingShop();
		if (type.equals("1")) {
			outViewGrid.setVisibility(View.GONE);
			outViewList.setVisibility(View.VISIBLE);
		} else if (type.equals("2")) {
			outViewGrid.setVisibility(View.VISIBLE);
			outViewList.setVisibility(View.GONE);
		}
		initgrid();
		initList();
		getProductList(0, "0");
		try {
			// 注册布局切换广播
			IntentFilter filter = new IntentFilter();
			filter.addAction("fragment_layout");
			BroadCastManager.getInstance().registerReceiver(getActivity(), new LocalReceiver(), filter);// 注册广播接收者
			// 注册排序方式广播
			IntentFilter sortfilter = new IntentFilter();
			sortfilter.addAction("fragment_sort");
			BroadCastManager.getInstance().registerReceiver(getActivity(), new SortReceiver(), sortfilter);// 注册广播接收者
		} catch (Exception e) {
			e.printStackTrace();
		}

		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent i = new Intent();
				i.putExtra("IsFreeShipping", isFreeShipping);
				i.putExtra("ProductSKUId", mList.get(arg2).getId());
				i.setClass(getActivity(), ActivityProductDetailsNew.class);
				startActivity(i);

			}
		});
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent i = new Intent();
				i.putExtra("IsFreeShipping", isFreeShipping);
				i.putExtra("ProductSKUId", mList.get(arg2).getId());
				i.setClass(getActivity(), ActivityProductDetailsNew.class);
				startActivity(i);
			}
		});

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
				}, 500);

			}

			@Override
			public void onLoadMore(boolean isSilence) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;
							getProductList(1, mType);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mGridView.postDelayed(new Runnable() {
								@Override
								public void run() {
									((BaseActivity) getActivity()).showToast("没有更多了~");
									outViewGrid.stopLoadMore();
								}
							}, 0);
						}

					}
				}, 0);
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
				}, 500);

			}

			@Override
			public void onLoadMore(boolean isSilence) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;

							getProductList(1, mType);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									((BaseActivity) getActivity()).showToast("没有更多了~");
									outViewList.stopLoadMore();
								}
							}, 0);
						}

					}
				}, 0);
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

	class LocalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mType = intent.getStringExtra("type");
			if (mType.equals("1")) {
				outViewGrid.setVisibility(View.GONE);
				outViewList.setVisibility(View.VISIBLE);
			} else if (mType.equals("2")) {
				outViewGrid.setVisibility(View.VISIBLE);
				outViewList.setVisibility(View.GONE);
			}
		}

	}

	class SortReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String sort = intent.getStringExtra("sort");
			mType = sort;
			PageIndex = 1;
			getProductList(2, mType);

		}
	}

	private void getProductList(final int id, String OrderBy) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", shopId);
		map.put("WhereCode", ccode);
		map.put("OrderBy", OrderBy);
		map.put("ZY", isFreeShipping);
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
		//dialog.show();
		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListCodeBpw_New?UserId=" + shopId + "&WhereCode="
				+ ccode + "&OrderBy=" + OrderBy + "&ZY=" + isFreeShipping + "&PageIndex=" + String.valueOf(PageIndex)
				+ "&pageSize=" + "20" + "&Sign=" + Sign + "&Ts=" + Ts;

		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						dialog.dismiss();
						parseList(result, id);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
				dialog.dismiss();
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

					m.setIsfexiao(data.getString("BCP_IsFX"));
					m.setCommissionPrice(data.getString("CommissionPrice"));

					m.setImg(data.getString("ProductThumbnail"));
					m.setMarketprice(data.getString("MarketPrice"));
					m.setIsshiping(isFreeShipping);
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
					mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
					mGridView.setAdapter(mAdapter);

					mListAdapter = new AdapterProductGridViewClassSub(mList, getActivity(),
							R.layout.wj_item_class_sub_list);
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
					mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
					mGridView.setAdapter(mAdapter);

					mListAdapter = new AdapterProductGridViewClassSub(mList, getActivity(),
							R.layout.wj_item_class_sub_list);
					mListView.setAdapter(mListAdapter);

					break;
				default:
					break;
				}

			} else {
				// Toast.makeText(getActivity(), "没有更多商品了", 0).show();
			}

		} catch (Exception e) {

		}

	}

	@Override
	protected void lazyLoad() {
		if (!isPrepared || !isVisible) {
			return;
		} else {
			init();
		}
	}

}
