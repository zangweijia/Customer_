package com.bopinjia.customer.activity;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * 启动图后的广告图
 * 
 * @author Administrator
 *
 */
public class ActivityAdvertisement extends BaseActivity {

	private ImageView mAdvertisement;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_advertisement);
		initWindow();
		String url = getIntent().getStringExtra("url");
		mAdvertisement = (ImageView) findViewById(R.id.iv_advertisement);

		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(mAdvertisement, url, imageOptions);

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
					GoNext();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

	@TargetApi(19)
	private void initWindow() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
	}

	private void GoNext() {
		String s = getBopinjiaSharedPreference("FirstEnter");
		if (s != null && s.equals("1")) {
			String shopid = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
			if (shopid == null || shopid.length() <= 0) {
				Intent i = new Intent();
				i.putExtra("type", 1);
				forward(ActivityShopList.class, i);
				finish();
			} else {
				forward(ActivityHome.class);
				overridePendingTransition(R.anim.logo_anim_in, R.anim.logo_anim_out);
				finish();
			}
		} else {
			forward(ActivityWelcome.class);
			overridePendingTransition(R.anim.logo_anim_in, R.anim.logo_anim_out);
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		mAdvertisement.setImageBitmap(null);
		super.onDestroy();
	}
}
