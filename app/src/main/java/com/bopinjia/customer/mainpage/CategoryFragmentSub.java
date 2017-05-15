package com.bopinjia.customer.mainpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityCategory;
import com.bopinjia.customer.activity.ActivityProductListNew;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.stickygridheaders.StickyGridHeadersGridView;
import com.bopinjia.customer.stickygridheaders.StickyGridHeadersSimpleAdapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;

@ContentView(R.layout.category_fragmentsub)
public class CategoryFragmentSub extends Fragment implements OnClickListener {

	@ViewInject(R.id.lst_main_category)
	private ListView mlstCategoryMain;
	/**
	 * 二三级商品列表
	 */
	@ViewInject(R.id.lst_sub_category)
	private StickyGridHeadersGridView mlstCategorySub;

	private List<JSONObject> mCategoryMainList = null;
	private CategoryMainListAdapter mCategoryMainAdapter = null;

	private List<JSONObject> mCategorySubList = null;
	private CategorySubListAdapter mCategorySubAdapter = null;

	private Map<String, List<JSONObject>> mCateMap;

	 

	private String IsFreeShipping;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return x.view().inject(this, inflater, container);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mlstCategoryMain.setOnItemClickListener(onItemClick);
		int type = ActivityCategory.type;

		if (type == 0) {
			IsFreeShipping = "0";
		} else if (type == 1) {
			IsFreeShipping = "1";
		}

