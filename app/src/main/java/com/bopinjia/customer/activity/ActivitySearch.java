package com.bopinjia.customer.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.StringUtils;
import com.bopinjia.customer.view.SearchTipsGroupView;
import com.bopinjia.customer.view.SearchTipsGroupViewOnItemClick;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ArrayAdapter;

public class ActivitySearch extends BaseActivity {

	/**
	 * 搜索框
	 */
	@ViewInject(R.id.edt_search)
	private EditText mEdtSearch;
	/**
	 * 搜索按钮
	 */
	@ViewInject(R.id.tv_search)
	private TextView mTvSearch;
	/**
	 * 返回
	 */
	@ViewInject(R.id.iv_back)
	private ImageView mTvCancel;
	/**
	 * 清空按钮
	 */
	@ViewInject(R.id.tv_delete)
	private TextView mTvDelete;

	/**
	 * 热词搜索
	 */
	@ViewInject(R.id.search_tips)
	private SearchTipsGroupView HotView;
	private List<String> mHotSearchList;
	/**
	 * 最近搜索
	 */
	@ViewInject(R.id.grd_lately)
	private SearchTipsGroupView mLatestSearchView;
	private List<String> mLateSearchList;
	// 是否是直邮 /0现货 /1直邮 /3现货
	private String IsFreeShipping;

	private ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_search);
		x.view().inject(this);
		int type = getIntent().getIntExtra("type", 1);

		if (type == 0) {
			IsFreeShipping = "0";
		} else if (type == 1) {
			IsFreeShipping = "1";
		}

		mHotSearchList = new ArrayList<String>();
		mHotSearchList.add("花王");
		mHotSearchList.add("纸尿裤");
		mHotSearchList.add("贝亲");
		mHotSearchList.add("嘉宝");
		mHotSearchList.add("狮王牙膏");
		mHotSearchList.add("保湿面膜");
		// 热词
		HotView.initViews(mHotSearchList, new SearchTipsGroupViewOnItemClick() {

			@Override
			public void SearchTipsGroupViewonClick(int position) {
				// TODO Auto-generated method stub
				setSearchWord(mHotSearchList.get(position));
			}
		});

		// 返回按钮
		mTvCancel.setOnClickListener(this);
		// 删除搜索内容
		mTvDelete.setOnClickListener(this);
		// 搜索按钮
		mTvSearch.setOnClickListener(this);
		
		// 先把Linearlayout 的子view移除 否则会再创建一个linearlayout 删除的时候会报错
		mLatestSearchView.deleteAllView();
		// 最近搜索
		mLateSearchList = getLatestSearch();
		mLatestSearchView.initViews(mLateSearchList, new SearchTipsGroupViewOnItemClick() {

			@Override
			public void SearchTipsGroupViewonClick(int position) {
				// TODO Auto-generated method stub
				setSearchWord(mLateSearchList.get(position));
			}
		});

	}

	 

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_delete:
			if (mLateSearchList != null && !mLateSearchList.isEmpty()) {
				removeSharedPreferences(Constants.KEY_PREFERENCE_LATEST_SEARCH);
				mLateSearchList.clear();
				mLatestSearchView.deleteAllView();
				showToast("已删除最近搜索");
			} else {
				showToast("最近搜索为空");
			}
			break;
		case R.id.tv_search:
			setSearchWord(mEdtSearch.getText().toString().trim());
			break;
		case R.id.iv_back:
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 记录最近搜索内容
	 * 
	 * @param searchWord
	 */
	public void addLatestSearch(String searchWord) {
		String latestSearch = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_LATEST_SEARCH);

		if (!StringUtils.isNull(latestSearch)) {
			if (!latestSearch.equals(searchWord)) {

				if (latestSearch.indexOf(searchWord) != -1) {
					latestSearch = latestSearch.replace(searchWord + ",", "");
				}

				latestSearch = searchWord + "," + latestSearch;
			}
		} else {
			latestSearch = searchWord;
		}

		Editor shared = getSharedPreferences(Constants.KEY_PREFERENCE, 0).edit();
		shared.putString(Constants.KEY_PREFERENCE_LATEST_SEARCH, latestSearch);
		shared.commit();
	}

	/**
	 * 设置检索关键词
	 * 
	 * @param searchWord
	 */
	private void setSearchWord(String searchWord) {
		if (!StringUtils.isNull(searchWord)) {
			addLatestSearch(searchWord);
			Intent intent = new Intent();
			intent.putExtra("IsFreeShipping", IsFreeShipping);
			intent.putExtra("SearchWord", searchWord);
			forward(ActivityProductListNew.class, intent);
			finish();
		} else {
			showToast("请输入搜索名称");
		}

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// 这里注意要作判断处理，ActionDown、ActionUp都会回调到这里，不作处理的话就会调用两次
		if (KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {
			// 处理事件
			String searchWord = ((EditText) findViewById(R.id.edt_search)).getText().toString();
			setSearchWord(searchWord);
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
