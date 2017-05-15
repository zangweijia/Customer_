package com.bopinjia.customer.adapter;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * 引导页面轮播图
 * @author Administrator
 *
 */
public class ImageViewPagerAdapter extends PagerAdapter {

	private List<View> views;

	public ImageViewPagerAdapter(List<View> views) {
		this.views = views;
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
