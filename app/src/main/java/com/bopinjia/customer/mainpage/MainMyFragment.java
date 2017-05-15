package com.bopinjia.customer.mainpage;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityAddressList;
import com.bopinjia.customer.activity.ActivityFXGL;
import com.bopinjia.customer.activity.ActivityFXIntroduce;
import com.bopinjia.customer.activity.ActivityFXQRcode;
import com.bopinjia.customer.activity.ActivityFXSPersonalCenter;
import com.bopinjia.customer.activity.ActivityFXWallet;
import com.bopinjia.customer.activity.ActivityJoin;
import com.bopinjia.customer.activity.ActivityLogin;
import com.bopinjia.customer.activity.ActivityMyFavourite;
import com.bopinjia.customer.activity.ActivityMyself;
import com.bopinjia.customer.activity.ActivityOrderList;
import com.bopinjia.customer.activity.ActivityRedPagketList;
import com.bopinjia.customer.activity.ActivityRetractList;
import com.bopinjia.customer.activity.ActivitySetting;
import com.bopinjia.customer.activity.ActivityShopList;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.bean.LoginBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.MyBadgeView;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@ContentView(R.layout.fragment_activity_main_my)
public class MainMyFragment extends Fragment {

	private MyBadgeView mUnpaidBadge;
	private MyBadgeView mUnshippingBadge;
	private MyBadgeView mUnreceivingBadge;

	@ViewInject(R.id.my_tv_name)
	private TextView mMyName;

	@ViewInject(R.id.my_tv_qianming)
	private TextView mQianMing;

	@ViewInject(R.id.tv_money)
	private TextView mMyMoney;

	private String headPortrait;

	private boolean isLogged;

	private String distribution;

	private String name;
	private ImageView mIV;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return x.view().inject(this, inflater, container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initClick();
	}

	@Override
	public void onResume() {
		super.onResume();
		isLogged = ((BaseActivity) getActivity()).isLogged();
		if (isLogged) {
			init();
		} else {
			noLogged();
		}
	}

	private void initClick() {
		getActivity().findViewById(R.id.all).setOnClickListener(null);

		mUnpaidBadge = getBadgeView(R.id.iv_my_btn_pending_payment);
		mUnshippingBadge = getBadgeView(R.id.iv_my_btn_unshipping);
		mUnreceivingBadge = getBadgeView(R.id.iv_my_btn_unreceiving);
		// 右上角数字提示
		mUnpaidBadge.setVisibility(View.INVISIBLE);
		mUnshippingBadge.setVisibility(View.INVISIBLE);
		mUnreceivingBadge.setVisibility(View.INVISIBLE);
	}

