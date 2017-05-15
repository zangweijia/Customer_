package com.bopinjia.customer.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
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

public class ActivityFXClientDetails extends BaseActivity {

	private TextView mTiTleBack;
	private TextView mTiTleName;
	private ListView mListClient;

	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;
	private String mCurrStatus;

	/**
	 * 刷新控件
	 */
	private XRefreshView outView;
	public static long lastRefreshTime;

	private List<JSONObject> mOrderList;
	private OrderListAdapter mOrderListAdapter;
	private String Clientid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_my_client_details);
		setTitle();
		init();
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTiTleName.setText("客户详情");
		mTiTleBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void init() {
		Clientid = getIntent().getStringExtra("id");
		String img = getIntent().getStringExtra("img");
		String phone = getIntent().getStringExtra("phone");
		String name = getIntent().getStringExtra("name");

		TextView mPhone = (TextView) findViewById(R.id.tv_phone);
		mPhone.setText(phone);
		TextView mName = (TextView) findViewById(R.id.tv_client_name);
		mName.setText(name);

		mListClient = (ListView) findViewById(R.id.lst_my_client);

		ImageView iv = (ImageView) findViewById(R.id.iv_head);
		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setCircular(true).setCrop(true).build();
		x.image().bind(iv, img, imageOptions);
		GetOrderList(0);

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

							mListClient.postDelayed(new Runnable() {
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
			}

			@Override
			public void onHeaderMove(double offset, int offsetY) {

			}
		});
		outView.restoreLastRefreshTime(lastRefreshTime);
	}

	private void GetOrderList(final int id) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", Clientid);
		map.put("PageIndex", String.valueOf(PageIndex));
		map.put("GDSUserId", getLoginUserId());
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

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/List_GDSUserDetail?UserId=" + Clientid + "&GDSUserId="
				+ getLoginUserId() + "&PageIndex=" + String.valueOf(PageIndex) + "&Sign=" + Sign + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetOrderListCallBack(), id, null,this);

	}

	class GetOrderListCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parse(id, result);
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
					mOrderListAdapter = new OrderListAdapter(ActivityFXClientDetails.this);
					mListClient.setAdapter(mOrderListAdapter);

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
					mListClient.setAdapter(mOrderListAdapter);
					mOrderListAdapter.notifyDataSetChanged();

					break;
				default:
					break;
				}
			} else {
				mOrderList = new ArrayList<JSONObject>();
				mOrderListAdapter = new OrderListAdapter(this);
				mListClient.setAdapter(mOrderListAdapter);
			}

		} catch (Exception e) {
			showSysErr(e);
		}

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

			// findViewById(R.id.ll_complete).setVisibility(View.INVISIBLE);
			// findViewById(R.id.complete_order_status).setVisibility(View.INVISIBLE);
			// findViewById(R.id.btn_delete).setVisibility(View.GONE);

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

}
