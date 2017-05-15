package com.bopinjia.customer.adapter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class ProductListAdapter extends BaseAdapter {
	private Context context;
	private List<ProductListModel> list;
	private ProductListHold hold;
	private LayoutInflater inflater;

	public ProductListAdapter(Context context, List<ProductListModel> list) {
	
		this.context = context;
		this.list = list;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.fragement_item_list_xianhuo_new_product, null);
			hold = new ProductListHold();
			hold.item_name = (TextView) convertView.findViewById(R.id._tv_xianhuo_list_name);
			hold.item_market_price = (TextView) convertView.findViewById(R.id._tv_xianhuo_list_market_price);
			hold.item_market_price.getPaint().setStrikeThruText(true);
			hold.item_sale_price = (TextView) convertView.findViewById(R.id._tv_xianhuo_list_sale_price);
			hold.item_sale_number = (TextView) convertView.findViewById(R.id._tv_xianhuo_list_count);
			hold.item_picture = (ImageView) convertView.findViewById(R.id._iv_xianhuo_list_thumbnails);
			convertView.setTag(hold);
		} else {
			hold = (ProductListHold) convertView.getTag();
		}
		ProductListModel jm = list.get(position);
		
		hold.item_name.setText(jm.getName());
		
		DecimalFormat df = new DecimalFormat("#,##0.00");
		hold.item_market_price.setText("¥" + df.format(new BigDecimal(jm.getMarket_price()).doubleValue()));
		
		hold.item_sale_price.setText("¥" + df.format(new BigDecimal(jm.getSale_price()).doubleValue()));
		
		hold.item_sale_number.setText(jm.getCount()+"人已购买");
		
		
		ImageOptions imageOptions = new ImageOptions.Builder()
				.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(hold.item_picture, jm.getThumbnails(), imageOptions);
		
		return convertView;
	}

}
