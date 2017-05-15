package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.List;


import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.ImageViewPagerAdapter;
import com.bopinjia.customer.constants.Constants;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class ActivityWelcome extends BaseActivity {

	private CirclePageIndicator mIndicator;
	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_welcome);

		mIndicator = (CirclePageIndicator) findViewById(R.id._indicator);
		mPager = (ViewPager) findViewById(R.id._pager);

		// --------顶部轮播-----start
		List<View> images = new ArrayList<View>();
		ImageView imageView = new ImageView(this);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setImageResource(R.drawable.welcome1);
		images.add(imageView);

		imageView = new ImageView(this);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setImageResource(R.drawable.welcome2);
		images.add(imageView);

		imageView = new ImageView(this);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		imageView.setImageResource(R.drawable.welcome3);
		images.add(imageView);
		mPager.setAdapter(new ImageViewPagerAdapter(images));

		mIndicator.setViewPager(mPager);
		mIndicator.setOnPageChangeListener(mPageChangeListener);

		findViewById(R.id.go_start).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 点击体验
				putSharedPreferences("FirstEnter", "1");

				String shopid = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
				if (shopid == null || shopid.length() <= 0) {
					Intent i = new Intent();
					i.putExtra("type", 1);
					forward(ActivityShopList.class,i);
					finish();
				} else {
					forward(ActivityHome.class);
					finish();
				}

			}
		});

	}

	OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			if (arg0 == 0) {
				findViewById(R.id.go_start).setVisibility(View.GONE);
			} else if (arg0 == 1) {
				findViewById(R.id.go_start).setVisibility(View.GONE);
			} else if (arg0 == 2) {
				findViewById(R.id.go_start).setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub

		}
	};

}
