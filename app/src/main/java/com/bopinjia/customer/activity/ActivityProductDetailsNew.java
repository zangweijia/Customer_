package com.bopinjia.customer.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.GoodsDetailFragment;
import com.bopinjia.customer.fragment.GoodsInfoFragment;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.MyBadgeView;
import com.bopinjia.customer.view.NoScrollViewProduct;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityProductDetailsNew extends BaseActivity {

	@ViewInject(R.id.tv_title)
	public TextView tv_title;

	// 加入购物车
	@ViewInject(R.id.tv_add_cart)
	private TextView mAddCart;

	@ViewInject(R.id.psts_tabs)
	public PagerSlidingTabStrip psts_tabs;

	@ViewInject(R.id.vp_content)
	public NoScrollViewProduct vp_content;

	// 购物车图标
	@ViewInject(R.id.iv_cart)
	private ImageView mIvCart;
	// 购物车控件
	@ViewInject(R.id.ll_cart)
	private LinearLayout mCart;

	@ViewInject(R.id.tv_cart)
	private TextView mTVCart;

	@ViewInject(R.id.tv_collection)
	private TextView tv_collection;
	// 收藏图标
	@ViewInject(R.id.iv_collection)
	private ImageView mIvCollection;
	// 收藏控件
	@ViewInject(R.id.ll_collection)
	private LinearLayout mCollection;

	private List<Fragment> fragmentList = new ArrayList<Fragment>();

	private String skuid;

	private String userid;

	private MyBadgeView badge1;

	private String isfreeShip;
	private String skuname, skupic, skuurl;
	private boolean isFXS = false;

	private boolean isShare = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_product_details_new);
		x.view().inject(this);
		UMShareAPI.get(ActivityProductDetailsNew.this);

		vp_content = (NoScrollViewProduct) findViewById(R.id.vp_content);
		if (isLogged()) {
			userid = getLoginUserId();
		} else {
			userid = "0";
		}

		if (getIntent().hasExtra("url")) {
			String url = getIntent().getStringExtra("url");
			Uri uri = Uri.parse(url);
			skuid = uri.getQueryParameter("sid");
		} else {
			skuid = getIntent().getStringExtra("ProductSKUId");
		}
		isfreeShip = getIntent().getStringExtra("IsFreeShipping");

		String action = getIntent().getAction();
		if (Intent.ACTION_VIEW.equals(action)) {
			Uri uri = getIntent().getData();
			if (uri != null) {
				isShare = true;

				skuid = uri.getQueryParameter("id");
				getUserSharedV(skuid);
				String type = uri.getQueryParameter("type");
				// 分销商ID
				String shareUserId = uri.getQueryParameter("shareuserID");
				putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_GDSUSERID, shareUserId);

				try {
					String urlStr = URLDecoder.decode(type, "UTF-8");
					String urlname = urlStr.substring(23, 25);
					if (urlname.equals("zy")) {
						isfreeShip = "1";
					} else if (urlname.equals("xh")) {
						isfreeShip = "0";
					}

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		if (isfreeShip.equals("0")) {
			this.skuurl = "http://wap.bopinwang.com/" + "xhproductdetail.aspx?sid=" + skuid + "&ShareUserID=" + userid;
		} else if (isfreeShip.equals("1")) {
			this.skuurl = "http://wap.bopinwang.com/" + "zyproductdetail.aspx?sid=" + skuid + "&ShareUserID=" + userid;
		}

		fragmentList.add(GoodsInfoFragment.newInstance(skuid, isfreeShip));
		fragmentList.add(GoodsDetailFragment.newInstance(skuid, isfreeShip));

		ItemTitlePagerAdapter adapter = new ItemTitlePagerAdapter(getSupportFragmentManager(), fragmentList,
				new String[] { "商品", "详情" });

		vp_content.setAdapter(adapter);
		psts_tabs.setViewPager(vp_content);
		ShowGDS();
		getIsDistribution();
		badge1 = new MyBadgeView(this, mTVCart);
		// 需要显示的提醒类容
		badge1.setBadgePosition(MyBadgeView.POSITION_TOP_RIGHT);// 显示的位置.右上角,BadgeView.POSITION_BOTTOM_LEFT,下左，还有其他几个属性
		badge1.setTextColor(Color.WHITE); // 文本颜色
		badge1.setBadgeBackgroundColor(getResources().getColor(R.color.main_color)); // 提醒信息的背景颜色，自己设置
		badge1.setTextSize(8); // 文本大小
		badge1.setGravity(Gravity.CENTER);
		badge1.setBadgeMargin(1);// 各边间隔
		getCollection();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getCartnumber();
	}

	public class ItemTitlePagerAdapter extends FragmentPagerAdapter {
		private String[] titleArray;
		private List<Fragment> fragmentList;

		public ItemTitlePagerAdapter(FragmentManager fm, List<Fragment> fragmentList, String[] titleArray) {
			super(fm);
			this.fragmentList = fragmentList;
			this.titleArray = titleArray;
		}

		public void setFramentData(List<Fragment> fragmentList) {
			this.fragmentList = fragmentList;
			notifyDataSetChanged();
		}

		public void setTitleData(String[] titleArray) {
			this.titleArray = titleArray;
			notifyDataSetChanged();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return titleArray[position];
		}

		@Override
		public int getCount() {
			return titleArray.length;
		}

		@Override
		public Fragment getItem(int position) {
			return fragmentList.get(position);
		}
	}

	/**
	 * 得到商品SKU详情 增加是否显示分销商佣金
	 */
	private void ShowGDS() {

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("SkuId", skuid);
		map.put("UserId", userid);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Product/ShowGDS?SkuId=" + skuid + "&UserId=" + userid + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new showGds(), this);

	}

	class showGds implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject data = jo.getJSONObject("Data");
					JSONObject sku = data.getJSONObject("sku");

					skuname = sku.getString("SkuName");
					skupic = sku.getString("SkuThumbnail");

					String RealStock = sku.getString("RealStock");
					if (RealStock.equals("0")) {
						mAddCart.setBackgroundColor(getResources().getColor(R.color.add_cart));
						mAddCart.setText("暂时无货");
						mAddCart.setEnabled(false);
					} else {
						mAddCart.setBackgroundColor(getResources().getColor(R.color.main_color));
						mAddCart.setText("加入购物车");
						mAddCart.setEnabled(true);
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	@Event(value = { R.id.tv_add_cart, R.id.ll_cart, R.id.ll_back, R.id.ll_collection, R.id.iv_share })
	private void getEvent(View v) {

		switch (v.getId()) {
		case R.id.tv_add_cart:
			upDataCart(skuid, isfreeShip);
			break;
		case R.id.ll_cart:
			forward(ActivityCart.class);
			break;
		case R.id.ll_back:
			if (isShare) {
				forward(ActivityHome.class);
				finish();
			}
			finish();

			break;
		case R.id.ll_collection:
			if (isLogged()) {
				Collection(skuid);

			} else {
				showToast("亲，收藏功能需要先登录");
				forward(ActivityLogin.class);
			}
			break;
		case R.id.iv_share:
			Share();
			break;
		default:
			break;
		}
	}

	/**
	 * 更新购物车
	 * 
	 * @param Quantity
	 * @param SkuId
	 * @param ProductType
	 */
	private void upDataCart(String SkuId, String ProductType) {

		String shopid = getBindingShop();

		String Ts = MD5.getTimeStamp();
		String str = "Key=" + Constants.WEBAPI_KEY + "&MUserId=" + shopid + "&Paystate=0" + "&ProductType="
				+ ProductType + "&Quantity=" + "1" + "&SkuId=" + SkuId + "&Ts=" + Ts + "&UserId=" + userid + "&UUID="
				+ getIMEI1() + "&WarehouseId=0";

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwAdd_New");
		params.addBodyParameter("ProductType", ProductType);
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
						getCartnumber();
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
	 * 收藏
	 */
	private void Collection(String skuid) {
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

		XutilsHttp.getInstance().get(url, null, new CallBack(), this);

	}

	class CallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					if (isCollection) {
						showToast("取消收藏");
						isCollection = false;
						mIvCollection.setImageResource(R.drawable.product_favorite_defult);
						tv_collection.setText("收藏");
						tv_collection.setTextColor(getResources().getColor(R.color.bg_666666));
					} else {
						showToast("已收藏");
						isCollection = true;
						mIvCollection.setImageResource(R.drawable.productsectionseleced);
						tv_collection.setText("已收藏");
						tv_collection.setTextColor(getResources().getColor(R.color.main_color));
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean isCollection = false;

	/**
	 * 判断是否收藏
	 */
	private void getCollection() {

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("MdUserId", getBindingShop());
		map.put("UserId", userid);
		map.put("ProductSKUId", skuid);

		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/UserCollection/Exists?UserId=" + userid + "&ProductSKUId=" + skuid
				+ "&MdUserId=" + getBindingShop() + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getCollectionCallback(), this);

	}

	class getCollectionCallback implements XCallBack {

		@Override
		public void onResponse(String result) {
			// 1 收藏 2 未收藏
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					isCollection = true;
					mIvCollection.setImageResource(R.drawable.productsectionseleced);
					tv_collection.setText("已收藏");
					tv_collection.setTextColor(getResources().getColor(R.color.main_color));

				} else if (jsonresult.equals("2")) {
					isCollection = false;
					mIvCollection.setImageResource(R.drawable.product_favorite_defult);
					tv_collection.setText("收藏");
					tv_collection.setTextColor(getResources().getColor(R.color.bg_666666));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取购物车数量
	 */
	public void getCartnumber() {
		String uuid = getIMEI();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("UserId", userid);
		map.put("UUID", uuid);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwGetShppingCartSum?UserId="
				+ userid + "&UUID=" + uuid + "&Sign=" + Sign + "&Ts=" + Ts);
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						ScaleAnimation animation = new ScaleAnimation(0.0f, 1.2f, 0.0f, 1.2f,
								Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f);
						animation.setDuration(1000);// 设置动画持续时间
						ScaleAnimation animation1 = new ScaleAnimation(0.0f, 1.2f, 0.0f, 1.2f,
								Animation.RELATIVE_TO_SELF, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f);
						animation1.setDuration(500);// 设置动画持续时间

						String number = jo.getString("Data");

						int i = Integer.parseInt(number);
						if (i == 0) {
							badge1.hide();
						} else if (i > 99) {
							// 动画效果
							badge1.toggle(animation, animation1);
							badge1.setText("99+");
							badge1.show();
						} else if (number.equals(badge1.getText().toString().trim())) {
							badge1.setText(number);
							badge1.show();
						} else {
							// 动画效果
							badge1.toggle(animation, animation1);
							badge1.setText(number);
							badge1.show();
						}

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(CancelledException cex) {
				Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFinished() {

			}
		});
	}

	/**
	 * 分享
	 */
	public void Share() {
		ShareAction s = new ShareAction(this)
				.setDisplayList(SHARE_MEDIA.QQ, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE)
				.setShareboardclickCallback(shareBoardlistener);
		s.open();
	}

	private ShareBoardlistener shareBoardlistener = new ShareBoardlistener() {

		@Override
		public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
			String str = "我在舶品家发现了一个不错的商品,赶紧来看看吧。";
			UMImage img = new UMImage(ActivityProductDetailsNew.this, skupic);
			String mFXSnumber = getBopinjiaSharedPreference(Constants.KEY_FXS_NUMBER);
			if (share_media == SHARE_MEDIA.QQ) {

				UMWeb web = new UMWeb(skuurl);
				web.setTitle(skuname);// 标题
				web.setThumb(img); // 缩略图
				if (isLogged()) {
					if (isFXS) {
						web.setDescription("我在舶品家发现了一个不错的商品，您可以通过邀请码【" + mFXSnumber + "】进店购买商品,有惊喜哟");// 描述
					} else {
						web.setDescription(str);// 描述
					}
				} else {
					web.setDescription(str);// 描述
				}

				new ShareAction(ActivityProductDetailsNew.this).setCallback(umShareListener).withMedia(web)
						.setPlatform(SHARE_MEDIA.QQ).share();

			} else if (share_media == SHARE_MEDIA.WEIXIN) {

				UMWeb web = new UMWeb(skuurl);
				web.setTitle(skuname);// 标题
				web.setThumb(img); // 缩略图

				if (isLogged()) {
					if (isFXS) {
						web.setDescription("我在舶品家发现了一个不错的商品，您可以通过邀请码【" + mFXSnumber + "】进店购买商品,有惊喜哟");// 描述
					} else {
						web.setDescription(str);// 描述
					}
				} else {
					web.setDescription(str);// 描述
				}
				new ShareAction(ActivityProductDetailsNew.this).setCallback(umShareListener).withMedia(web)
						.setPlatform(SHARE_MEDIA.WEIXIN).share();

			} else if (share_media == SHARE_MEDIA.WEIXIN_CIRCLE) {

				UMWeb web = new UMWeb(skuurl);
				web.setThumb(img); // 缩略图
				if (isLogged()) {
					if (isFXS) {
						web.setTitle(skuname + "  " + "通过邀请码" + mFXSnumber + "进入APP 购买有惊喜哟");// 标题
					} else {
						web.setTitle(skuname);// 标题
					}
				} else {
					web.setTitle(skuname);// 标题
				}

				new ShareAction(ActivityProductDetailsNew.this).setCallback(umShareListener).withMedia(web)
						.setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE).share();

			}
		}
	};

	private UMShareListener umShareListener = new UMShareListener() {
		@Override
		public void onResult(SHARE_MEDIA platform) {
		}

		@Override
		public void onError(SHARE_MEDIA platform, Throwable t) {
		}

		@Override
		public void onCancel(SHARE_MEDIA platform) {
		}

		@Override
		public void onStart(SHARE_MEDIA arg0) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * 判断是否为分销商
	 */
	private void getIsDistribution() {

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", userid);
		map.put("MDUserId", getBindingShop());
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GDSExists?UserId=" + userid + "&MDUserId="
				+ getBindingShop() + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new IsDistributionCallBack(), this);

	}

	/**
	 * 判断是否为经销商回调
	 */
	class IsDistributionCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					String Data = jo.getString("Data");

					if (Data.equals("0")) {
						// 一般客户
						isFXS = false;
					} else if (Data.equals("1")) {
						// 分销商
						isFXS = true;
					} else if (Data.equals("2")) {
						// 一般用户 可申请分销商
						isFXS = false;
					}

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		UMShareAPI.get(ActivityProductDetailsNew.this).onActivityResult(requestCode, resultCode, data);

	}

	private void getUserSharedV(String skuid) {

		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("ProductSKUId", skuid);

		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Product/UserSharedV?ProductSKUId=" + skuid + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new getUserSharedV(), this);

	}

	class getUserSharedV implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject data = jo.getJSONObject("Data");
					putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_SHOP, data.getString("MdUserID"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	// e3e5e9
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isShare) {
				forward(ActivityHome.class);
				finish();
			}
			finish();
			
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
