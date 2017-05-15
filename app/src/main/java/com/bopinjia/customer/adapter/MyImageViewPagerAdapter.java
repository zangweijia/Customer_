package com.bopinjia.customer.adapter;

import java.util.ArrayList;
import java.util.List;

import com.bopinjia.customer.activity.ActivitySpecialWebView;
import com.bopinjia.customer.activity.StartActivity;
import com.bopinjia.customer.bean.ImageViewListBean;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MyImageViewPagerAdapter extends PagerAdapter {

	private List<View> views;
	Context context;
	List<ImageViewListBean> list;
	private int index;

	public MyImageViewPagerAdapter(Context context, List<View> views, List<ImageViewListBean> list) {
		this.views = views;
		this.context = context;
		this.list = list;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(views.get(arg1 % views.size()));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		// 注意这里一定要返回一个稍微大点值,不然滑到顶就滑不动了
		return views.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(views.get(arg1 % views.size()), 0);
		index = arg1;
		views.get(arg1 % views.size()).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (list.get(index % views.size()).getUrl().equals("")) {
				} else {
					Intent i = new Intent();
					i.putExtra("name", list.get(index % views.size()).getName());
					i.putExtra("url", list.get(index % views.size()).getImg());
					i.setClass(context, ActivitySpecialWebView.class);
					context.startActivity(i);
				}

			}
		});

		return views.get(arg1 % views.size());
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {

	}

}