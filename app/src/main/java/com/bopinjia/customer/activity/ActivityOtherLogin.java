package com.bopinjia.customer.activity;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityOtherLogin extends BaseActivity {

	private ImageView mRiv;
	private TextView tv_type_name;
	private TextView tv_name, tv_register, tv_lation;
	private String type;
	public static ActivityOtherLogin instance = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_other_login);
		instance= this;
		mRiv = (ImageView) findViewById(R.id.iv_head_portrait);

		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setCircular(true).setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(mRiv, getIntent().getStringExtra("url"), imageOptions);

		tv_type_name = (TextView) findViewById(R.id.type_name);

		type = getIntent().getStringExtra("type");
		if (type.equals("xl")) {
			tv_type_name.setText("亲爱的新浪用户：");
			
		} else if (type.equals("qq")) {
			tv_type_name.setText("亲爱的腾讯用户：");
		} else if (type.equals("wx")) {
			tv_type_name.setText("亲爱的微信用户：");
		}

		tv_name = (TextView) findViewById(R.id.name);
		tv_name.setText(getIntent().getStringExtra("name"));

		tv_register = (TextView) findViewById(R.id.tv_register);
		tv_register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 注册
				Intent i = new Intent();
				i.putExtra("uid", getIntent().getStringExtra("uid"));
				i.putExtra("unionid", getIntent().getStringExtra("unionid"));
				i.putExtra("type", type);
				forward(ActivityBindRegister.class, i);
			}
		});

		tv_lation = (TextView) findViewById(R.id.tv_lation);
		tv_lation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 关联
				Intent i = new Intent();
				i.putExtra("uid", getIntent().getStringExtra("uid"));
				i.putExtra("type", type);
				i.putExtra("unionid", getIntent().getStringExtra("unionid"));
				forward(ActivityBindLogin.class, i);
			}
		});
		findViewById(R.id.btn_return).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

}
