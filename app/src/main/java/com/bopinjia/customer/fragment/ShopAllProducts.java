package com.bopinjia.customer.fragment;


import com.bopinjia.customer.R;
import com.bopinjia.customer.view.CustomViewPager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ShopAllProducts extends LazyFragment {

//	CustomViewPager vp;
//
//	public ShopAllProducts(CustomViewPager vp) {
//		this.vp = vp;
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_shop_all_product, null);
//		vp.setObjectForPosition(view, 0);
		return view;
	}

	@Override
	protected void lazyLoad() {
		// TODO Auto-generated method stub

	}
}
