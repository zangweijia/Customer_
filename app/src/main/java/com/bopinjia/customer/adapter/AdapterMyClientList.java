package com.bopinjia.customer.adapter;

import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;
import org.xutils.image.ImageOptions.Builder;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.bean.MyClientBean;
import com.bopinjia.customer.util.ViewHolderUtils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

public class AdapterMyClientList extends CommonAdapter {

	List<MyClientBean> list;
	Context context;
	int layoutId;

	public AdapterMyClientList(List<MyClientBean> list, Context context, int layoutId) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {

		// 排名
		holder.setText(R.id.tv_index, list.get(position).getIndex());
		TextView txt = holder.getView(R.id.tv_index);
		txt.getPaint().setFakeBoldText(true);

		// 客户名称
		holder.setText(R.id.tv_name, list.get(position).getName());

		holder.setText(R.id.tv_phone, list.get(position).getPhone());

		holder.setText(R.id.tv_order_number, list.get(position).getOrder_number());

		holder.setText(R.id.tv_price, list.get(position).getPrice());

		holder.setText(R.id.tv_commission, list.get(position).getCommission());

		holder.setImageURl(R.id.iv_head, list.get(position).getImg());

		ImageView iv = holder.getView(R.id.iv_head);
		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setCircular(true).setCrop(true).build();
		x.image().bind(iv, list.get(position).getImg(), imageOptions);

	}

}
