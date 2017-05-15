package com.bopinjia.customer.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.view.annotation.ViewInject;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterMyClientList;
import com.bopinjia.customer.bean.MyClientBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBackID;
import com.bopinjia.customer.util.MD5;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class ActivityFXClientList extends BaseActivity {

	@ViewInject(R.id.btn_return)
	private TextView mBack;

	@ViewInject(R.id.et_search)
	private EditText mSearch;

	@ViewInject(R.id.tv_price)
	private TextView mPrice;

	@ViewInject(R.id.tv_all)
	private TextView mAll;

	@ViewInject(R.id.line_all)
	private View mLineAll;

	@ViewInject(R.id.tv_paixu)
	private TextView mPaixu;

	@ViewInject(R.id.line_paixu)
	private View mLinePX;

	@ViewInject(R.id.lv_my_client)
	private ListView mList;

	AdapterMyClientList mAdapter;

	private List<MyClientBean> list;
	/** 检索 */
	private int PageIndex = 1;
	/** 一共多少页 */
	private String mAllPages;

	private String stateid = "0";

	private List<MyClientBean> dataList;

	PopupWindow popupWindow;
	private ListView lv_group;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_act_my_client);
		x.view().inject(this);
		init();

		getMyClientList(PageIndex, 0, "0", "");

		mSearch.setOnKeyListener(new OnKeyListener() {

			@Override

			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					// 先隐藏键盘
					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
							ActivityFXClientList.this.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					// 进行搜索操作的方法，在该方法中可以加入mEditSearchUser的非空判断
					String str = mSearch.getText().toString().trim();
					if (str.equals("") || str == null) {
					} else {
						// 搜索功能
						String mEt = null;
						try {
							mEt = URLEncoder.encode(str, "utf-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						getMyClientList(PageIndex, 3, "0", mEt);
					}

				}
				return false;
			}
		});
	}

	private void init() {
		mBack.setOnClickListener(this);
		mAll.setOnClickListener(this);
		mPaixu.setOnClickListener(this);

		mPrice.setText(getIntent().getStringExtra("cumulativeMoney"));

		mList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// 当不滚动时
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// 判断是否滚动到底部
					if (view.getLastVisiblePosition() == view.getCount() - 1) {
						// 加载更多功能的代码
						if (PageIndex < Integer.parseInt(mAllPages)) {
							PageIndex += 1;
							getMyClientList(PageIndex, 1, stateid, "");
						} else if (PageIndex >= Integer.parseInt(mAllPages)) {

						}

					}
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub

			}
		});

		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent i = new Intent();
				i.putExtra("id", dataList.get(arg2).getId());
				i.putExtra("img", dataList.get(arg2).getImg());
				i.putExtra("phone", dataList.get(arg2).getPhone());
				i.putExtra("name", dataList.get(arg2).getName());
				forward(ActivityFXClientDetails.class, i);
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_return:
			finish();
			break;

		case R.id.tv_all:
			setContent(1);
			break;

		case R.id.tv_paixu:
			setContent(2);
			showPopupWindow(mPaixu);
			break;
		default:
			break;
		}
	}

	private void setContent(int i) {
		switch (i) {
		case 1:
			mAll.setTextColor(getResources().getColor(R.color.main_color));
			mLineAll.setVisibility(View.VISIBLE);
			mPaixu.setTextColor(getResources().getColor(R.color.bg_666666));
			mLinePX.setVisibility(View.GONE);
			stateid = "0";
			setTextViewDrawable(2);
			break;
		case 2:
			mAll.setTextColor(getResources().getColor(R.color.bg_666666));
			mLineAll.setVisibility(View.GONE);
			mPaixu.setTextColor(getResources().getColor(R.color.main_color));
			mLinePX.setVisibility(View.VISIBLE);
			setTextViewDrawable(1);
			break;

		default:
			break;
		}

	}

	/**
	 * 设置排序旁边的按钮
	 * 
	 * @param i
	 *            i= 1显示橘色 i=2显示黑色
	 */
	private void setTextViewDrawable(int i) {
		Drawable drawable = null;
		if (i == 1) {
			drawable = getResources().getDrawable(R.drawable.ic_triangle_orange);
		} else if (i == 2) {
			drawable = getResources().getDrawable(R.drawable.ic_trangle_black);
		}
		/// 这一步必须要做,否则不会显示.
		drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
		mPaixu.setCompoundDrawables(null, null, drawable, null);
	}

	private void showPopupWindow(View parent) {
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = layoutInflater.inflate(R.layout.group_list, null);
			lv_group = (ListView) view.findViewById(R.id.lvGroup);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.wj_item_pop_sort, R.id.tv_name,
					new String[] { "默认最新", "成交订单从高到低", "交易金额从高到低", "佣金从高到低" });

			lv_group.setAdapter(adapter);
			popupWindow = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}

		popupWindow.setOutsideTouchable(true);
		ColorDrawable dw = new ColorDrawable(0xb0000000);
		popupWindow.setBackgroundDrawable(dw);

		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

		popupWindow.showAsDropDown(parent, 0, 4);

		//
		lv_group.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
				if (popupWindow != null) {

					if (position == 0) {
						stateid = "1";
					} else if (position == 1) {
						stateid = "2";
					} else if (position == 2) {
						stateid = "3";
					} else if (position == 3) {
						stateid = "4";
					}

					popupWindow.dismiss();
					getMyClientList(PageIndex, 3, stateid, "");
				} else {

				}

			}
		});

	}

	/**
	 * 获取客户列表
	 */
	private void getMyClientList(int index, final int id, String stateid, String str) {
		String Ts = MD5.getTimeStamp();
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});

		map.put("UserId", getLoginUserId());
		map.put("StateId", stateid);
		map.put("PageIndex", String.valueOf(index));
		map.put("Where", str);
		map.put("Key", Constants.WEBAPI_KEY);
		map.put("Ts", Ts);
		StringBuffer stringBuffer = new StringBuffer();
		Set<String> keySet = map.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			stringBuffer.append(key).append("=").append(map.get(key)).append("&");
		}
		stringBuffer.deleteCharAt(stringBuffer.length() - 1);
		String Sign = MD5.Md5(stringBuffer.toString());

		String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/List_GDSUser?UserId=" + getLoginUserId() + "&StateId="
				+ stateid + "&Where=" + str + "&PageIndex=" + String.valueOf(index) + "&Sign="

				+ Sign + "&Ts=" + Ts;
		XutilsHttp.getInstance().get(url, null, new getMyClientListCallBack(), id, null,this);
	}

	class getMyClientListCallBack implements XCallBackID {

		@Override
		public void onResponse(String result, int id, String str) {

			try {
				JSONObject jo = new JSONObject(result);
				String jsonresult = jo.getString("Result");
				if (jsonresult.equals("1")) {

					parseproductList(result, id);
				} else {

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	private void parseproductList(String jsonarray, int id) {
		try {

			JSONObject jo = new JSONObject(jsonarray);

			JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");

			JSONObject Paging = jo.getJSONObject("Data").getJSONObject("Paging");

			mAllPages = Paging.getString("Pages");

			if (dataArray != null && dataArray.length() > 0) {
				list = new ArrayList<MyClientBean>();

				for (int i = 0; i < dataArray.length(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					MyClientBean m = new MyClientBean();
					m.setId(data.getString("MGDS_UserID"));
					m.setIndex(i + 1 + "");
					m.setName(data.getString("MGDS_UserName"));
					m.setPhone(data.getString("MGDS_UserRegtel"));
					m.setCommission("¥" + data.getString("MGDS_UserCommission"));
					m.setOrder_number(data.getString("MGDS_UserTotalOrder"));
					m.setImg(data.getString("MGDS_UserHearpic"));

					m.setPrice("¥" + data.getString("MGDS_UserOrderAmount"));
					list.add(m);

				}

				switch (id) {
				case 0:
					dataList = list;
					mAdapter = new AdapterMyClientList(dataList, this, R.layout.wj_item_my_client);
					mList.setAdapter(mAdapter);

					break;
				case 1:
					if (list != null && !list.isEmpty()) {
						dataList.addAll(list);
						mAdapter.notifyDataSetChanged();
					}
					break;
				case 3:
					dataList.clear();
					dataList = list;
					mAdapter = new AdapterMyClientList(dataList, this, R.layout.wj_item_my_client);
					mList.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();

					break;
				default:
					break;
				}

			}
		} catch (Exception e) {
			showSysErr(e);
		}

	}

}
