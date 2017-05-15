package com.bopinjia.customer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bopinjia.customer.R;

import org.xutils.view.annotation.ContentView;

@ContentView(R.layout.fragment_shop_main)
public class ShopMainFragment extends LazyFragment {


    // 标志位，标志已经初始化完成。
    private boolean isPrepared;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_main, null);
        lazyLoad();
        return view;
    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        } else {

        }
    }

}
