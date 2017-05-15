package com.bopinjia.customer.activity;

import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityOrderDetail extends BaseActivity {

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

	public static ActivityOrderDetail instances = null;

	/**
	 * 判断是否是直邮现货自营
	 */
	private String IsFreeShipping;
	private String shopPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_order_detail);
		instances = this;
		mCurrStatus = getIntent().getStringExtra("mCurrStatus");
		mOrderId = getIntent().getStringExtra("OrderId");
		mOrderSn = getIntent().getStringExtra("OrderSn");
		mOtype = getIntent().getStringExtra("Otype");
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);

		// 订单操作
		findViewById(R.id.btn_cancel).setOnClickListener(this);
		findViewById(R.id.btn_reminder).setOnClickListener(this);
		findViewById(R.id.btn_delete).setOnClickListener(this);
		findViewById(R.id.btn_pay).setOnClickListener(this);
		findViewById(R.id.btn_order_status).setOnClickListener(this);
		findViewById(R.id.complete_order_status).setOnClickListener(this);
		findViewById(R.id.confirmreceipt_order_status).setOnClickListener(this);
		// 确认收货
		findViewById(R.id.txt_order_confirmreceipt).setOnClickListener(this);

		// 退款
		findViewById(R.id.btn_tk).setOnClickListener(this);
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

				if (retractable) {
					// 商品可退数量
					String retractableCount = product.getString("RetractableCount");
					// 商品可退数量非空并且是数值时
					if (!StringUtils.isNull(retractableCount) && StringUtils.isInteger(retractableCount)) {
						int reCount = Integer.parseInt(retractableCount);
						// 商品可退数量大于零时
						if (reCount > 0) {

							// 显示售后按钮
							productView.findViewById(R.id.ll_customer_service).setVisibility(View.VISIBLE);
							Button btnRetract = (Button) productView.findViewById(R.id.btn_customer_service);
							// 售后按钮点击事件
							btnRetract.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {

									if (IsFreeShipping.equals("1")) {
										// 直邮跳到退货界面
										Intent intent = new Intent();
										intent.putExtra("name", ((TextView) findViewById(R.id.txt_consignee)).getText()
												.toString().trim());
										intent.putExtra("phone",
												((TextView) findViewById(R.id.txt_mobile)).getText().toString().trim());
										intent.putExtra("product", product.toString());
										intent.putExtra("OrderSn", mOrderSn);
										forward(ActivityRetractRequest.class, intent);
									} else {
										// 非直邮，拨打电话联系商家
										mDialog.show();
										// 获取自定义dialog布局控件
										((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否拨打电话联系店铺?");
										// 确定按钮点击事件
										confirmBt.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												Intent intent = new Intent();
												intent.setAction(Intent.ACTION_CALL);
												intent.setData(Uri.parse("tel:" + shopPhone));
												startActivity(intent);
												mDialog.dismiss();
											}
										});
										// 取消按钮点击事件
										cancelBt.setOnClickListener(new OnClickListener() {

											@Override
											public void onClick(View v) {
												mDialog.dismiss();
											}
										});

									}
								}
							});
						}
					}
				}
				if (reduce) {
					// 显示退款按钮
					productView.findViewById(R.id.ll_customer_refund).setVisibility(View.INVISIBLE);
					Button btnRefund = (Button) productView.findViewById(R.id.btn_customer_refund);
					// 售后按钮点击事件
					btnRefund.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
						}
					});
				} else {
				}

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
				findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
				// findViewById(R.id.see_reason).setVisibility(View.VISIBLE);
				findViewById(R.id.complete_order_status).setVisibility(View.GONE);
			} else if ("3".equals(orderStatus)) {
				// 已关闭
				statusDispId = R.string.txt_order_close;
				findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
				findViewById(R.id.complete_order_status).setVisibility(View.GONE);
			} else {
				// 待付款
				statusDispId = R.string.txt_order_unpaid;
				findViewById(R.id.ll_unpaid).setVisibility(View.VISIBLE);
			}
		} else {
			// 支付状态是已付款
			if ("1".equals(payStatus)) {
				// 订单状态是未确认
				if ("0".equals(orderStatus)) {
					// 待发货
					reduce = true;
					statusDispId = R.string.txt_order_unshipping;
					findViewById(R.id.ll_unship).setVisibility(View.VISIBLE);
				} else if ("1".equals(orderStatus)) {
					// 待发货
					reduce = true;
					statusDispId = R.string.txt_order_unshipping;
					findViewById(R.id.ll_unship).setVisibility(View.VISIBLE);
				} else if ("4".equals(orderStatus)) {
					// 订单状态是确认
					// 待收货
					retractable = true;
					statusDispId = R.string.txt_order_unreceiving;
					findViewById(R.id.ll_confirmreceipt).setVisibility(View.VISIBLE);
				} else if ("6".equals(orderStatus)) {
					// 订单状态是已完成
					// 已完成
					retractable = true;
					statusDispId = R.string.txt_order_complete;
					findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
					findViewById(R.id.complete_order_status).setVisibility(View.GONE);
					// findViewById(R.id.bt_shouhou).setVisibility(View.VISIBLE);
				} else if ("5".equals(orderStatus)) {
					// 已退货
					statusDispId = R.string.txt_order_close;
					findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
					findViewById(R.id.complete_order_status).setVisibility(View.GONE);
				} else if ("3".equals(orderStatus)) {
					// 已关闭
					statusDispId = R.string.txt_order_close;
					findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);
					findViewById(R.id.complete_order_status).setVisibility(View.GONE);
				} else if ("2".equals(orderStatus)) {
					// 已取消
					statusDispId = R.string.txt_order_canceled;
					findViewById(R.id.ll_complete).setVisibility(View.VISIBLE);

					// findViewById(R.id.see_reason).setVisibility(View.VISIBLE);
					findViewById(R.id.complete_order_status).setVisibility(View.GONE);

				} else if ("7".equals(orderStatus)) {
					// 待审核
					statusDispId = R.string.txt_order_daishenhe;
					findViewById(R.id.ll_unship).setVisibility(View.GONE);
					findViewById(R.id.btn_order_status).setVisibility(View.GONE);
					findViewById(R.id.btn_tk).setVisibility(View.GONE);
				} else if ("999".equals(orderStatus)) {
					// 待退款
					statusDispId = R.string.txt_order_daituikuan;
					findViewById(R.id.ll_unship).setVisibility(View.GONE);
					findViewById(R.id.btn_order_status).setVisibility(View.GONE);
					findViewById(R.id.btn_tk).setVisibility(View.GONE);

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
		case R.id.txt_order_confirmreceipt:
			orderReceive();

			break;
		case R.id.btn_return:
			// 返回
			backward();
			break;
		case R.id.confirmreceipt_order_status:
		case R.id.complete_order_status:
		case R.id.btn_order_status:
			// 查看物流
			Intent intento = new Intent();
			intento.putExtra("OrderId", mOrderId);
			intento.putExtra("Otype", mOtype);
			forward(ActivityOrderStateList.class, intento);
			break;
		case R.id.btn_cancel:
			mDialog.show();
			// 获取自定义dialog布局控件
			((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否取消该订单?");
			// 确定按钮点击事件
			confirmBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					operateOrder(1);
					mDialog.dismiss();
				}
			});
			// 取消按钮点击事件
			cancelBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			break;
		case R.id.btn_tk:
			// 退款
			Intent i = new Intent();
			i.putExtra("OrderSn", mOrderSn);
			i.putExtra("otype", mOtype);
			i.putExtra("orderAmount", orderAmount);
			// i.putExtra("name", ((TextView)
			// findViewById(R.id.txt_consignee)).getText().toString().trim());
			// i.putExtra("phone", ((TextView)
			// findViewById(R.id.txt_mobile)).getText().toString().trim());
			// i.putExtra("id", "0");
			forward(ActivityRefund.class, i);
			break;
		case R.id.btn_reminder:
			operateOrder(2);
			showToast("催单成功！");
			break;
		case R.id.btn_delete:
			mDialog.show();
			// 获取自定义dialog布局控件
			((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("确定删除该订单?");
			// 确定按钮点击事件
			confirmBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					operateOrder(3);
					mDialog.dismiss();
				}
			});
			// 取消按钮点击事件
			cancelBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mDialog.dismiss();
				}
			});
			break;
		case R.id.btn_pay:
			// 跳转到支付页面
			double dbAmount = Double.parseDouble(orderAmount);
			DecimalFormat df = new DecimalFormat("###,##0.00");
			// 跳转到支付页面
			Intent intent = new Intent();
			intent.putExtra("CreateTime", mCreateTime);
			intent.putExtra("Mode", mOtype);
			// 增加红包 把order改成ordersn
			intent.putExtra("OrderId", mOrderSn);
			intent.putExtra("JsonSkuName", ProductSKUName);
			intent.putExtra("payAmount", df.format(dbAmount));
			forward(ActivityPay.class, intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 订单处理
	 * 
	 * @param i
	 *            1:取消，2：催单，3：删除
	 */
	private void operateOrder(int i) {
		String api = "";
		if (i == 1) {
			api = "api/Order/Cancel_New";
		} else if (i == 2) {
			api = "api/Order/Reminder";
		} else if (i == 3) {
			api = "api/Order/Delete_New";
		}

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("UserId", getLoginUserId());
		map.put("OrderId", mOrderSn);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("Otype", mOtype);
		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			stringBuffer.append(key).append("=").append(map.get(key)).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);
		String Sign = MD5.Md5(stringBuffer.toString());
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("UserId", getLoginUserId());
		maps.put("Otype", mOtype);
		maps.put("OrderId", mOrderSn);
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + api, maps, new operateOrderCallBack(),this);
	}

	class operateOrderCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					Intent intent = new Intent();
					intent.putExtra("status", Constants.ORDER_STATUS_ALL);
					forward(ActivityOrderList.class, intent);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void orderReceive() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		map.put("OrderId", mOrderId);
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
		Map<String, String> maps = new HashMap<String, String>();
		maps.put("OrderId", mOrderId);
		maps.put("Otype", mOtype);
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/Order/Receive", maps, new orderReceiveCallBack(),this);
	}

	class orderReceiveCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					showToast("收货成功！");
					ActivityOrderList.instance.tab5();
					finish();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
