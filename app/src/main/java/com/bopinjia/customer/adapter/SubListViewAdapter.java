package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 二级目录的子目录的数据适配器
 * 
 * @author Administrator
 * 
 */
public class SubListViewAdapter extends BaseAdapter {

	private List<RootListModel> sub_items;
	private Context context;
	private int root_position;
	private LayoutInflater inflater;

	public SubListViewAdapter(Context context, List<RootListModel> sub_items, int position) {
		this.context = context;
		inflater = LayoutInflater.from(context);
		this.sub_items = sub_items;
		this.root_position = position;
	}

	@Override
	public int getCount() {
		RootListModel  s= (sub_items.get(root_position));
		return s.listModel.size();
	}

	@Override
	public Object getItem(int position) {
		RootListModel  s= (sub_items.get(root_position));
		return s.listModel.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {

			holder = new ViewHolder();
			convertView = (View) inflater.inflate(R.layout.root_listview_item, parent, false);
			holder.item_text = (TextView) convertView.findViewById(R.id.item_name_text);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		RootListModel  s= (sub_items.get(root_position));
		
		holder.item_text.setText(s.listModel.get(position).getCname());
		return convertView;
	}

	class ViewHolder {
		TextView item_text;
	}

}