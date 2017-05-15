package com.bopinjia.customer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
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

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityFXOrder extends BaseActivity {

	private List<JSONObject> mOrderList;
	private OrderListAdapter mOrderListAdapter;
	private ListView mLstOrder;
	private LinearLayout lv;
	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;
	private String mCurrStatus;

	private Dialog mDialog;
	private View dialogView;
	public static ActivityFXOrder instance = null;

	/**
	 * 刷新控件
	 */
	private XRefreshView outView;
	public static long lastRefreshTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_my_commission_oder);
		instance = this;
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);

		// 状态按钮
		findViewById(R.id.btn_all).setOnClickListener(this);
		findViewById(R.id.btn_unpaid).setOnClickListener(this);
		findViewById(R.id.btn_unshipping).setOnClickListener(this);
		findViewById(R.id.btn_unreceiving).setOnClickListener(this);
		findViewById(R.id.btn_complete).setOnClickListener(this);
		lv = (LinearLayout) findViewById(R.id.data_null);
		findViewById(R.id.tv_go_new_product).setOnClickListener(this);

		mLstOrder = (ListView) findViewById(R.id.lst_order);
		setMenuStatus("0");

		init();

	}

	private void init() {
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

						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;

							GetOrderList(1);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mLstOrder.postDelayed(new Runnable() {
								@Override
								public void run() {
									showToast("没有更多了~");
									outView.stopLoadMore();
								}
							}, 500);
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

	public void tab5() {
		setMenuStatus(Constants.ORDER_STATUS_COMPLETE);
		GetOrderList(3);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		GetOrderList(0);
	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		switch (viewId) {
		case R.id.tv_go_new_product:
			// 没有商品回首页
			ActivityHome.instance.finish();
			forward(ActivityHome.class);
			finish();
			break;
		case R.id.btn_return:
			// 返回
			finish();
			break;
		case R.id.btn_all:
			setMenuStatus(Constants.ORDER_STATUS_ALL);
			GetOrderList(3);
			break;
		case R.id.btn_unpaid:
			setMenuStatus(Constants.ORDER_STATUS_UNPAID);
			GetOrderList(3);
			break;
		case R.id.btn_unshipping:
			setMenuStatus(Constants.ORDER_STATUS_UNSHIPPING);
			GetOrderList(3);
			break;
		case R.id.btn_unreceiving:
			setMenuStatus(Constants.ORDER_STATUS_UNRECEIVING);
			GetOrderList(3);
			break;
		case R.id.btn_complete:
			setMenuStatus(Constants.ORDER_STATUS_COMPLETE);
			GetOrderList(3);
			break;
		default:
			break;
		}
	}

	/**
	 * 设置TAB的状态
	 * 
	 * @param status
	 *            状态
	 */
	private void setMenuStatus(String status) {
		this.mCurrStatus = status;
		this.PageIndex = 1;
		((TextView) findViewById(R.id.txt_all)).setTextColor(getResources()
				.getColor(Constants.ORDER_STATUS_ALL.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_all)
				.setVisibility(Constants.ORDER_STATUS_ALL.equals(status) ? View.VISIBLE : View.INVISIBLE);

		((TextView) findViewById(R.id.txt_unpaid)).setTextColor(getResources()
				.getColor(Constants.ORDER_STATUS_UNPAID.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_unpaid)
				.setVisibility(Constants.ORDER_STATUS_UNPAID.equals(status) ? View.VISIBLE : View.INVISIBLE);

		((TextView) findViewById(R.id.txt_unshipping)).setTextColor(getResources()
				.getColor(Constants.ORDER_STATUS_UNSHIPPING.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_unshipping)
				.setVisibility(Constants.ORDER_STATUS_UNSHIPPING.equals(status) ? View.VISIBLE : View.INVISIBLE);

		((TextView) findViewById(R.id.txt_unreceiving)).setTextColor(getResources()
				.getColor(Constants.ORDER_STATUS_UNRECEIVING.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_unreceiving)
				.setVisibility(Constants.ORDER_STATUS_UNRECEIVING.equals(status) ? View.VISIBLE : View.INVISIBLE);

		((TextView) findViewById(R.id.txt_complete)).setTextColor(getResources()
				.getColor(Constants.ORDER_STATUS_COMPLETE.equals(status) ? R.color.main_color : R.color.txt_black));
		findViewById(R.id.line_bottom_complete)
				.setVisibility(Constants.ORDER_STATUS_COMPLETE.equals(status) ? View.VISIBLE : View.INVISIBLE);
	}

	/**
	 * 订单列表适配器
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
				final JSONObject model = mOrderList.get(position).getJSONObject("order");
				// 订单号 、id、创建时间 、订单类型
				viewItem.setOrder(model.getString("OrderSn"), model.getString("OrderId"), model.getString("CreateTime"),
						model.getString("Otype"));
				// 状态
				viewItem.setStatus(model.getString("OrderStatus"), model.getString("PayStatus"),
						model.getString("RetractableCount"));
				// 件数
				viewItem.setCount(model.getString("ProductCount"));
				// 金额
				viewItem.setAmount(model.getString("OrderAmount"));
				// 佣金
				viewItem.setCommissionfee(model.getString("GDSUserBrokerageTotal"));
				// 设置商品列表
				viewItem.setProductsList(mOrderList.get(position).getJSONArray("orderProduct"));

			} catch (Exception e) {
				// 系统异常
				showSysErr(e);
			}
			return viewItem;
		}
	}

	/**
	 * 销售列表控件
	 */
	class OrderListItem extends LinearLayout {

		private String mOrderSn;
		private String mOrderStatus;
		private String mPayStatus;
		private String mOrderId;
		private String ProductSKUName;
		private String amount;
		private String mCreateTime;
		private String Otype;

		public OrderListItem(Context context) {
			super(context);
			View.inflate(getContext(), R.layout.item_order, this);
			this.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("OrderId", mOrderId);
					intent.putExtra("OrderSn", mOrderSn);
					intent.putExtra("OrderStatus", mOrderStatus);
					intent.putExtra("Otype", Otype);
					intent.putExtra("CurrStatus", mCurrStatus);
					forward(ActivityFXOrderDetails.class, intent);
				}
			});

			this.findViewById(R.id.complete_order_status).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 跳转到物流详情
					Intent intent = new Intent();
					intent.putExtra("Otype", Otype);
					intent.putExtra("OrderId", mOrderId);
					forward(ActivityOrderStateList.class, intent);
				}
			});
		}

		// 设置状态
		public void setStatus(String orderStatus, String payStatus, String state) {
			this.mOrderStatus = orderStatus;
			this.mPayStatus = payStatus;
			int statusDispId = -1;
			// 支付状态是未付款
			// 支付状态是未付款
			if ("0".equals(payStatus)) {
				if ("2".equals(orderStatus)) {
					// 已取消
					statusDispId = R.string.txt_order_canceled;
				} else if ("3".equals(orderStatus)) {
					// 已关闭
					statusDispId = R.string.txt_order_close;
				} else {
					// 待付款
					statusDispId = R.string.txt_order_unpaid;
				}

			} else {
				// 支付状态是已付款
				if ("1".equals(payStatus)) {
					// 订单状态是未确认
					if ("0".equals(orderStatus)) {
						// 待发货
						statusDispId = R.string.txt_order_unshipping;

					} else if ("1".equals(orderStatus)) {
						// 待发货
						statusDispId = R.string.txt_order_unshipping;

					} else if ("4".equals(orderStatus)) {
						// 订单状态是确认
						// 待收货
						statusDispId = R.string.txt_order_unreceiving;

					} else if ("6".equals(orderStatus)) {
						// 订单状态是已完成
						// 已完成
						statusDispId = R.string.txt_order_complete;

					} else if ("5".equals(orderStatus)) {
						// 已退货
						statusDispId = R.string.txt_order_close;

					} else if ("3".equals(orderStatus)) {
						// 已关闭
						statusDispId = R.string.txt_order_close;

					} else if ("2".equals(orderStatus)) {
						// 已取消
						statusDispId = R.string.txt_order_canceled;

					} else if ("7".equals(orderStatus)) {

						statusDispId = R.string.txt_order_daituikuan;
					} else if ("999".equals(orderStatus)) {
						// 待退款
						statusDispId = R.string.txt_order_daituikuan;

					}

				}
			}

			findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
			findViewById(R.id.complete_order_status).setVisibility(View.VISIBLE);
			findViewById(R.id.btn_delete).setVisibility(View.GONE);

			if (statusDispId != -1) {
				((TextView) findViewById(R.id.txt_status)).setText(statusDispId);
			}

		}

		// 设置订单号 ，时间,
		public void setOrder(String orderSn, String orderId, String time, String Otype) {
			this.mOrderSn = orderSn;
			this.mOrderId = orderId;
			this.mCreateTime = time;
			this.Otype = Otype;
			((TextView) findViewById(R.id.txt_order_no)).setText(orderSn);
		}

		// 设置件数
		public void setCount(String count) {
			String formatCount = MessageFormat.format(getString(R.string.txt_order_count), count);
			((TextView) findViewById(R.id.txt_count)).setText(formatCount);
		}

		// 设置金额
		public void setAmount(String amount) {
			this.amount = amount;
			double dbAmount = Double.parseDouble(amount);
			DecimalFormat df = new DecimalFormat("###,##0.00");
			((TextView) findViewById(R.id.txt_amount)).setText("合计：¥" + df.format(dbAmount));
		}

		// 设置佣金
		public void setCommissionfee(String shipfee) {

			String ss = "佣金：" + shipfee;
			SpannableStringBuilder builder = new SpannableStringBuilder(ss);
			// ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色

			ForegroundColorSpan blackSpan = new ForegroundColorSpan(getResources().getColor(R.color.main_color));

			builder.setSpan(blackSpan, 3, ss.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			((TextView) findViewById(R.id.txt_shipfee)).setText(builder);

		}

		// 设置商品列表
		public void setProductsList(JSONArray products) {
			try {
				LinearLayout lstProducts = (LinearLayout) findViewById(R.id.lst_product);

				for (int i = 0; i < products.length(); i++) {
					final JSONObject product = products.getJSONObject(i);

					LinearLayout productView = new LinearLayout(getContext());
					View.inflate(getContext(), R.layout.wj_item_comision_product, productView);

					// 商品名称
					((TextView) productView.findViewById(R.id.txt_product_name))
							.setText(product.getString("ProductSKUName"));
					// 单价
					((TextView) productView.findViewById(R.id.txt_price))
							.setText("单价：¥" + product.getString("ProductPrice"));

					if (product.getString("GDSUserBrokerage").equals("0")) {
						((TextView) productView.findViewById(R.id.tv_commission_price)).setVisibility(View.GONE);
						((TextView) productView.findViewById(R.id.tv_yj)).setVisibility(View.GONE);

					} else {
						((TextView) productView.findViewById(R.id.tv_commission_price)).setVisibility(View.VISIBLE);
						((TextView) productView.findViewById(R.id.tv_yj)).setVisibility(View.VISIBLE);
						// 佣金
						((TextView) productView.findViewById(R.id.tv_commission_price))
								.setText(product.getString("GDSUserBrokerage"));
					}

					// 商品数量
					((TextView) productView.findViewById(R.id.txt_count)).setText("x" + product.getString("BuyCount"));

					ImageView iv = (ImageView) productView.findViewById(R.id.iv_product_thumbnails);

					ImageOptions imageOptions = new ImageOptions.Builder()
							.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
							.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
							.build();

					x.image().bind(iv, product.getString("ThumbnailsUrl"), imageOptions);

					LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
					params.weight = 1;

					productView.setLayoutParams(params);

					lstProducts.addView(productView);

					if (i < products.length() - 1) {
						// 分割线
						View view = new View(getContext());
						view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
								(int) getResources().getDimension(R.dimen.divider_line1)));
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

	/**
	 * 获取订单列表
	 */
	private void GetOrderList(final int id) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("StateId", mCurrStatus);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Order/List_GDS?UserId=" + getLoginUserId() + "&StateId="
				+ mCurrStatus + "&PageIndex=" + String.valueOf(PageIndex) + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetOrderListCallBack(), id, null,this);

	}

	class GetOrderListCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					lv.setVisibility(View.GONE);
					mLstOrder.setVisibility(View.VISIBLE);

					parse(id, result);
				} else if (jsonresult.equals("2")) {
					mLstOrder.setVisibility(View.GONE);
					lv.setVisibility(View.VISIBLE);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	private void parse(int id, String result) {
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
					mOrderList = dataList;
					mOrderListAdapter = new OrderListAdapter(ActivityFXOrder.this);
					mLstOrder.setAdapter(mOrderListAdapter);

					break;
				case 1:
					// 上拉加载更多时
					if (dataList != null && !dataList.isEmpty()) {
						mOrderList.addAll(dataList);
						mOrderListAdapter.notifyDataSetChanged();
					}
					outView.stopLoadMore();
					break;
				case 3:
					// 画面初期化时 的检索
					mOrderList = dataList;
					mOrderListAdapter = new OrderListAdapter(this);
					mLstOrder.setAdapter(mOrderListAdapter);
					mOrderListAdapter.notifyDataSetChanged();

					break;
				default:
					break;
				}
			} else {
				mOrderList = new ArrayList<JSONObject>();
				mOrderListAdapter = new OrderListAdapter(this);
				mLstOrder.setAdapter(mOrderListAdapter);
			}

		} catch (Exception e) {
			showSysErr(e);
		}

	}

}
