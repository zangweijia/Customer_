package com.bopinjia.customer.mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityCategory;
import com.bopinjia.customer.activity.ActivityHome;
import com.bopinjia.customer.activity.ActivityLogin;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.ActivitySearch;
import com.bopinjia.customer.activity.ActivityThreeClassification;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.StatusBarUtils;

import org.xutils.view.annotation.ContentView;
import org.xutils.x;

@ContentView(R.layout.fragment_activity_main_zhiyou)
public class MainZYFragment extends Fragment implements OnClickListener {

	private WebView webView;
	private LinearLayout mNoNet;

	private String uuid;
	private String muserId;
	private String userid;
	private String url;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return x.view().inject(this, inflater, container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int i = R.color.bg_0000;

		StatusBarUtils.setWindowStatusBarColor(getActivity(), i);
		init();
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (this.getView() != null)
			this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
	}

	/**
	 * 当界面重新展示时（fragment.show）,调用onrequest刷新界面
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (!hidden) {
			LinearLayout mTitle = (LinearLayout)getActivity().findViewById(R.id.title);
			mTitle.setVisibility(View.GONE);
			uuid = ((BaseActivity) getActivity()).getIMEI1();
			boolean isLogged = ((BaseActivity) getActivity()).isLogged();
			if (isLogged) {
				userid = ((BaseActivity) getActivity()).getLoginUserId();
				getActivity().findViewById(R.id.iv_red).setVisibility(View.GONE);
			} else {
				userid = "0";
				getActivity().findViewById(R.id.iv_red).setVisibility(View.VISIBLE);
			}
			muserId = ((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
			url = "http://m.bopinwang.com/zyindex.aspx?MuserID=" + muserId + "&CuserID=" + userid + "&UUID=" + uuid;
			webView.loadUrl(url);
		} else {
			// 相当于Fragment的onPause
		}
	}

	private void init() {
		uuid = ((BaseActivity) getActivity()).getIMEI1();
		boolean isLogged = ((BaseActivity) getActivity()).isLogged();
		getActivity().findViewById(R.id.iv_red).setOnClickListener(this);
		if (isLogged) {
			userid = ((BaseActivity) getActivity()).getLoginUserId();
			getActivity().findViewById(R.id.iv_red).setVisibility(View.GONE);

		} else {
			userid = "0";
			getActivity().findViewById(R.id.iv_red).setVisibility(View.VISIBLE);
		}
		muserId = ((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
		url = "http://m.bopinwang.com/zyindex.aspx?MuserID=" + muserId + "&CuserID=" + userid + "&UUID=" + uuid;

		// 直邮区分类
		getActivity().findViewById(R.id._zhiyou_classify).setOnClickListener(this);
		// 直邮区搜索
		getActivity().findViewById(R.id._zhiyou_search).setOnClickListener(this);
		// 直邮区扫码
		getActivity().findViewById(R.id._zhiyou_scan).setOnClickListener(this);
		// 没有网络时刷新界面按钮
		getActivity().findViewById(R.id.refresh).setOnClickListener(this);
		// 没有网络时显示的LinearLayout（默认隐藏）
		mNoNet = (LinearLayout) getActivity().findViewById(R.id.ll_no_net);

		// webview
		webView = (WebView) getActivity().findViewById(R.id._webview);

		WebSettings webSettings = webView.getSettings();
		webSettings.setSavePassword(false);
		webSettings.setSaveFormData(false);
		webSettings.setAppCacheEnabled(false);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setJavaScriptEnabled(true);
		// 开启 DOM storage 功能
		webSettings.setDomStorageEnabled(true);
		webSettings.setSupportZoom(false);
		webView.loadUrl(url);
		webView.addJavascriptInterface(this, "xianhuo");
		// 屏蔽webview长按 可复制功能
		webView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});
		// 给webview 添加监听
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				// webview 加载失败
				webView.setVisibility(View.GONE);
				mNoNet.setVisibility(View.VISIBLE);
			}

		});

	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		webView.pauseTimers();
	}

	@Override
	public void onResume() {
		super.onResume();
		webView.resumeTimers();
		//			隐藏HomeActivity 顶部
		LinearLayout mTitle = (LinearLayout) getActivity().findViewById(R.id.title);
		mTitle.setVisibility(View.GONE);
	}

	/**
	 * 从h5页面拿到点击分类的id
	 * 
	 * @param subid
	 */
	@JavascriptInterface
	public void GetShopProductCategory(String url) {
		Intent i = new Intent();
		i.putExtra("url", url);
		i.putExtra("IsFreeShipping", "1");
		i.setClass(getActivity(), ActivityThreeClassification.class);
		startActivity(i);
	}

	@JavascriptInterface
	public void AddSuccess(String str) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
					((ActivityHome) getActivity()).getCartnumber();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();

	}

	/**
	 * 从h5页面拿到点击商品的skuid
	 * 
	 * @param subid
	 */
	@JavascriptInterface
	public void GetShopProductDetaile(String url) {
		Intent i = new Intent();
		i.putExtra("url", url);
		i.putExtra("IsFreeShipping", "1");
		i.setClass(getActivity(), ActivityProductDetailsNew.class);
		startActivity(i);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id._zhiyou_classify:
			// 分类
			Intent toClass = new Intent();
			toClass.putExtra("type", 1);
			toClass.setClass(getActivity(), ActivityCategory.class);
			startActivity(toClass);
			break;
		case R.id._zhiyou_search:
			// 搜索
			Intent toSearch = new Intent();
			toSearch.putExtra("type", 1);
			toSearch.setClass(getActivity(), ActivitySearch.class);
			startActivity(toSearch);
			break;
		case R.id._zhiyou_scan:
			// 扫码
			Intent toScan = new Intent(getActivity(), CaptureActivity.class);
			startActivityForResult(toScan, 1);
			break;
		case R.id.refresh:
			webView.reload();
			webView.setVisibility(View.VISIBLE);
			mNoNet.setVisibility(View.GONE);
			break;

		case R.id.iv_red:

			((BaseActivity) getActivity()).forward(ActivityLogin.class);
			break;

		default:
			break;
		}

	}

}
