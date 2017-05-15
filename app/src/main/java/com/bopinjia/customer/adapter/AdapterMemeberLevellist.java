package com.bopinjia.customer.adapter;

import java.util.HashMap;
import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.net.XutilsHttp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class AdapterMemeberLevellist extends BaseAdapter {

	private Context context;
	private List<AdapterMemeberLevelModel> userList;
	HashMap<String, Boolean> states = new HashMap<String, Boolean>();// 用于记录每个RadioButton的状态，并保证只可选一个
	private LayoutInflater inflater;
	private boolean isFirst = true;

	public AdapterMemeberLevellist(Context context, List<AdapterMemeberLevelModel> userList) {
		this.context = context;
		this.userList = userList;
		inflater = LayoutInflater.from(context);

	}

	@Override
	public int getCount() {

		return userList.size();
	}

	@Override
	public Object getItem(int arg0) {

		return userList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {

		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.wj_item_memeber_level, null);
			holder = new ViewHolder();
			holder.iv = (ImageView) convertView.findViewById(R.id.iv_img);
			holder.name = (TextView) convertView.findViewById(R.id.tv_level_name);
			holder.price = (TextView) convertView.findViewById(R.id.tv_price);
			holder.rdBtn = (RadioButton) convertView.findViewById(R.id.rb);
			holder.time = (TextView) convertView.findViewById(R.id.tv_time);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final RadioButton radio = (RadioButton) convertView.findViewById(R.id.rb);
		holder.rdBtn = radio;
		AdapterMemeberLevelModel am = userList.get(position);

		holder.name.setText(am.getName());
		holder.price.setText("¥" + am.getPrice());
		holder.time.setText("会员有效期：" + am.getTime());

		XutilsHttp.getInstance().bindCommonImage(holder.iv, am.getImg(), true, R.drawable.ic_default);
		int self = Integer.parseInt(am.getSelflevel());
		int level = Integer.parseInt(am.getLevel());

		if (self == level) {
			if (isFirst) {
				holder.rdBtn.setChecked(true);
				states.put(String.valueOf(position), radio.isChecked());
				AdapterMemeberLevellist.this.notifyDataSetChanged();
			}
		}

		holder.rdBtn.setEnabled(true);
		// 当RadioButton被选中时，将其状态记录进States中，并更新其他RadioButton的状态使它们不被选中
		holder.rdBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				isFirst = false;
				// 重置，确保最多只有一项被选中
				for (String key : states.keySet()) {
					states.put(key, false);
				}
				states.put(String.valueOf(position), radio.isChecked());
				AdapterMemeberLevellist.this.notifyDataSetChanged();
			}
		});

		boolean res = false;
		if (states.get(String.valueOf(position)) == null || states.get(String.valueOf(position)) == false) {
			res = false;
			states.put(String.valueOf(position), false);
		} else
			res = true;

		holder.rdBtn.setChecked(res);

		return convertView;
	}

	static class ViewHolder {

		RadioButton rdBtn;
		ImageView iv;
		TextView name;
		TextView price;
		TextView time;
	}

	public interface ClickRadioButton {
		void show();
	}

}
