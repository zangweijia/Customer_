package com.bopinjia.customer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.GoodsInfoFragment.showGds;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.ItemWebView;

/**
 * item页ViewPager里的详情Fragment
 */
public class GoodsDetailFragment extends Fragment {

	private View rootView;
	private String isFreeShipping;
	private String skuid;
	private String userid;

	public static GoodsDetailFragment newInstance(String skuid, String isFreeShipping) {
		GoodsDetailFragment newFragment = new GoodsDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString("skuid", skuid);
		bundle.putString("isFreeShipping", isFreeShipping);
		newFragment.setArguments(bundle);
		return newFragment;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			skuid = args.getString("skuid");
			isFreeShipping = args.getString("isFreeShipping");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_goods_detail, null);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ShowGDS();
	}

	/**
	 * 得到商品SKU详情 增加是否显示分销商佣金
	 */
	private void ShowGDS() {
		if (((BaseActivity) getActivity()).isLogged()) {
			userid = ((BaseActivity) getActivity()).getLoginUserId();
		} else {
			userid = "0";
		}
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new HashMap<String, String>();
		map.put("SkuId", skuid);
		map.put("UserId", userid);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);

		String url = Constants.WEBAPI_ADDRESS + "api/Product/ShowGDS?SkuId=" + skuid + "&UserId=" + userid + "&Sign="
				+ NetUtils.getSign(map) + "&Ts=" + Ts;

		XutilsHttp.getInstance().get(url, null, new showGds(), getActivity());

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

					ItemWebView mDetail = (ItemWebView) getActivity().findViewById(R.id.webviews);
//					mDetail.getSettings().setSupportZoom(true);
//					mDetail.getSettings().setUseWideViewPort(true);
					mDetail.loadData(sku.getString("SkuDesc"), "text/html; charset=UTF-8", null);

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}
}
