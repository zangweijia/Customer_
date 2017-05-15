package com.bopinjia.customer.mainpage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityCategory;
import com.bopinjia.customer.activity.ActivityProductListNew;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.brand.PinyinComparator;
import com.bopinjia.customer.brand.SortAdapter;
import com.bopinjia.customer.brand.SortModel;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.CharacterParser;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.SideBar;
import com.bopinjia.customer.view.SideBar.OnTouchingLetterChangedListener;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

@ContentView(R.layout.category_fragmentbrand)
public class CategoryFragmentBrand extends Fragment {

	private String IsFreeShipping;

	@ViewInject(R.id._lvbrand)
	private ListView sortListView;
	@ViewInject(R.id.dialog)
	private TextView dialog;

	@ViewInject(R.id.sidebar)
	private SideBar sidebar;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;
	private SortAdapter adapter;
	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return x.view().inject(this, inflater, container);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		int type = ActivityCategory.type;
		if (type == 0) {
			IsFreeShipping = "0";
		} else if (type == 1) {
			IsFreeShipping = "1";
		}
		getBrond();
		initView();

	}

	public void initView() {

		sidebar.setTextView(dialog);

		// 设置右侧触摸监听
		sidebar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				SortModel m = (SortModel) adapter.getItem(position);
				Intent intent = new Intent();
				intent.setClass(getActivity(), ActivityProductListNew.class);
				intent.putExtra("BrandId", m.getId());
				intent.putExtra("IsFreeShipping", IsFreeShipping);
				startActivity(intent);
			}
		});

	}

	private void getBrond() {
		
		String s = ((BaseActivity) getActivity()).getBindingShop();

		String url = Constants.WEBAPI_ADDRESS + "api/Brand/ListUser?UserId="+s+"&ZY="+IsFreeShipping;
		RequestParams params = new RequestParams(url);
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override

			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						parseBand(result);
					}
				} catch (JSONException e) {
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

	private void parseBand(String result) {
		// TODO Auto-generated method stub

		try {
			JSONObject jo = new JSONObject(result);
			JSONArray dataArray = jo.getJSONArray("Data");

			if (dataArray != null && dataArray.length() > 0) {
				// String[] datas = new String[dataArray.length()];
				List<SortModel> datas = new ArrayList<SortModel>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					SortModel m = new SortModel();
					m.setId(data.getString("BrandId"));
					m.setName(data.getString("BrandName"));
					m.setImgaddress(data.getString("Logo"));
					datas.add(m);
				}
				SourceDateList = filledData(datas);
				Collections.sort(SourceDateList, pinyinComparator);
				adapter = new SortAdapter(getActivity(), SourceDateList);
				sortListView.setAdapter(adapter);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private List<SortModel> filledData(List<SortModel> date) {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		List<SortModel> mSortList = new ArrayList<SortModel>();
		for (int i = 0; i < date.size(); i++) {
			SortModel sortModel = date.get(i);
			// sortModel.setName(date.get(i).getName());
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(sortModel.getName().toString().trim());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

}
