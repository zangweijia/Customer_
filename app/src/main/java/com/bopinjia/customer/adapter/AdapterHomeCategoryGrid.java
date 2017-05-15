package com.bopinjia.customer.adapter;

import java.net.URL;
import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.CommissionProductBean;
import com.bopinjia.customer.bean.HomeCategoryBean;
import com.bopinjia.customer.util.ViewHolderUtils;
import com.bopinjia.customer.view.RoundImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.view.View;
import android.widget.ImageView;

public class AdapterHomeCategoryGrid extends CommonAdapter {

	private List<HomeCategoryBean> list;
	private Context context;
	private int layoutId;
	private boolean iscircle;

	public AdapterHomeCategoryGrid(List<HomeCategoryBean> list, Context context, int layoutId, boolean iscircle) {
		super(list, context, layoutId);
		this.list = list;
		this.context = context;
		this.layoutId = layoutId;
		this.iscircle = iscircle;
	}

	@Override
	public void convert(ViewHolderUtils holder, Object t, int position) {

		holder.setText(R.id.tv_name, list.get(position).getName());

		if (!iscircle) {
			holder.setImageURl(R.id.ic_category, list.get(position).getImg());
		} else {
			holder.setCircleImageURl(R.id.ic_category, list.get(position).getImg());
		}

	}

}
