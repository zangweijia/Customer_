package com.bopinjia.customer.activity;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.ThreeClassificationProduct;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.BroadCastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActivityThreeClassification extends BaseActivity {

	private PagerSlidingTabStrip _Tabs;
	private ViewPager _vp;
	private List<tabModel> list;
	private String type = "2";
	private String CCode;
	private TextView mTiTleName;
	private String isFreeShipping;
	private MyAdapter adapter;
	PopupWindow popupWindow;
	private ListView lv_group;
	private TextView tvSort;
	private TextView mTitleLayout;
	private View view;
	private ImageView mIvPoint;

	private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_class_subs);
		setTitle();
		getParam();
		init();

	}

	/**
	 * 获取CCode 参数
	 */
	private void getParam() {
		if (getIntent().hasExtra("url")) {
			String url = getIntent().getStringExtra("url");
			Uri uri = Uri.parse(url);
			CCode = uri.getQueryParameter("CCode");
		}else{
			CCode= getIntent().getStringExtra("ccode");
		}
		
		isFreeShipping = getIntent().getStringExtra("IsFreeShipping");
		getSubCategoryNext(CCode);
	}

	private void setTitle() {
		View mTiTle = findViewById(R.id.include_title);
		TextView mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
		mTiTleBack.setOnClickListener(this);
		mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
		mTitleLayout = (TextView) mTiTle.findViewById(R.id.btn_edit);
		mTitleLayout.setOnClickListener(this);
		mTitleLayout.setBackgroundResource(R.drawable.ic_grid);
	}

	private void init() {
		mIvPoint = (ImageView) findViewById(R.id.iv_point);
		_Tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		_vp = (ViewPager) findViewById(R.id.viewPager);
		tvSort = (TextView) findViewById(R.id.tv_sort);
		tvSort.setOnClickListener(this);
		mIvPoint.setOnClickListener(this);
	}

	private void getSubCategoryNext(String code) {
		String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/SubCategoryNext?Code=" + code;
		XutilsHttp.getInstance().get(url, null, new getSubCategoryNextCallBack(), this);
	}

	class getSubCategoryNextCallBack implements XCallBack {

		@Override
		public void onResponse(String result) {

			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {
					JSONObject Data = jo.getJSONObject("Data");
					mTiTleName.setText(Data.getString("ParentCategoryCodeName"));
					JSONArray dataArray = Data.getJSONArray("ProductCategory");
					if (dataArray != null && dataArray.length() > 0) {
						list = new ArrayList<ActivityThreeClassification.tabModel>();
						tabModel modeln = new tabModel();
						modeln.setName("全部");
						modeln.setId(CCode);
						list.add(modeln);
						for (int i = 0; i < dataArray.length(); i++) {
							JSONObject data = dataArray.getJSONObject(i);
							tabModel model = new tabModel();
							model.setName(data.getString("CName"));
							model.setId(data.getString("CCode"));
							list.add(model);
						}

						adapter = new MyAdapter(getSupportFragmentManager());
						_vp.setAdapter(adapter);
						_Tabs.setViewPager(_vp);

					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		BroadCastManager.getInstance().unregisterReceiver(this, );
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			finish();
			break;

		case R.id.btn_edit:
			setTextViewDrawable(2);
			if (type.equals("1")) {
				type = "2";
				mTitleLayout.setBackgroundResource(R.drawable.ic_grid);
				adapter.notifyDataSetChanged();
				Intent intent = new Intent();
				intent.putExtra("type", type);
				intent.setAction("fragment_layout");
				BroadCastManager.getInstance().sendBroadCast(this, intent);
			} else if (type.equals("2")) {
				type = "1";
				mTitleLayout.setBackgroundResource(R.drawable.ic_list);
				adapter.notifyDataSetChanged();
				Intent intent = new Intent();
				intent.putExtra("type", type);
				intent.setAction("fragment_layout");
				BroadCastManager.getInstance().sendBroadCast(this, intent);
			}
			break;
		case R.id.iv_point:
		case R.id.tv_sort:
			if (isup) {
				setTextViewDrawable(2);
				if (popupWindow == null) {
				} else {
					popupWindow.dismiss();
				}
			} else {
				setTextViewDrawable(1);
				showPopupWindow(tvSort);
			}
			break;
		default:
			break;
		}
	}

	private boolean isup = false;

	/**
	 * 设置排序旁边的按钮
	 * 
	 * @param i
	 *            i= 1显示up i=2显示down
	 */
	private void setTextViewDrawable(int i) {
		if (i == 1) {
			isup = true;
			mIvPoint.setImageResource(R.drawable.ic_up);
		} else if (i == 2) {
			isup = false;
			mIvPoint.setImageResource(R.drawable.ic_down);
		}
	}

	private void showPopupWindow(View parent) {
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = layoutInflater.inflate(R.layout.wj_list_group_class_sub, null);
			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.wj_item_class_sub_sort, R.id.tv_name,
					new String[] { "综合排序", "销量从高到低", "销量从低到高", "价格从高到低", "价格从低到高" });

			lv_group.setAdapter(adapter);
			popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		popupWindow.setOutsideTouchable(false);
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		popupWindow.setBackgroundDrawable(dw);
		popupWindow.setFocusable(false);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		popupWindow.showAsDropDown(parent, 0, 4);
		view.findViewById(R.id.view_null).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popupWindow.dismiss();
				setTextViewDrawable(2);
			}
		});
		lv_group.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (popupWindow != null) {
					setTextViewDrawable(2);
					Intent intent = new Intent();
					if (position == 0) {
						intent.putExtra("sort", "0");
					} else if (position == 1) {
						intent.putExtra("sort", "1");
					} else if (position == 2) {
						intent.putExtra("sort", "2");
					} else if (position == 3) {
						intent.putExtra("sort", "3");
					} else if (position == 4) {
						intent.putExtra("sort", "4");
					}

					intent.setAction("fragment_sort");
					BroadCastManager.getInstance().sendBroadCast(ActivityThreeClassification.this, intent);
					popupWindow.dismiss();
				} else {

				}

			}
		});

	}

	public class MyAdapter extends FragmentStatePagerAdapter {

		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return list.get(position).getName();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Fragment getItem(int position) {
			ThreeClassificationProduct fas = ThreeClassificationProduct.newInstance(list.get(position).getId(), type, isFreeShipping);
			return fas;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	class tabModel {
		private String name;
		private String id;

		public void setName(String name) {
			this.name = name;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

}
