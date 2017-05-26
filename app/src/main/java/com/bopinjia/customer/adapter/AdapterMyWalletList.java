package com.bopinjia.customer.adapter;

import android.content.Context;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.MyWalletBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import java.util.List;

public class AdapterMyWalletList extends CommonAdapter {

	List<MyWalletBean> list;
	Context context;
	int layoutId;

	public AdapterMyWalletList(List<MyWalletBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {



		holder.setText(R.id.tv_time, list.get(position).getDatetime());
		holder.setText(R.id.tv_type_name, list.get(position).getType());

		String typeid = list.get(position).getTypeId();

		if (typeid.equals("2")) {
			// 支出
			holder.setText(R.id.tv_price, "-"+list.get(position).getPrice());
		} else if (typeid.equals("1")) {
			// 收入
			holder.setText(R.id.tv_price, "+"+list.get(position).getPrice());
			((TextView)holder.getView(R.id.tv_price)).setTextColor(context.getResources().getColor(R.color.main_color));
		}

	}

}
