package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.ProductListAdapter;
import com.bopinjia.customer.adapter.ProductListModel;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityMyTrack extends BaseActivity {

	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_my_track);
		ListView mListView = (ListView) findViewById(R.id._lv_my_track);

		List<ProductListModel> list = new ArrayList<ProductListModel>();
		for (int i = 0; i < 7; i++) {
			ProductListModel m = new ProductListModel();
			m.setName("1");
			m.setCount("1");
			m.setMarket_price("111");
			m.setSale_price("1212");
			m.setThumbnails("http://scimg.jb51.net/allimg/160716/105-160G61F250436.jpg");
			list.add(m);
		}
		mListView.setAdapter(new ProductListAdapter(this, list));

		TextView back = (TextView) findViewById(R.id.btn_return);
		back.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			finish();
			break;

		default:
			break;
		}
	}

}
