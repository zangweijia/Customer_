package com.bopinjia.customer.mainpage;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Administrator on 2016/8/4 0004.
 */
public class CategoryFragmentAdapter extends FragmentPagerAdapter {

	
	private Fragment[] fragments = new Fragment[] { new CategoryFragmentSub(), new CategoryFragmentBrand() };

	public CategoryFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return fragments[position];
	}

	@Override
	public int getCount() {
		return fragments.length;
	}
}
