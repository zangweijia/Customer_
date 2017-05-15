package com.bopinjia.customer.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityRetractList extends BaseActivity {

	/**
	 * 退货列表
	 */
	private List<JSONObject> mRetractList;
	/**
	 * 退货列表适配器
	 */
	private RetractListAdapter mRetractListAdapter;
	private ListView mLstRetract;

	/**
	 * 退款订单
	 */
	private List<JSONObject> mOrderList;
	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages, mTkAllPages;
	private LinearLayout lv;

	/**
	 * 刷新控件
	 */
	private XRefreshView outView;
	public static long lastRefreshTime;

	private String mSelectedMsgId = "0";
	private static final String SelectedTK = "0";
	private static final String SelectedTH = "1";

	private OrderListAdapter mOrderListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_retract_list);

		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 退款
		findViewById(R.id.ll_tk).setOnClickListener(this);
		// 退货
		findViewById(R.id.ll_th).setOnClickListener(this);
		lv = (LinearLayout) findViewById(R.id.data_null);
		mLstRetract = (ListView) findViewById(R.id.lst_retract);

		// GetRetractList(0);
		GetTKOrderList(0);

		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
//		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		outView.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						outView.stopRefresh();
						lastRefreshTime = outView.getLastRefreshTime();
					}
				}, 2000);

			}

			@Override
			public void onLoadMore(boolean isSilence) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {

						if (mSelectedMsgId.equals("0")) {
							// 退款上拉刷新
							if (PageIndex < Integer.parseInt(mTkAllPages)) {
								PageIndex += 1;
								GetTKOrderList(1);
							} else if (PageIndex >= Integer.parseInt(mTkAllPages)) {

								mLstRetract.postDelayed(new Runnable() {
									@Override
									public void run() {
										showToast("没有更多了~");
										outView.stopLoadMore();
									}
								}, 500);
							}
						} else if (mSelectedMsgId.equals("1")) {
							// 退货上拉刷新
							if (PageIndex < Integer.parseInt(mAllPages)) {
								PageIndex += 1;
								GetRetractList(1);
							} else if (PageIndex >= Integer.parseInt(mAllPages)) {

								mLstRetract.postDelayed(new Runnable() {
									@Override
									public void run() {
										showToast("没有更多了~");
										outView.stopLoadMore();
									}
								}, 500);
							}
						}
					}
				}, 1500);
			}


			@Override
			public void onRelease(float direction) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onHeaderMove(double offset, int offsetY) {

			}

		});
		outView.restoreLastRefreshTime(lastRefreshTime);
	}

	/**
	 * 设置TAB的状态
	 * 
	 * @param status
	 *            状态
	 */
	private void setMenuStatus(String status) {
		this.mSelectedMsgId = status;
		// 退款
		((TextView) findViewById(R.id.txt_return_money)).setTextColor(
				getResources().getColor(SelectedTK.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_product).setVisibility(SelectedTK.equals(status) ? View.VISIBLE : View.INVISIBLE);

		// 退货
		((TextView) findViewById(R.id.txt_return_goods)).setTextColor(
				getResources().getColor(SelectedTH.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_shop).setVisibility(SelectedTH.equals(status) ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		switch (viewId) {
		case R.id.btn_return:
			// 返回
			finish();
			break;
		case R.id.ll_tk:
			setMenuStatus("0");
			PageIndex = 1;
			GetTKOrderList(0);
			break;
		case R.id.ll_th:
			setMenuStatus("1");
			PageIndex = 1;
			GetRetractList(0);
			break;
		default:
			break;
		}
	}

	/**
	 * 退款列表适配器
	 */
	class RetractListAdapter extends BaseAdapter {

		private final Context mContext;

		public RetractListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return mRetractList.size();
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
			RetractListItem viewItem = new RetractListItem(mContext);
			try {
				// 数据模型
				final JSONObject model = mRetractList.get(position);

				// 订单号
				viewItem.setOrderSn(model.getString("OrderSn"));
				// 状态
				viewItem.setStatus(model.getString("Status"));
				// 交易金额
				viewItem.setDealAmount(model.getString("ApplyReturnMoney"));
				// 退款金额
				viewItem.setReturnAmount(model.getString("ReturnMoney"));
				// 设置商品列表
				viewItem.setProduct(model);

				// LayoutParams layoutParams = new
				// LayoutParams(LayoutParams.MATCH_PARENT,
				// parent.getMeasuredHeight() / 4);
				// viewItem.setLayoutParams(layoutParams);

			} catch (Exception e) {
				// 系统异常
				showSysErr(e);
			}
			return viewItem;
		}
	}

	/**
	 * 退货列表控件
	 */
	class RetractListItem extends LinearLayout {

		private String mData;

		public RetractListItem(Context context) {
			super(context);
			View.inflate(getContext(), R.layout.item_retract, this);

		}

		// 设置状态
		public void setStatus(String retractStatus) {
			if ("1".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText(R.string.txt_retract_reviewing);
			} else if ("2".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText("待退款");
			} else if ("3".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText(R.string.txt_retract_success);
			} else if ("9".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText("已拒绝");
			}
		}

		// 设置订单号
		public void setOrderSn(String orderSn) {
			((TextView) findViewById(R.id.txt_order_no)).setText(orderSn);
		}

		// 设置交易金额
		public void setDealAmount(String dealAmount) {
			((TextView) findViewById(R.id.txt_req_amount)).setText("¥" + dealAmount);
		}

		// 设置退款金额
		public void setReturnAmount(String returnAmount) {
			((TextView) findViewById(R.id.txt_amount)).setText("¥" + returnAmount);
		}

		// 设置商品列表
		public void setProduct(JSONObject product) {
			try {
				mData = product.toString();
				((TextView) findViewById(R.id.txt_product_name)).setText(product.getString("ProductSKUName"));
				((TextView) findViewById(R.id.txt_price)).setText("¥" + product.getString("MarketPrice"));
				((TextView) findViewById(R.id.txt_count)).setText("x" + product.getString("ReturnCount"));
				((TextView) findViewById(R.id.phone)).setText(product.getString("ShopMobile"));
				setImageFromUrl(product.getString("ProductThumbnail"),
						(ImageView) findViewById(R.id.iv_product_thumbnails));
			} catch (Exception e) {
				// 系统异常
				showSysErr(e);
			}
		}
	}

	/**
	 * 获取退货列表
	 */
	private void GetRetractList(final int id) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("PageIndex", String.valueOf(PageIndex));
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

		String url = Constants.WEBAPI_ADDRESS + "api/ReturnOrder/List?UserId=" + getLoginUserId() + "&PageIndex="
				+ String.valueOf(PageIndex) + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetRetractListCallBack(), id, null, this);
	}

	class GetRetractListCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					lv.setVisibility(View.GONE);
					mLstRetract.setVisibility(View.VISIBLE);
					parseth(id, result);
				} else if (jsonresult.equals("2")) {
					mLstRetract.setVisibility(View.GONE);
					lv.setVisibility(View.VISIBLE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 退货列表 解析
	 * 
	 * @param id
	 * @param result
	 */
	private void parseth(int id, String result) {
		try {
			JSONObject jo = new JSONObject(result);
			JSONObject jdata = jo.getJSONObject("Data");
			JSONObject Paging = jdata.getJSONObject("Paging");
			JSONArray dataArray = jdata.getJSONArray("Records");
			mAllPages = Paging.getString("Pages");

			if (dataArray != null && dataArray.length() > 0) {
				List<JSONObject> dataList = new ArrayList<JSONObject>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);

					dataList.add(data);
				}

				switch (id) {
				case 0:
					// 画面初期化时 的检索
					mRetractList = dataList;
					mRetractListAdapter = new RetractListAdapter(this);
					mLstRetract.setAdapter(mRetractListAdapter);
					break;
				case 1:
					// 上拉加载更多时
					if (dataList != null && !dataList.isEmpty()) {
						mRetractList.addAll(dataList);
						mRetractListAdapter.notifyDataSetChanged();

					}
					break;
				default:
					break;
				}
			} else {
				if (id == 1) {
				}
			}

		} catch (Exception e) {
			showSysErr(e);
		}
	}

	/**
	 * tk列表适配器
	 *
	 */
	class OrderListAdapter extends BaseAdapter {

		private final Context mContext;

		public OrderListAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return mOrderList.size();
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
			OrderListItem viewItem = new OrderListItem(mContext);
			try {
				// 数据模型
				final JSONObject model = mOrderList.get(position).getJSONObject("ReturnOrder");
				viewItem.setOrderSn(model.getString("ReturnOrderSn"));
				// 状态
				viewItem.setStatus(model.getString("Status"));
				// 交易金额
				viewItem.setDealAmount(model.getString("ApplyReturnMoney"));
				// 退款金额
				viewItem.setReturnAmount(model.getString("ReturnMoney"));

				viewItem.setPhone(model.getString("ShopMobile"));

				// 设置商品列表
				viewItem.setProductsList(mOrderList.get(position).getJSONArray("ReturnOrderProduct"));

			} catch (Exception e) {
				// 系统异常
				showSysErr(e);
			}
			return viewItem;
		}
	}

	/**
	 * 退款列表控件
	 */
	class OrderListItem extends LinearLayout {

		public OrderListItem(Context context) {
			super(context);
			View.inflate(getContext(), R.layout.wj_item_tk, this);
		}

		// 设置状态
		public void setStatus(String retractStatus) {
			if ("1".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText(R.string.txt_retract_reviewing);
			} else if ("2".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText("待退款");
			} else if ("3".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText(R.string.txt_retract_success);
			} else if ("9".equals(retractStatus)) {
				((TextView) findViewById(R.id.txt_status)).setText("已拒绝");
			}
		}

		// 设置订单号
		public void setOrderSn(String orderSn) {
			((TextView) findViewById(R.id.txt_order_no)).setText(orderSn);
		}

		// 设置商家电话
		public void setPhone(String phone) {
			((TextView) findViewById(R.id.phone)).setText(phone);
		}

		// 设置交易金额
		public void setDealAmount(String dealAmount) {
			((TextView) findViewById(R.id.txt_req_amount)).setText("¥" + dealAmount);
		}

		// 设置退款金额
		public void setReturnAmount(String returnAmount) {
			((TextView) findViewById(R.id.txt_amount)).setText("¥" + returnAmount);
		}

		// 设置商品列表
		public void setProductsList(JSONArray products) {
			try {
				LinearLayout lstProducts = (LinearLayout) findViewById(R.id.lst_product);

				for (int i = 0; i < products.length(); i++) {
					final JSONObject product = products.getJSONObject(i);

					LinearLayout productView = new LinearLayout(getContext());
					View.inflate(getContext(), R.layout.item_order_product2, productView);
					((TextView) productView.findViewById(R.id.txt_product_name))
							.setText(product.getString("ProductSKUName"));
					((TextView) productView.findViewById(R.id.txt_product_tax)).setText(MessageFormat
							.format(getString(R.string.txt_cart_product_tax), product.getString("ProductTaxMoney")));
					((TextView) productView.findViewById(R.id.txt_price))
							.setText("￥" + product.getString("ProductPrice"));
					((TextView) productView.findViewById(R.id.txt_count)).setText("x" + product.getString("BuyCount"));

					ImageOptions imageOptions = new ImageOptions.Builder()
							.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
							.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
							.build();

					x.image().bind((ImageView) productView.findViewById(R.id.iv_product_thumbnails),
							product.getString("ThumbnailsUrl"), imageOptions);

					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
					params.weight = 1;

					productView.setLayoutParams(params);

					productView
							.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, mLstRetract.getHeight() / 6));

					lstProducts.addView(productView);

					if (i < products.length() - 1) {
						// 分割线
						View view = new View(getContext());
						view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
								(int) getResources().getDimension(R.dimen.divider_line)));
						view.setBackgroundColor(getResources().getColor(R.color.bg_divider_line_minus));

						lstProducts.addView(view);
					}
				}
			} catch (Exception e) {
				// 系统异常
				showSysErr(e);
			}
		}
	}

	private void GetTKOrderList(final int id) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("PageIndex", String.valueOf(PageIndex));
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

		String url = Constants.WEBAPI_ADDRESS + "api/ReturnOrderMoney/List?UserId=" + getLoginUserId() + "&PageIndex="
				+ String.valueOf(PageIndex) + "&PageSize=" + "50" + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetTKOrderListCallBack(), id, null, this);

	}

	class GetTKOrderListCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					lv.setVisibility(View.GONE);
					mLstRetract.setVisibility(View.VISIBLE);
					parsetk(id, result);
				} else if (jsonresult.equals("2")) {
					mLstRetract.setVisibility(View.GONE);
					lv.setVisibility(View.VISIBLE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void parsetk(int id, String result) {
		try {
			JSONObject jo = new JSONObject(result);
			JSONObject jdata = jo.getJSONObject("Data");
			JSONObject Paging = jdata.getJSONObject("Paging");
			JSONArray dataArray = jdata.getJSONArray("Records");
			mTkAllPages = Paging.getString("Pages");

			if (dataArray != null && dataArray.length() > 0) {
				List<JSONObject> dataList = new ArrayList<JSONObject>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					dataList.add(data);
				}
				switch (id) {
				case 0:
					// 画面初期化时 的检索
					mOrderList = dataList;
					mOrderListAdapter = new OrderListAdapter(ActivityRetractList.this);
					mLstRetract.setAdapter(mOrderListAdapter);

					break;
				case 1:
					// 上拉加载更多时
					if (dataList != null && !dataList.isEmpty()) {
						mOrderList.addAll(dataList);
						mOrderListAdapter.notifyDataSetChanged();
					}
					break;
				case 3:
					// 画面初期化时 的检索
					mOrderList = dataList;
					mOrderListAdapter = new OrderListAdapter(this);
					mLstRetract.setAdapter(mOrderListAdapter);
					mOrderListAdapter.notifyDataSetChanged();

					break;
				default:
					break;
				}
			} else {
				mOrderList = new ArrayList<JSONObject>();
				mOrderListAdapter = new OrderListAdapter(this);
				mLstRetract.setAdapter(mOrderListAdapter);
			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

}
