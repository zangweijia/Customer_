package com.bopinjia.customer.mainpage;

import org.xutils.x;
import org.xutils.view.annotation.ContentView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityCategory;
import com.bopinjia.customer.activity.ActivityHome;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.ActivitySearch;
import com.bopinjia.customer.activity.ActivityThreeClassification;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.qrcode.CaptureActivity;

import android.content.Intent;
import android.os.Build;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

@ContentView(R.layout.fragment_activity_main_xianhuo)
public class MainXHFragment extends Fragment implements OnClickListener {

    private WebView webView;
    private LinearLayout mNoNet;
    private String uuid;
    private String userid;
    private String muserId;
    private String url;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return x.view().inject(this, inflater, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
        uuid = ((BaseActivity) getActivity()).getIMEI1();
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
     * 当界面重新展示时（fragment.show）,调用onrequest刷新界面
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            boolean isLogged = ((BaseActivity) getActivity()).isLogged();
            if (isLogged) {
                userid = ((BaseActivity) getActivity()).getLoginUserId();
            } else {
                userid = "0";
            }
            muserId = ((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
            url = "http://m.bopinwang.com/index.aspx?MuserID=" + muserId + "&CuserID=" + userid + "&UUID=" + uuid;

            webView.loadUrl(url);
        } else {
            // 相当于Fragment的onPause
        }
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        if (this.getView() != null)
            this.getView().setVisibility(menuVisible ? View.VISIBLE : View.GONE);
    }

    private void init() {

        boolean isLogged = ((BaseActivity) getActivity()).isLogged();
        if (isLogged) {
            userid = ((BaseActivity) getActivity()).getLoginUserId();
        } else {
            userid = "0";
        }
        muserId = ((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
        url = "http://m.bopinwang.com/index.aspx?MuserID=" + muserId + "&CuserID=" + userid + "&UUID=" + uuid;

        getActivity().findViewById(R.id.xh_refresh).setOnClickListener(this);
        getActivity().findViewById(R.id._xianhuo_classify).setOnClickListener(this);
        getActivity().findViewById(R.id._xianhuo_search).setOnClickListener(this);
        getActivity().findViewById(R.id._xianhuo_scan).setOnClickListener(this);

        mNoNet = (LinearLayout) getActivity().findViewById(R.id.xh_ll_no_net);

        webView = (WebView) getActivity().findViewById(R.id.xh_webview);
        if (Build.VERSION.SDK_INT >= 23) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            webView.setLayoutParams(layoutParams);
        } else {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            webView.setLayoutParams(layoutParams);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                // webview 加载失败
                webView.setVisibility(View.GONE);
                mNoNet.setVisibility(View.VISIBLE);
            }

        });

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
        i.putExtra("IsFreeShipping", "0");
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
     * 从h5页面拿到点击商品的url
     *
     * @param subid
     */
    @JavascriptInterface
    public void GetShopProductDetaile(String url) {
        Intent i = new Intent();
        i.putExtra("url", url);
        i.putExtra("IsFreeShipping", "0");
        i.setClass(getActivity(), ActivityProductDetailsNew.class);
        startActivity(i);
    }

    /**
     * 弹窗
     */
    @JavascriptInterface
    public void showToast() {
        Toast.makeText(getActivity(), "成功", 0).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id._xianhuo_classify:
                Intent toClass = new Intent();
                toClass.putExtra("type", 0);
                toClass.setClass(getActivity(), ActivityCategory.class);
                startActivity(toClass);
                break;
            case R.id._xianhuo_search:
                Intent toSearch = new Intent();
                toSearch.putExtra("type", 0);
                toSearch.setClass(getActivity(), ActivitySearch.class);
                startActivity(toSearch);
                break;
            case R.id._xianhuo_scan:
                Intent toScan = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(toScan, 1);
                break;
            case R.id.xh_refresh:
                webView.reload();
                webView.setVisibility(View.VISIBLE);
                mNoNet.setVisibility(View.GONE);
                break;
            default:
                break;

        }
    }

}
