package com.bopinjia.customer.adapter.recycleviewAdapter;

import android.widget.ImageView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.RecycleBean.Product;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.List;

/**
 * Created by ZWJ on 2017/5/16.
 */

public class productListAdapter extends BaseQuickAdapter<Product> {


    public productListAdapter(int layoutResId, List<Product> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Product product) {
        baseViewHolder.setText(R.id.tv_country, product.getCountryName())
                .setText(R.id.txt_product_name, product.getProductSKUName());

        ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                .setCircular(true).setCrop(true).build();
        x.image().bind((ImageView) baseViewHolder.getView(R.id.iv_product_thumbnails), product.getProductThumbnail());
    }


}
