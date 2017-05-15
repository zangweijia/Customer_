package com.bopinjia.customer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.andview.refreshview.XRefreshView.XRefreshViewListener;
import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityMyFavourite extends BaseActivity {

	private ListView mList;

	private boolean editMode = false;

	private List<JSONObject> mShopList = null;
	private String mSelectedMsgId = "0";

	private FProdeuctListAdapter mProductAdapter = null;
	private List<JSONObject> mProductList = null;
	String productSKUIds = "";

	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_my_favourite);
		init();

	}

	private XRefreshView outView;
	public static long lastRefreshTime;

	private void init() {

		findViewById(R.id.tv_delete_all).setOnClickListener(this);
		findViewById(R.id.btn_return).setOnClickListener(this);
		findViewById(R.id.tv_edit).setOnClickListener(this);
		findViewById(R.id.tv_delete).setOnClickListener(this);

		mList = (ListView) findViewById(R.id.lst_favourite);
		// 初始化展示商品列表
		searchproduct(PageIndex, 0);
		// setRefreshListner(mList);

		outView = (XRefreshView) findViewById(R.id.custom_view);
		outView.setPullLoadEnable(true);
//		outView.setRefreshViewType(XRefreshViewType.ABSLISTVIEW);
		outView.setXRefreshViewListener(new XRefreshViewListener() {

			@Override
			public void onRefresh() {

				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						searchproduct(PageIndex, 0);
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

							searchproduct(PageIndex, 1);
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

							mList.postDelayed(new Runnable() {
								@Override
								public void run() {
									showToast("没有更多了~");
									outView.stopLoadMore();
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
		outView.restoreLastRefreshTime(lastRefreshTime);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_delete_all:
			final Dialog mDialog = new Dialog(ActivityMyFavourite.this, R.style.CustomDialogTheme);
			View dialogView = LayoutInflater.from(ActivityMyFavourite.this).inflate(R.layout.send_tel_dailog, null);
			// 设置自定义的dialog布局
			mDialog.setContentView(dialogView);
			// false表示点击对话框以外的区域对话框不消失，true则相反
			mDialog.setCanceledOnTouchOutside(false);
			// -----------------------------------

			mDialog.show();
			// 获取自定义dialog布局控件
			((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否清空我的收藏?");
			Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
			Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
			// 确定按钮点击事件
			confirmBt.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteProduct("0");
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

		case R.id.btn_return:
			finish();
			break;
		case R.id.tv_edit:
			editMode = !editMode;
			findViewById(R.id.ll_delete).setVisibility(editMode ? View.VISIBLE : View.GONE);
			((TextView) findViewById(R.id.tv_edit)).setText(editMode ? R.string.cmm_btn_cancel : R.string.cmm_btn_edit);
			if (mProductList != null && !mProductList.isEmpty()) {
				for (JSONObject model : mProductList) {
					try {
						model.put("editFlg", editMode);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				mProductAdapter.notifyDataSetChanged();
			}

			break;
		case R.id.tv_delete:
			try {

				for (int i = mProductList.size() - 1; i >= 0; i--) {
					JSONObject model = mProductList.get(i);
					if (model.getBoolean("checked")) {
						productSKUIds += model.getString("ProductSKUId") + ",";
					}
				}
			} catch (Exception e) {
				showSysErr(e);
			}

			break;

		default:
			break;
		}
	}

	/**
	 * 商品列表适配器
	 * 
	 */
	class FProdeuctListAdapter extends BaseAdapter {

		private Context mContext;
		private List<JSONObject> list;

		public FProdeuctListAdapter(Context context, List<JSONObject> list) {
			this.mContext = context;
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
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

			try {
				// 数据模型
				final JSONObject model = list.get(position);
				ProductListItem viewItem = new ProductListItem(mContext, list);
				// 传递shuid和isfreeshipping
				viewItem.setContent(model.getString("Productskuid"), model.getString("Isfreeshipping"));
				// 商品名称
				viewItem.setProductName(model.getString("Productskuname"));
				// 店铺收藏人数
				viewItem.setCollection(model.getString("Cumulativesales"));
				// 销售价格
				viewItem.setSalePrice(model.getString("Scanprice"));
				// 市场价格
				viewItem.setMarketPrice(model.getString("Marketprice"));

				// 缩略图
				viewItem.setThumbnail(model.getString("Productthumbnail"));

				// 删除
				viewItem.setEditFlg(model.getBoolean("editFlg"));

				CheckBox chkEdit = (CheckBox) viewItem.findViewById(R.id.checkbox);

				chkEdit.setChecked(model.getBoolean("checked"));

				chkEdit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						try {
							model.put("checked", isChecked);
						} catch (JSONException e) {
							showSysErr(e);
						}
					}
				});

				return viewItem;
			} catch (Exception e) {
				showSysErr(e);
			}

			return convertView;
		}

	}

	/**
	 * 商品列表item
	 * 
	 * @author Administrator
	 *
	 */
	class ProductListItem extends LinearLayout {

		private String skuid;
		private String isFreeShipping;

		public ProductListItem(Context context, final List<JSONObject> list) {
			super(context);
			View.inflate(getContext(), R.layout.wj_item_favourite_product, this);

			this.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent ii = new Intent();
					ii.putExtra("IsFreeShipping", isFreeShipping);
					ii.putExtra("ProductSKUId", skuid);
					ii.setClass(ActivityMyFavourite.this, ActivityProductDetailsNew.class);
					startActivity(ii);

				}
			});
			this.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					final Dialog mDialog = new Dialog(ActivityMyFavourite.this, R.style.CustomDialogTheme);
					View dialogView = LayoutInflater.from(ActivityMyFavourite.this).inflate(R.layout.send_tel_dailog,
							null);
					// 设置自定义的dialog布局
					mDialog.setContentView(dialogView);
					// false表示点击对话框以外的区域对话框不消失，true则相反
					mDialog.setCanceledOnTouchOutside(false);
					// -----------------------------------

					mDialog.show();
					// 获取自定义dialog布局控件
					((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否删除该商品?");
					Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
					Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
					// 确定按钮点击事件
					confirmBt.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							deleteProduct(skuid);
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

					return false;
				}
			});

			((CheckBox) this.findViewById(R.id.checkbox)).setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					try {
						for (int i = 0; i < list.size(); i++) {
							JSONObject product = list.get(i);
							int index = product.getInt("index");
							mProductList.get(index).put("checked", isChecked);
						}

					} catch (Exception e) {
						showSysErr(e);
					}
				}
			});
		}

		public void setContent(String skuid, String isShipping) {
			this.isFreeShipping = isShipping;
			this.skuid = skuid;
		}

		// 设置购买人数
		public void setCollection(String collection) {
			((TextView) findViewById(R.id.txt_colletion)).setText(collection + "人已购买");
		}

		// 设置商品名称
		public void setProductName(String name) {
			((TextView) findViewById(R.id.txt_shop_name)).setText(name);
		}

		// 设置销售价格
		public void setSalePrice(String price) {
			((TextView) findViewById(R.id._tv_sale_price)).setText(price);
		}

		// 设置市场价格
		public void setMarketPrice(String price) {
			((TextView) findViewById(R.id._tv_market_price)).setText("¥" + price);
			((TextView) findViewById(R.id._tv_market_price)).getPaint().setStrikeThruText(true);
		}

		// 缩略图
		public void setThumbnail(String thumbnail) {
			ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
					.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
					.build();

			x.image().bind((ImageView) findViewById(R.id.iv_shop_thumbnails), thumbnail, imageOptions);
		}

		// 设置删除
		public void setEditFlg(boolean isDelete) {
			if (isDelete) {
				findViewById(R.id.checkbox).setVisibility(VISIBLE);
			} else {
				findViewById(R.id.checkbox).setVisibility(GONE);
			}
		}
	}

	/**
	 * 获取商品列表
	 */
	private void searchproduct(int i, final int id) {
		String mId = getBindingShop();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("PageIndex", String.valueOf(i));
		map.put("Ts", Ts);
		map.put("MdUserId", mId);
		map.put("pageSize", "20");

		String url = Constants.WEBAPI_ADDRESS + "api/UserCollection/List?UserId=" + getLoginUserId() + "&MdUserId="
				+ mId + "&PageIndex=" + String.valueOf(i) + "&pageSize=" + "20" + "&Sign=" + NetUtils.getSign(map)
				+ "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new searchproductCallBack(), id, null, this);

	}

	class searchproductCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					parseProduct(result, id);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 删除收藏商品
	 */
	private void deleteProduct(String skuid) {
		String mId = getBindingShop();

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", getLoginUserId());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		map.put("ProductSKUId", skuid);
		map.put("MdUserId", mId);

		String url = Constants.WEBAPI_ADDRESS + "api/UserCollection/Add?UserId=" + getLoginUserId() + "&ProductSKUId="
				+ skuid + "&MdUserId=" + mId + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new deleteProductCallBack(), this);
	}

	class deleteProductCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			searchproduct(1, 0);
		}

	}

	/**
	 * 解析商品列表
	 * 
	 * @param result
	 */
	public void parseProduct(String result, int id) {
		try {
			JSONObject jo = new JSONObject(result);
			JSONObject jod = jo.getJSONObject("Data");
			JSONArray dataArray = jod.getJSONArray("Records");

			JSONObject Paging = jod.getJSONObject("Paging");
			mAllPages = Paging.getString("Pages");

			if (dataArray != null && dataArray.length() > 0) {
				List<JSONObject> cartList = new ArrayList<JSONObject>();
				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					data.put("checked", false);
					data.put("editFlg", false);
					cartList.add(data);
				}

				if (id == 0) {
					this.mProductList = cartList;
					mProductAdapter = new FProdeuctListAdapter(this, mProductList);
					mList.setAdapter(mProductAdapter);
				} else if (id == 1) {
					if (cartList != null && !cartList.isEmpty()) {
						mProductList.addAll(cartList);
						mProductAdapter.notifyDataSetChanged();
					}
					// mList.onRefreshComplete();
				}

			} else {
				mProductList.clear();
				mProductAdapter.notifyDataSetChanged();
			}
		} catch (Exception e) {

		}
	}

}