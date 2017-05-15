package com.bopinjia.customer.adapter;

import java.util.List;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.MyClientBean;
import com.bopinjia.customer.bean.MyWalletBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;

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

		holder.setText(R.id.tv_date_year, list.get(position).getDatayear());

		holder.setText(R.id.tv_date_time, list.get(position).getDatetime());

		holder.setText(R.id.tv_account, list.get(position).getAccount());

		

		holder.setText(R.id.tv_type, list.get(position).getType());

		String typeid = list.get(position).getTypeId();

		if (typeid.equals("0")) {
			// 支出

			holder.setText(R.id.tv_price, "-"+list.get(position).getPrice());
		} else if (typeid.equals("1")) {
			// 收入
			holder.setText(R.id.tv_price, "+"+list.get(position).getPrice());
		}

	}

}
