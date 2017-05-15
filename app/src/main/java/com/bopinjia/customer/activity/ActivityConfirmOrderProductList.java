package com.bopinjia.customer.activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;

public class ActivityConfirmOrderProductList extends BaseActivity {
	private int mCount;
	private TextView tv_count;
	private TextView tv_return;
	private ListView listView;

	private JSONArray mShopList;
	private List<JSONObject> mCartList;
	private List<JSONObject> dataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_confirm_order_product_list);

		mCount = getIntent().getIntExtra("count", 0);
		init();
	}

	private void init() {
		tv_count = (TextView) findViewById(R.id.tv_all_count);
		tv_count.setText("共" + mCount + "件");
		tv_return = (TextView) findViewById(R.id.btn_return);
		tv_return.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				backward();
			}
		});
		listView = (ListView) findViewById(R.id.listview);
		getProductList();
	}

	private void getProductList() {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", getLoginUserId());
		map.put("PageIndex", "1");
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/CSC/BpwPayList?UserId=" + getLoginUserId() + "&PageIndex=" + "1"
				+ "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getProductListCallBack(),this);
	}

	class getProductListCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {

			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parse(jo);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void parse(JSONObject jo) {
		try {

			JSONObject jD = jo.getJSONObject("Data");
			JSONObject Total = jD.getJSONObject("Total");

			mShopList = jD.getJSONArray("ShopList");
			if (mShopList != null) {
				dataList = new ArrayList<JSONObject>();

				for (int i = 0; i < mShopList.length(); i++) {
					JSONObject data = mShopList.getJSONObject(i);
					dataList.add(data);
				}
				this.mCartList = dataList;
				CartListAdapter cartListAdapter = new CartListAdapter(this);
				listView.setAdapter(cartListAdapter);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 购物车列表适配器
	 * 
	 * @author yushen 2015/12/29
	 */
	class CartListAdapter extends BaseAdapter {

		private Context mContext;

		public CartListAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return mCartList.size();
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
			CartListItem viewItem = new CartListItem(mContext);
			try {
				// 数据模型
				JSONObject model = mCartList.get(position);
				// 自营现货区
				viewItem.setWareHouse(model.getString("TypeName"));
				// 设置分区是否选中
				viewItem.setChecked("1");
				// 设置仓库列表
				JSONArray mHuouseList = model.getJSONArray("Warehouse");
				viewItem.setHouseList(mHuouseList);

				viewItem.setPosition(position);

			} catch (Exception e) {
			}
			return viewItem;
		}
	}

	/**
	 * 购物车列表控件
	 */
	class CartListItem extends LinearLayout {

		private String mMainId;
		private List<LinearLayout> mViewList, mProductList;
		private DecimalFormat df;
		private int position;

		public CartListItem(Context context) {
			super(context);
			View.inflate(getContext(), R.layout.wj_item_cart, this);
			mViewList = new ArrayList<LinearLayout>();
			mProductList = new ArrayList<LinearLayout>();
			// ((CheckBox)
			// this.findViewById(R.id.checkbox_cartmain)).setVisibility(View.INVISIBLE);
		}

		public void setPosition(int position) {
			this.position = position;
		}

		// 设置区域是否选中
		public void setChecked(String state) {
			((CheckBox) findViewById(R.id.checkbox_cartmain)).setChecked(true);
			((CheckBox) findViewById(R.id.checkbox_cartmain)).setEnabled(false);
			;
		}

		// 设置分区
		public void setWareHouse(String wareHouse) {
			if (wareHouse.equals("直邮区")) {
				mMainId = "1";
			} else if (wareHouse.equals("现货区")) {
				mMainId = "0";
			} else if (wareHouse.equals("自营区")) {
				mMainId = "2";
			}
			// LinearLayout.LayoutParams lp = new
			// LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
			// LinearLayout.LayoutParams.WRAP_CONTENT);
			// lp.setMargins(20, 23, 0, 0);
			// ((TextView)
			// findViewById(R.id.cartmain_name)).setLayoutParams(lp);
			// ((TextView) findViewById(R.id.cartmain_name)).setText(wareHouse);
		}

		// 设置仓库列表
		public void setHouseList(JSONArray warhousers) {
			try {
				df = new DecimalFormat("#,##0.00");
				LinearLayout lstHouses = (LinearLayout) findViewById(R.id.ll_cartmain);
				for (int i = 0; i < warhousers.length(); i++) {
					final JSONObject house = warhousers.getJSONObject(i);
					final LinearLayout HouseView = new LinearLayout(ActivityConfirmOrderProductList.this);
					View.inflate(ActivityConfirmOrderProductList.this, R.layout.wj_item_cart_sub, HouseView);

					if (mMainId.equals("1")) {
						// 仓库名称
						((TextView) HouseView.findViewById(R.id.tv_housename))
								.setText(house.getString("WarehouseName"));
					} else {
						((TextView) HouseView.findViewById(R.id.tv_housename)).setVisibility(View.GONE);
					}
					// 销售价
					// double dbSellPrice = new
					// BigDecimal(house.getString("WarehousePrice")).doubleValue();
					// ((TextView)
					// HouseView.findViewById(R.id.txt_sell_price)).setText("¥"
					// + df.format(dbSellPrice));
					if (!mMainId.equals("0")) {
						((TextView) HouseView.findViewById(R.id.txt_sell_price)).setVisibility(View.GONE);
						((TextView) HouseView.findViewById(R.id.tv_spze)).setVisibility(View.GONE);
					} else {
						((LinearLayout) HouseView.findViewById(R.id.ll_yc)).setVisibility(View.GONE);
					}
					// --------------------

					JSONArray products = house.getJSONArray("productdata");

					// 设置商品列表
					LinearLayout lstProducts = (LinearLayout) HouseView.findViewById(R.id.ll_cartmain_sub);
					for (int j = 0; j < products.length(); j++) {
						final JSONObject product = products.getJSONObject(j);

						final LinearLayout productView = new LinearLayout(ActivityConfirmOrderProductList.this);
						View.inflate(ActivityConfirmOrderProductList.this, R.layout.item_confirm_productlist,
								productView);

						// 商品名称
						((TextView) productView.findViewById(R.id._tv_list_name)).setText(product.getString("SkuName"));
						// 销售价
						final double dbSellPric = new BigDecimal(product.getString("SellPrice")).doubleValue();
						((TextView) productView.findViewById(R.id._tv_list_sale_price))
								.setText("¥" + df.format(dbSellPric));
						// 商品数量
						// ((TextView)
						// productView.findViewById(R.id.tv_product_count)).setVisibility(View.INVISIBLE);

						((TextView) productView.findViewById(R.id.tv_count))
								.setText("x" + product.getString("Quantity"));
						;

						// 商品缩略图
						ImageOptions imageOptions = new ImageOptions.Builder()
								.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
								.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
								.build();

						x.image().bind((ImageView) productView.findViewById(R.id._iv_list_thumbnails),
								product.getString("ProductThumbnail"), imageOptions);

						// productView.findViewById(R.id.product_checkbox).setVisibility(View.INVISIBLE);

						// 减号点击事件
						// productView.findViewById(R.id.tv_reduce).setVisibility(View.INVISIBLE);

						// 加号点击事件
						// productView.findViewById(R.id.tv_add).setVisibility(View.INVISIBLE);
						productView.setLayoutParams(
								new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
						lstProducts.addView(productView);
						mProductList.add(productView);

						if (i < products.length() - 1) {
							// 分割线
							View view = new View(getContext());
							view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
									(int) getResources().getDimension(R.dimen.divider_line)));
							view.setBackgroundColor(getResources().getColor(R.color.bg_divider_line_minus));
							lstProducts.addView(view);
						}

					}
					// ----------------------------

					HouseView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
					lstHouses.addView(HouseView);
					mViewList.add(HouseView);

					if (i < warhousers.length() - 1) {
						// 分割线
						View view = new View(getContext());
						view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
								(int) getResources().getDimension(R.dimen.divider_line)));
						view.setBackgroundColor(getResources().getColor(R.color.bg_divider_line_minus));
						lstHouses.addView(view);
					}

				}
			} catch (

			Exception e) {
			}

		}

	}

}
