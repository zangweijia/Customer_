package com.bopinjia.customer.activity;

import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityFXOrderDetails extends BaseActivity {

	private String mOrderId;
	private String mOrderSn;

	private JSONObject mDivisionData;

	private String orderAmount;
	private String ProductSKUName;
	private String mCreateTime;
	boolean reduce = false;
	private String mOtype, mCurrStatus;
	private Dialog mDialog;
	private View dialogView;
	private Button confirmBt;
	private Button cancelBt;

	/**
	 * 判断是否是直邮现货自营
	 */
	private String IsFreeShipping;
	private String shopPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_my_comision_order_details);

		mCurrStatus = getIntent().getStringExtra("mCurrStatus");
		mOrderId = getIntent().getStringExtra("OrderId");
		mOrderSn = getIntent().getStringExtra("OrderSn");
		mOtype = getIntent().getStringExtra("Otype");
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);

		mDivisionData = readDivisions();
		// ------------------------------
		mDialog = new Dialog(this, R.style.CustomDialogTheme);
		dialogView = LayoutInflater.from(this).inflate(R.layout.send_tel_dailog, null);
		// 设置自定义的dialog布局
		mDialog.setContentView(dialogView);
		// false表示点击对话框以外的区域对话框不消失，true则相反
		mDialog.setCanceledOnTouchOutside(false);
		confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
		cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
		// ------------------------------
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 订单详情查询
		getOrderDetails();
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
	 * 获取订单详情
	 */
	public void getOrderDetails() {

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("OrderId", mOrderSn);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Otype", mOtype);
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

		String url = Constants.WEBAPI_ADDRESS + "api/Order/Detail_New?OrderId=" + mOrderSn + "&Otype=" + mOtype
				+ "&Sign=" + Sign + "&Ts=" + Ts;
		
		XutilsHttp.getInstance().get(url, null, new getOrderDetailsCallBack(),this);
	}

	class getOrderDetailsCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject jsonData = jo.getJSONObject("Data");
					if (jsonData != null && jsonData.length() > 0) {
						display(jsonData);
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 订单信息展示
	 * 
	 * @param data
	 *            订单信息
	 */
	private void display(JSONObject data) {
		try {
			JSONObject order = data.getJSONObject("order");
			// 收货人
			((TextView) findViewById(R.id.txt_consignee)).setText(order.getString("Consignee"));

			IsFreeShipping = order.getString("IsFreeShippingOrder");
			shopPhone = order.getString("ShopMobile");
			// 地址
			((TextView) findViewById(R.id.txt_address)).setText("地址无效");

			if (mDivisionData.has(order.getString("Province"))) {
				JSONObject provinceData = mDivisionData.getJSONObject(order.getString("Province"));

				if (provinceData.has(order.getString("City"))) {
					JSONObject cityData = provinceData.getJSONObject(order.getString("City"));

					if (cityData.has(order.getString("County"))) {
						JSONObject countyData = cityData.getJSONObject(order.getString("County"));
						String province = provinceData.getString("name");
						String city = cityData.getString("name");
						String county = countyData.getString("name");
						((TextView) findViewById(R.id.txt_address))
								.setText(province + " " + city + " " + county + " " + order.getString("Address"));
					}
				}
			}
			mCreateTime = order.getString("CreateTime");
			// 订单时间
			((TextView) findViewById(R.id.txt_order_time)).setText(order.getString("CreateTime"));
			// 手机
			((TextView) findViewById(R.id.txt_mobile)).setText(order.getString("Mobile"));
			// 运费
			String shipfee = order.getString("ShipFee");
			if (!StringUtils.isNull(shipfee) && !StringUtils.isZero(shipfee)) {
				String formatShipfee = MessageFormat.format(getString(R.string.txt_order_shipfe), shipfee);
				((TextView) findViewById(R.id.txt_shipfee)).setText(formatShipfee);
			}

			orderAmount = order.getString("OrderAmount");
			((TextView) findViewById(R.id.txt_amount)).setText("¥" + orderAmount);

			Float productAmount = Float.parseFloat(orderAmount) - Float.parseFloat(shipfee);
			// 商品总金额
			((TextView) findViewById(R.id.txt_product_amount)).setText("¥" + String.valueOf(productAmount));

			// 红包金额
			((TextView) findViewById(R.id.txt_hb_fee)).setText("-¥" + order.getString("FullCut"));

			// 订单编号
			((TextView) findViewById(R.id.txt_order_no)).setText(mOrderSn);
			// 订单状态
			boolean retractable = setStatus(order.getString("OrderStatus"), order.getString("PayStatus"));

			// 设置商品列表
			setProductsList(data.getJSONArray("orderProduct"), retractable);
		} catch (Exception e) {
			showSysErr(e);
		}
	}

	// 设置商品列表
	public void setProductsList(JSONArray products, boolean retractable) {
		try {
			LinearLayout lstProducts = (LinearLayout) findViewById(R.id.lst_product);
			lstProducts.removeAllViews();
			for (int i = 0; i < products.length(); i++) {
				final JSONObject product = products.getJSONObject(i);

				LinearLayout productView = new LinearLayout(this);
				View.inflate(this, R.layout.item_order_product, productView);
				ProductSKUName = product.getString("ProductSKUName");
				((TextView) productView.findViewById(R.id.txt_product_name))
						.setText(product.getString("ProductSKUName"));

				((TextView) productView.findViewById(R.id.txt_price)).setText("¥" + product.getString("ProductPrice"));
				((TextView) productView.findViewById(R.id.txt_count)).setText("x" + product.getString("BuyCount"));

				setImageFromUrl(product.getString("ThumbnailsUrl"),
						(ImageView) productView.findViewById(R.id.iv_product_thumbnails));

				productView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {

							// 跳转至商品详情页面
							Intent intent = new Intent();
							intent.putExtra("ProductSKUId", product.getString("ProductSKUId"));
							intent.putExtra("IsFreeShipping", product.getString("IsFreeShipping"));
							forward(ActivityProductDetailsNew.class, intent);
						} catch (Exception e) {
							// 系统异常
							showSysErr(e);
						}

					}
				});
				productView.findViewById(R.id.ll_customer_service).setVisibility(View.GONE);

				// 显示退款按钮
				productView.findViewById(R.id.ll_customer_refund).setVisibility(View.GONE);

				lstProducts.addView(productView);

				if (i < products.length() - 1) {
					// 分割线
					View view = new View(this);
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

	// 设置状态
	public boolean setStatus(String orderStatus, String payStatus) {
		boolean retractable = false;
		int statusDispId = -1;
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
					reduce = true;
					statusDispId = R.string.txt_order_unshipping;

				} else if ("1".equals(orderStatus)) {
					// 待发货
					reduce = true;
					statusDispId = R.string.txt_order_unshipping;
				} else if ("4".equals(orderStatus)) {
					// 订单状态是确认
					// 待收货
					retractable = true;
					statusDispId = R.string.txt_order_unreceiving;
				} else if ("6".equals(orderStatus)) {
					// 订单状态是已完成
					// 已完成
					retractable = true;
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
					// 待审核
					statusDispId = R.string.txt_order_daituikuan;
				} else if ("999".equals(orderStatus)) {
					// 待退款
					statusDispId = R.string.txt_order_daituikuan;

				}
			}
		}

		if (statusDispId != -1) {
			((TextView) findViewById(R.id.txt_status)).setText(statusDispId);
		}

		return retractable;
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
			backward();
			break;
		default:
			break;
		}
	}

}
