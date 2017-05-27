package com.bopinjia.customer.mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityCategory;
import com.bopinjia.customer.activity.ActivitySearch;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.goodsInStockFragment.ManagerRecommended;
import com.bopinjia.customer.fragment.goodsInStockFragment.NewProductRecommended;
import com.bopinjia.customer.fragment.goodsInStockFragment.ShopAllProducts;
import com.bopinjia.customer.fragment.goodsInStockFragment.ShopMainFragment;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.MyScrollView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@ContentView(R.layout.fragment_goods_in_stock)
public class GoodsInStock extends Fragment implements View.OnClickListener {

    @ViewInject(R.id.iv_shop_head)
    private ImageView mShopHead;

    @ViewInject(R.id.iv_bg_shop_info)
    private ImageView mBgShop;

    @ViewInject(R.id.tv_shop_name)
    private TextView mShopName;

    @ViewInject(R.id.tv_intorduce)
    private TextView mIntorduce;

    @ViewInject(R.id.tabs_main)
    private PagerSlidingTabStrip mTabs;

    @ViewInject(R.id.scrollview)
    private MyScrollView mScrollview;

    @ViewInject(R.id.vp)
    public ViewPager mViewpager;

    private List<Fragment> fragments = new ArrayList<Fragment>();
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return x.view().inject(this, inflater, container);
    }

    /**
     * 当界面重新展示时（fragment.show）,调用onrequest刷新界面
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            // onResume
            getMDInfo();
            LinearLayout ll= (LinearLayout)getActivity().findViewById(R.id.title);
            ll.setVisibility(View.VISIBLE);
        } else {
            // onPase
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }


    private void init() {
        initClick();
        getMDInfo();
        Fragment shopMainFragment = new ShopMainFragment();
        Fragment shopAllProducts = new ShopAllProducts();
        Fragment managerRecommended = new ManagerRecommended();
        Fragment newProductRecommended = new NewProductRecommended();

        fragments.add(shopMainFragment);
        fragments.add(shopAllProducts);
        fragments.add(managerRecommended);
        fragments.add(newProductRecommended);

        mViewpager.setAdapter(new mAdapter(getChildFragmentManager(), fragments));
        mTabs.setViewPager(mViewpager);
    }

    private void initClick() {
        ImageView mClassify = (ImageView) getActivity().findViewById(R.id._xianhuo_classify);
        LinearLayout mSearch = (LinearLayout) getActivity().findViewById(R.id._xianhuo_search);
        ImageView mScan = (ImageView) getActivity().findViewById(R.id._xianhuo_scan);
        mClassify.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mScan.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
            default:
                break;
        }
    }

    class mAdapter extends FragmentPagerAdapter {

        String[] titles = new String[]{"店铺首页", "全部商品", "店长推荐", "新品推荐"};
        private List<Fragment> fragments;

        public mAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }


    /**
     * 获取门店现货区首页信息
     */
    private void getMDInfo() {
        String Ts = MD5.getTimeStamp();
        String Mid = ((BaseActivity) getActivity()).getBindingShop();
        String Gdsid = ((BaseActivity) getActivity()).getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_GDSUSERID);
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("MDUserId", Mid);
        map.put("GDSUserId", Gdsid);
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
        String url = Constants.WEBAPI_ADDRESS + "api/Store/InfoXh?MDUserId=" + Mid + "&GDSUserId=" + Gdsid + "&Sign=" + Sign + "&Ts=" + Ts;

        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {

                        ((BaseActivity) getActivity()).setImageURl(mShopHead, jo.getJSONObject("Data").getString("MXhLogo"));

                        ((BaseActivity) getActivity()).setImageURl(mBgShop, jo.getJSONObject("Data").getString("MXhBanner"));

                        mShopName.setText(jo.getJSONObject("Data").getString("MXhShopName"));
                        mIntorduce.setText(jo.getJSONObject("Data").getString("MXhShopMark"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

}