	/**
	 * 没登录操作 跳转到登录界面
	 */
	private void noLogged() {
		// 右上角数字提示
		mUnpaidBadge.setVisibility(View.INVISIBLE);
		mUnshippingBadge.setVisibility(View.INVISIBLE);
		mUnreceivingBadge.setVisibility(View.INVISIBLE);

		getActivity().findViewById(R.id.customer).setVisibility(View.VISIBLE);
		getActivity().findViewById(R.id.distribution).setVisibility(View.GONE);
		// 性别标识
		getActivity().findViewById(R.id.iv_sex).setVisibility(View.GONE);
		getActivity().findViewById(R.id.my_money).setVisibility(View.GONE);
		// 分销管理
		getActivity().findViewById(R.id.my_fxgl).setVisibility(View.GONE);
		// 我要加盟
		getActivity().findViewById(R.id.my_myjoin).setEnabled(true);

		((TextView) getActivity().findViewById(R.id.txt_shenqing)).setText("");
		getActivity().findViewById(R.id.shenqingiv).setVisibility(View.VISIBLE);

		// 没登陆分销入口隐藏
		getActivity().findViewById(R.id.iv_fxsq).setVisibility(View.GONE);

		((TextView) getActivity().findViewById(R.id.my_tv_name)).setText("登录/注册");
		((TextView) getActivity().findViewById(R.id.my_tv_qianming)).setVisibility(View.GONE);

		ImageView ivHead = (ImageView) getActivity().findViewById(R.id.iv_head_portrait);
		ivHead.setImageResource(R.drawable.ic_touxiangcilcle);

		getActivity().findViewById(R.id.all).setEnabled(true);
		getActivity().findViewById(R.id.all).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((BaseActivity) getActivity()).forward(ActivityLogin.class);
			}
		});

	}

	/**
	 * 登录后操作
	 */
	private void init() {
		LinearLayout mTitle = (LinearLayout)getActivity().findViewById(R.id.title);
		mTitle.setVisibility(View.GONE);
		mIV = (ImageView) getActivity().findViewById(R.id.iv_fxsq);
		// 获取用户的最新信息
		GetUserInfor();
		getActivity().findViewById(R.id.all).setEnabled(false);
		// 获取订单各状态的件数
		GetCustomerOrderTotal();
		getIsDistribution();
	}

	/**
	 * 获取用户信息
	 */
	private void GetUserInfor() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();
		map.put("RegisterPhone",
				((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_PHONE));
		map.put("DeviceType", "0");
		map.put("DeviceToken", "0");
		map.put("Password", "0");
		map.put("UUID", ((BaseActivity) getActivity()).getIMEI1());
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

		Map<String, String> maps = new HashMap<String, String>();
		maps.put("RegisterPhone",
				((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_PHONE));
		maps.put("DeviceType", "0");
		maps.put("DeviceToken", "0");
		maps.put("Password", "0");
		maps.put("UUID", ((BaseActivity) getActivity()).getIMEI1());
		maps.put("Sign", Sign);
		maps.put("Ts", Ts);

		XutilsHttp.getInstance().post(Constants.WEBAPI_BOPINWANG + "api/Login/PostPhone", maps, new userinfoCallBack(),
				getActivity());

	}

	/**
	 * 获取用户信息 回调
	 * 
	 * @author ZWJ
	 *
	 */
	class userinfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			String mUserInf = result;

			Gson gson = new Gson();
			LoginBean mLogBean = new LoginBean();
			mLogBean = gson.fromJson(mUserInf, LoginBean.class);
			// 用户昵称
			name = mLogBean.getData().getNickName();
			((TextView) getActivity().findViewById(R.id.my_tv_name)).setText(name);

			// 性别
			String sex = mLogBean.getData().getSex();
			if (sex.equals("0")) {
				getActivity().findViewById(R.id.iv_sex).setVisibility(View.GONE);
			} else if (sex.equals("1")) {
				getActivity().findViewById(R.id.iv_sex).setVisibility(View.VISIBLE);
				((ImageView) getActivity().findViewById(R.id.iv_sex)).setImageResource(R.drawable.nann);
			} else if (sex.equals("2")) {
				getActivity().findViewById(R.id.iv_sex).setVisibility(View.VISIBLE);
				((ImageView) getActivity().findViewById(R.id.iv_sex)).setImageResource(R.drawable.nv);
			}
			// 用户等级
			int UserLevel = mLogBean.getData().getUserLevel();
			if (UserLevel > 0) {
				// 经销商状态时，加盟申请按钮不可用
				getActivity().findViewById(R.id.my_myjoin).setEnabled(false);
				((TextView) getActivity().findViewById(R.id.txt_shenqing)).setText("已通过");
				getActivity().findViewById(R.id.shenqingiv).setVisibility(View.GONE);
			} else {
				getActivity().findViewById(R.id.my_myjoin).setEnabled(true);
				((TextView) getActivity().findViewById(R.id.txt_shenqing)).setText("");
				getActivity().findViewById(R.id.shenqingiv).setVisibility(View.VISIBLE);
			}

			String Userqm = mLogBean.getData().getUserqm();
			TextView mQm = (TextView) getActivity().findViewById(R.id.my_tv_qianming);
			if (!Userqm.equals(null)) {
				mQm.setVisibility(View.VISIBLE);
				mQm.setText(Userqm);
			} else {
				mQm.setVisibility(View.GONE);
			}

			headPortrait = mLogBean.getData().getHeadPortrait();

			ImageView iv = (ImageView) getActivity().findViewById(R.id.iv_head_portrait);
			ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
					.setFailureDrawableId(R.drawable.ic_touxiangcilcle).setCircular(true).setCrop(true).build();
			x.image().bind(iv, headPortrait, imageOptions);

		}

	}

	/**
	 * 获取订单数量
	 */
	private void GetCustomerOrderTotal() {
		String s = ((BaseActivity) getActivity()).getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Order/Total_New?UserId=" + s + "&Sign=" + NetUtils.getSign(map)
				+ "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new GetCustomerOrderTotalCallBack(), getActivity());

	}

	/**
	 * 获取订单数量回调
	 */
	class GetCustomerOrderTotalCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject countData = jo.getJSONObject("Data");

					if (countData.has("ToPayTotal") && countData.getInt("ToPayTotal") > 0) {
						mUnpaidBadge.setText(countData.getString("ToPayTotal"));
						mUnpaidBadge.setVisibility(View.VISIBLE);
					} else {
						mUnpaidBadge.setText("0");
						mUnpaidBadge.setVisibility(View.INVISIBLE);
					}

					if (countData.has("ToSendTotal") && countData.getInt("ToSendTotal") > 0) {
						mUnshippingBadge.setText(countData.getString("ToSendTotal"));
						mUnshippingBadge.setVisibility(View.VISIBLE);
					} else {
						mUnshippingBadge.setText("0");
						mUnshippingBadge.setVisibility(View.INVISIBLE);
					}

					if (countData.has("ToReceiveTotal") && countData.getInt("ToReceiveTotal") > 0) {
						mUnreceivingBadge.setText(countData.getString("ToReceiveTotal"));
						mUnreceivingBadge.setVisibility(View.VISIBLE);
					} else {
						mUnreceivingBadge.setText("0");
						mUnreceivingBadge.setVisibility(View.INVISIBLE);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 判断是否为经销商
	 */
	private void getIsDistribution() {
		String s = ((BaseActivity) getActivity()).getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("MDUserId", ((BaseActivity) getActivity()).getBindingShop());

		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GDSExists?UserId=" + s + "&MDUserId="
				+ ((BaseActivity) getActivity()).getBindingShop() + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new IsDistributionCallBack(), getActivity());

	}

	/**
	 * 判断是否为经销商回调
	 * 
	 * @author ZWJ
	 *
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
						distribution = "0";
						getActivity().findViewById(R.id.customer).setVisibility(View.VISIBLE);
						getActivity().findViewById(R.id.distribution).setVisibility(View.GONE);
						// 钱包隐藏
						getActivity().findViewById(R.id.my_money).setVisibility(View.GONE);
						// 分销管理
						getActivity().findViewById(R.id.my_fxgl).setVisibility(View.GONE);

						mIV.setVisibility(View.GONE);

					} else if (Data.equals("1")) {
						// 分销商
						distribution = "1";
						getActivity().findViewById(R.id.customer).setVisibility(View.GONE);
						getActivity().findViewById(R.id.distribution).setVisibility(View.VISIBLE);

						getActivity().findViewById(R.id.my_money).setVisibility(View.VISIBLE);
						// 分销管理
						getActivity().findViewById(R.id.my_fxgl).setVisibility(View.VISIBLE);

						mIV.setVisibility(View.VISIBLE);
						mIV.setImageResource(R.drawable.ic_my_fxgl);

						getDistributionInfo();

					} else if (Data.equals("2")) {
						// 一般用户 可申请分销商
						distribution = "2";
						getActivity().findViewById(R.id.customer).setVisibility(View.VISIBLE);
						getActivity().findViewById(R.id.distribution).setVisibility(View.GONE);

						getActivity().findViewById(R.id.my_money).setVisibility(View.GONE);
						// 分销管理
						getActivity().findViewById(R.id.my_fxgl).setVisibility(View.GONE);
						mIV.setVisibility(View.VISIBLE);
						mIV.setImageResource(R.drawable.ic_my_fxsq);
					}

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 获取分销商信息
	 */
	private void getDistributionInfo() {
		String s = ((BaseActivity) getActivity()).getLoginUserId();
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("UserId", s);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GetGDSUserInfo?UserId=" + s + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new DistributionInfoCallBack(), getActivity());
	}

	/**
	 * 获取分销商信息回调
	 */
	class DistributionInfoCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {
			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");
					// 分销商绑定的店铺
					String MDGDSR_MDUserID = Data.getString("MDGDSR_MDUserID");
					// 把分销商绑定的门店 保存。
					((BaseActivity) getActivity()).putSharedPreferences(Constants.FXSMD, MDGDSR_MDUserID);

					((BaseActivity) getActivity()).putSharedPreferences(Constants.KEY_FXS_LEVEL,
							Data.getString("GDSType_Level"));

					String mDisTypeName = Data.getString("GDSType_Name");
					// 会员等级
					((TextView) getActivity().findViewById(R.id.tv_dis_type_name)).setText("铜牌分销会员");
					// 分销商等级图标
					((BaseActivity) getActivity()).setImageURl(R.id.iv_type, Data.getString("GDSType_Img"));

					// 会员头像
					((BaseActivity) getActivity()).ImageFromUrl(headPortrait, R.id.dis_iv);
					// 分销会员编号
					((TextView) getActivity().findViewById(R.id.tv_fxhybh))
							.setText("分销会员编号：" + Data.getString("MDGDSM_Number"));
					// 保存分销商号码
					((BaseActivity) getActivity()).putSharedPreferences(Constants.KEY_FXS_NUMBER,
							Data.getString("MDGDSM_Number"));

					// 店铺名称
					((TextView) getActivity().findViewById(R.id.tv_shop_name)).setText(name);
					// 我的钱包
					mMyMoney.setText("我的收入：¥ " + Data.getString("MDGDSM_ToMyMoney"));

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * 获取数字角标控件
	 * 
	 * @param id
	 * @return
	 */
	protected MyBadgeView getBadgeView(int id) {
		View v = getActivity().findViewById(id);
		MyBadgeView badge1 = new MyBadgeView(getActivity(), v);
		// 需要显示的提醒类容
		badge1.setBadgePosition(MyBadgeView.POSITION_TOP_RIGHT);// 显示的位置.右上角,BadgeView.POSITION_BOTTOM_LEFT,下左，还有其他几个属性
		badge1.setTextColor(Color.WHITE); // 文本颜色
		badge1.setBadgeBackgroundColor(getResources().getColor(R.color.main_color)); // 提醒信息的背景颜色，自己设置
		badge1.setTextSize(10); // 文本大小
		badge1.setGravity(Gravity.CENTER);
		badge1.setBadgeMargin(3);// 各边间隔
		badge1.show();
		return badge1;
	}

	@Event(value = { R.id.my_btn_aftermarket, R.id.my_tv_all_order, R.id.my_btn_pending_payment,
			R.id.my_btn_pending_payment, R.id.my_btn_unshipping, R.id.my_btn_unreceiving, R.id.iv_head_portrait,
			R.id.iv_set, R.id.my_iv_setting, R.id.my_myaddress, R.id.my_mycode, R.id.my_myfavorite, R.id.my_myjoin,
			R.id.my_mynearbystore, R.id.my_fxgl, R.id.my_red_pagket, R.id.iv_fxsq, R.id.my_money, R.id.dis_iv,
			R.id.ll_comission_info, R.id.tv_shop_name, R.id.iv_qrcode })
	private void getEvevt(View v) {
		Intent i = new Intent();
		switch (v.getId()) {
		case R.id.my_btn_aftermarket:
			// 售后
			if (isLogged) {
				i.setClass(getActivity(), ActivityRetractList.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_tv_all_order:
			// 所有订单
			if (isLogged) {
				i.setClass(getActivity(), ActivityOrderList.class);
				i.putExtra("status", "0");
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_btn_pending_payment:
			// 待付款
			if (isLogged) {
				i.setClass(getActivity(), ActivityOrderList.class);
				i.putExtra("status", "1");
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_btn_unshipping:
			// 待发货
			if (isLogged) {
				i.setClass(getActivity(), ActivityOrderList.class);
				i.putExtra("status", "2");
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_btn_unreceiving:
			// 待收货
			if (isLogged) {
				i.setClass(getActivity(), ActivityOrderList.class);
				i.putExtra("status", "3");
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.iv_head_portrait:
			// 个人资料
			if (isLogged) {
				i.setClass(getActivity(), ActivityMyself.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.iv_set:
		case R.id.my_iv_setting:
			// 设置
			if (isLogged) {
				i.setClass(getActivity(), ActivitySetting.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_myaddress:
			// 地址管理
			if (isLogged) {
				i.setClass(getActivity(), ActivityAddressList.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_mycode:
			// 扫码直达
			if (isLogged) {
				Intent intent = new Intent(getActivity(), CaptureActivity.class);
				startActivityForResult(intent, 1);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_myfavorite:
			// 我的收藏
			if (isLogged) {
				i.setClass(getActivity(), ActivityMyFavourite.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_myjoin:
			// 我的加盟
			if (isLogged) {
				((BaseActivity) getActivity()).forward(ActivityJoin.class);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_mynearbystore:
			// 家门口的免税店
			i.putExtra("type", 2);
			i.setClass(getActivity(), ActivityShopList.class);
			startActivity(i);

			break;
		case R.id.my_fxgl:
			// 分销管理
			if (isLogged) {
				i.setClass(getActivity(), ActivityFXGL.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_red_pagket:
			// 我的红包
			if (isLogged) {
				i.setClass(getActivity(), ActivityRedPagketList.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;

		case R.id.iv_fxsq:
			// 申请成为分销商
			if (isLogged) {
				if (distribution.equals("1")) {
					// 分销管理
					i.setClass(getActivity(), ActivityFXGL.class);
					startActivity(i);
				} else if (distribution.equals("2")) {
					// 申请分销
					i.setClass(getActivity(), ActivityFXIntroduce.class);
					startActivity(i);
				}
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.my_money:
			// 我的钱包
			if (isLogged) {
				i.setClass(getActivity(), ActivityFXWallet.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.dis_iv:
		case R.id.ll_comission_info:
		case R.id.tv_shop_name:
			// 会员信息
			if (isLogged) {
				i.setClass(getActivity(), ActivityFXSPersonalCenter.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		case R.id.iv_qrcode:
			// 二维码界面
			if (isLogged) {
				i.setClass(getActivity(), ActivityFXQRcode.class);
				startActivity(i);
			} else {
				gotoLoginActivity();
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 跳转到登陆界面
	 */
	private void gotoLoginActivity() {
		((BaseActivity) getActivity()).forward(ActivityLogin.class);
	}

	/**
	 * 当界面重新展示时（fragment.show）,调用onrequest刷新界面
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (!hidden) {
			isLogged = ((BaseActivity) getActivity()).isLogged();
			if (isLogged) {
				init();
			} else {
				noLogged();
			}
		} else {
		}
	}

}
