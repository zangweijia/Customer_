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
import com.bopinjia.customer.fragment.ShopAllProducts;
import com.bopinjia.customer.fragment.ShopMainFragment;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.view.MyScrollView;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.fragment_goods_in_stock)
public class GoodsInStock extends Fragment implements View.OnClickListener {

    @ViewInject(R.id.iv_shop_head)
    private ImageView mShopHead;

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
            LinearLayout mTitle = (LinearLayout) getActivity().findViewById(R.id.title);
            mTitle.setVisibility(View.VISIBLE);
        } else {
            // onPase
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinearLayout mTitle = (LinearLayout) getActivity().findViewById(R.id.title);
        mTitle.setVisibility(View.VISIBLE);
        initClick();
        Fragment main = new ShopMainFragment();
        Fragment AllProduct = new ShopAllProducts();
        Fragment main1 = new ShopMainFragment();
        Fragment AllProduct1 = new ShopAllProducts();

        fragments.add(main);
        fragments.add(AllProduct);
        fragments.add(main1);
        fragments.add(AllProduct1);

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

        String[] titles = new String[]{"店铺首页", "全部商品", "店铺首页", "全部商品"};
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


}