		search();

	}

	AdapterView.OnItemClickListener onItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				for (int i = 0; i < mCategoryMainList.size(); i++) {
					mCategoryMainList.get(i).put("selected", i == position);
				}
				mCategoryMainAdapter.notifyDataSetChanged();

				String Code = mCategoryMainList.get(position).getString("CCode");
				getItemOnclick(Code, position);

			} catch (Exception e) {
			}
		}
	};

	private void getItemOnclick(final String Code, int position) {

		String url = Constants.WEBAPI_ADDRESS + "api/Product/SubCategory?UserId=" + ((BaseActivity)getActivity()).getBindingShop()+"&ZY="+IsFreeShipping+"&Code="+Code;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {

					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						onSubCallBack(result, Code);
					} else {

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

	/**
	 * 解析二三级返回数据
	 */
	private void onSubCallBack(String result, String code) {
		try {
			JSONObject jo = new JSONObject(result);
			JSONArray dataArray = jo.getJSONArray("Data");

			if (dataArray != null && dataArray.length() > 0) {
				List<JSONObject> dataList = new ArrayList<JSONObject>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					dataList.add(data);
				}

				convertSubData(dataList, code);
				mCategorySubAdapter = new CategorySubListAdapter(getActivity());
				mlstCategorySub.setAdapter(mCategorySubAdapter);
				mCategorySubAdapter.notifyDataSetChanged();

			} else {
				mCategoryMainList = new ArrayList<JSONObject>();
				mCategoryMainAdapter.notifyDataSetChanged();
				mCategorySubList = new ArrayList<JSONObject>();
				mCategorySubAdapter.notifyDataSetChanged();
				mCateMap = new HashMap<String, List<JSONObject>>();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 解析一级返回数据
	 */
	public void onCallBack(String result) {

		try {
			JSONObject jo = new JSONObject(result);
			JSONArray dataArray = jo.getJSONArray("Data");

			if (dataArray != null && dataArray.length() > 0) {
				List<JSONObject> dataList = new ArrayList<JSONObject>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					dataList.add(data);
				}

				convertData(dataList);
				// 画面初期化时 的检索
				mCategoryMainAdapter = new CategoryMainListAdapter(getActivity());
				mlstCategoryMain.setAdapter(mCategoryMainAdapter);
				mCategorySubAdapter = new CategorySubListAdapter(getActivity());
				mlstCategorySub.setAdapter(mCategorySubAdapter);

				// 检索时
				mCategoryMainAdapter.notifyDataSetChanged();
				mCategorySubAdapter.notifyDataSetChanged();

			} else {
				mCategoryMainList = new ArrayList<JSONObject>();
				mCategoryMainAdapter.notifyDataSetChanged();
				mCategorySubList = new ArrayList<JSONObject>();
				mCategorySubAdapter.notifyDataSetChanged();
				mCateMap = new HashMap<String, List<JSONObject>>();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 检索处理
	 */
	private void search() {

		String url = Constants.WEBAPI_ADDRESS + "api/Product/TopCategory?UserId="
				+ ((BaseActivity) getActivity()).getBindingShop() + "&ZY=" + IsFreeShipping;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						onCallBack(result);
					} else {
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {

			}

			@Override
			public void onCancelled(Callback.CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});

	}

	/**
	 * 数据结构转换
	 * 
	 * @param dataList
	 *            服务器数据结构
	 */
	private void convertData(List<JSONObject> dataList) {
		try {

			SortShowOrder sorter = new SortShowOrder();

			// 一级分类
			List<JSONObject> level1List = new ArrayList<JSONObject>();

			for (JSONObject data : dataList) {
				if ("0".equals(data.getString("PCode"))) {
					level1List.add(data);
				}
			}
			Collections.sort(level1List, sorter);
			this.mCategoryMainList = level1List;

			for (int i = 0; i < mCategoryMainList.size(); i++) {
				mCategoryMainList.get(i).put("selected", i == 0);
			}
			String Code = (String) mCategoryMainList.get(0).get("CCode");
			getItemOnclick(Code, 0);

		} catch (Exception e) {
		}

	}

	/**
	 * 二三级数据结构转换
	 * 
	 * @param dataList
	 */

	private void convertSubData(List<JSONObject> dataList, String code) {

		try {
			SortShowOrder sorter = new SortShowOrder();
			// 二级分类和三级分类
			List<JSONObject> level2List = new ArrayList<JSONObject>();
			List<JSONObject> level3List = new ArrayList<JSONObject>();
			// 二级分类
			for (JSONObject level2Data : dataList) {
				if (code.equals(level2Data.getString("PCode"))) {
					level2List.add(level2Data);
				}
			}
			if (!level2List.isEmpty()) {
				Collections.sort(level2List, sorter);
			}
			int headerId = 0;

			// 三级分类
			for (JSONObject level2Data : level2List) {
				for (JSONObject level3Data : dataList) {
					if (level2Data.getString("CCode").equals(level3Data.getString("PCode"))) {
						level3Data.put("headerId", headerId);
						level3Data.put("ParentCategoryName", level2Data.getString("CName"));
						level3List.add(level3Data);
					}
				}
				headerId++;
			}

			// 三级分类排序
			// Collections.sort(level3List, sorter);

			// Date startDate = new Date(System.currentTimeMillis());
			// Date endDate = new Date(System.currentTimeMillis());
			// long diff = endDate.getTime() - startDate.getTime();
			// Log.i("parse23", Long.toString(diff));

			mCategorySubList = level3List;
		} catch (Exception e) {
		}

	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		// case R.id.btn_mine:
		//
		// break;
		default:
			break;
		}
	}

	/**
	 * 一级分类列表适配器
	 * 
	 * @author yushen 2015/12/24
	 */
	class CategoryMainListAdapter extends BaseAdapter {

		private final Context mContext;

		public CategoryMainListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return mCategoryMainList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CategoryMainListItem itemView = new CategoryMainListItem(mContext);

			try {
				// 数据模型
				final JSONObject model = mCategoryMainList.get(position);

				itemView.setMainCate(model.getString("CName"));
				itemView.setSelectStatus(model.getBoolean("selected"));

			} catch (JSONException e) {
			}

			return itemView;
		}

	}

	class CategoryMainListItem extends LinearLayout {
		public CategoryMainListItem(Context context) {
			super(context);
			View.inflate(getContext(), R.layout.item_main_category, this);
		}

		// 一级分类名
		public void setMainCate(String mainCate) {
			((TextView) findViewById(R.id.txt_main_cate)).setText(mainCate);
		}

		// 选中状态
		public void setSelectStatus(boolean isSelected) {
			this.setBackgroundColor(getResources().getColor(isSelected ? R.color.bg_white : R.color.bg_transparent));
			this.findViewById(R.id.v_selected).setVisibility(isSelected ? VISIBLE : INVISIBLE);
		}
	}

	/**
	 * 二级三级分类列表适配器
	 * 
	 * @author yushen 2015/12/24
	 */
	class CategorySubListAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter {

		private final Context mContext;

		public CategorySubListAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			if (mCategorySubList != null) {
				return mCategorySubList.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			try {

				// 数据模型
				final JSONObject model = mCategorySubList.get(position);

				CategorySubSubListItem itemView = new CategorySubSubListItem(mContext, model.getString("CId"), model.getString("CCode"));
				itemView.setCategoryName(model.getString("CName"));
				itemView.setCategoryThumbnail(model.getString("Thumbnail"));

				android.widget.AbsListView.LayoutParams layoutParams = new android.widget.AbsListView.LayoutParams(
						LayoutParams.MATCH_PARENT, parent.getMeasuredHeight() / 4);
				itemView.setLayoutParams(layoutParams);

				return itemView;
			} catch (JSONException e) {
			}

			return convertView;
		}

		@Override
		public long getHeaderId(int position) {
			int id = -1;
			try {
				id = mCategorySubList.get(position).getInt("headerId");
			} catch (Exception e) {
			}

			return id;
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {

			try {
				TextView txtHeader = new TextView(mContext);
				LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				txtHeader.setLayoutParams(params);
				txtHeader.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
				int padding = dipToPixels(5);
				int paddingleft = dipToPixels(15);
				txtHeader.setPadding(paddingleft, padding, padding, padding);
				txtHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				txtHeader.setTextColor(getResources().getColor(R.color.txt_black));
				txtHeader.setText(mCategorySubList.get(position).getString("ParentCategoryName"));

				return txtHeader;
			} catch (Exception e) {
			}

			return convertView;
		}

	}

	public int dipToPixels(int dip) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int) px;
	}

	class CategorySubListItem extends LinearLayout {
		private Context mContext;

		public CategorySubListItem(Context context) {
			super(context);
			mContext = context;
			View.inflate(getContext(), R.layout.item_sub_category, this);
		}

		// 二级分类名
		public void setCategoryName(String name) {
			((TextView) findViewById(R.id.txt_level2_title)).setText(name);
		}

		// 三级分类
		public void setSubSubCate(List<JSONObject> level3CateList) {
			try {
				GridLayout glLevel3Category = ((GridLayout) findViewById(R.id.gr_level3_cate));
				for (JSONObject data : level3CateList) {
					// 商品分类（三级）
					// String cateId = data.getString("ProductCategoryId");

					CategorySubSubListItem itemView = new CategorySubSubListItem(mContext, data.getString("CId") ,data.getString("CCode"));
					itemView.setCategoryName(data.getString("CName"));
					itemView.setCategoryThumbnail(data.getString("Thumbnail"));

					glLevel3Category.addView(itemView);
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * 三级分类列表适配器
	 * 
	 * @author yushen 2015/12/24
	 */
	class CategorySubSubListAdapter extends BaseAdapter {

		private final Context mContext;
		private List<JSONObject> mDataList;

		public CategorySubSubListAdapter(Context context, List<JSONObject> dataList) {
			mContext = context;
			mDataList = dataList;
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			try {

				// 数据模型
				final JSONObject model = mDataList.get(position);

				// 商品分类（三级）
				// String cateId = model.getString("ProductCategoryId3");

				CategorySubSubListItem itemView = new CategorySubSubListItem(mContext, model.getString("CId"),model.getString("CCode"));
				itemView.setCategoryName(model.getString("CName"));
				itemView.setCategoryThumbnail(model.getString("Thumbnail"));

				return itemView;
			} catch (JSONException e) {
			}

			return convertView;
		}

	}

	class CategorySubSubListItem extends LinearLayout {
		public CategorySubSubListItem(Context context, final String cateId ,final String ccode) {
			super(context);
			View.inflate(getContext(), R.layout.item_sub_sub_category, this);
			this.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.setClass(getActivity(), ActivityProductListNew.class);
					intent.putExtra("ProductCategoryId", cateId);
					intent.putExtra("ccode", ccode);
					
					intent.putExtra("IsFreeShipping", IsFreeShipping);
					startActivity(intent);

				}
			});
		}

		// 三级分类名
		public void setCategoryName(String subSubCate) {
			((TextView) findViewById(R.id.txt_level3_cate)).setText(subSubCate);
		}

		// 三级分类缩略图
		public void setCategoryThumbnail(String thumbnail) {

			// setImageFromUrl(thumbnail, (ImageView)
			// findViewById(R.id.iv_level3_cate));
			ImageOptions imageOptions = new ImageOptions.Builder().setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
					.build();
			x.image().bind((ImageView) findViewById(R.id.iv_level3_cate), thumbnail, imageOptions);
		}
	}

	/**
	 * 显示顺序
	 */
	class SortShowOrder implements Comparator<JSONObject> {

		@Override
		public int compare(JSONObject lhs, JSONObject rhs) {
			try {
				int startShowOrder = lhs.getInt("ShowOrder");
				int endShowOrder = rhs.getInt("ShowOrder");

				return startShowOrder - endShowOrder;
			} catch (Exception e) {
			}

			return 0;
		}

	}

}
