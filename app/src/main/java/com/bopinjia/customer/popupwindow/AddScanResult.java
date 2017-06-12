package com.bopinjia.customer.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bopinjia.customer.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * Created by ZWJ on 2017/6/7.
 */

public class AddScanResult extends PopupWindow {
    private View mMenuView;
    public TextView addCart;
    public TextView btn_go_cart;
    public ImageView iv_close;
    LinearLayout mLL1, mLL2;
    OnGetData mOnGetData;

    public AddScanResult(Context context) {
        super(context);
    }

    public AddScanResult(Activity context, View.OnClickListener itemsOnClick, String string) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.wj_activity_scan_result, null);

        addCart = (TextView) mMenuView.findViewById(R.id.addCart);
        iv_close = (ImageView) mMenuView.findViewById(R.id.iv_close);

        addCart.setOnClickListener(itemsOnClick);
        iv_close.setOnClickListener(itemsOnClick);

        setResult(string, mMenuView);
        mLL1 = (LinearLayout) mMenuView.findViewById(R.id.ll_1);
        mLL2 = (LinearLayout) mMenuView.findViewById(R.id.ll_2);

        mLL1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLL1.setBackgroundResource(R.drawable.kuang_);
                mLL2.setBackgroundResource(R.drawable.white_kuang);
                mSkuid = productskuid1;
                mOtype = otype1;
//                if (mOnGetData != null) {
//                    mOnGetData.OnGetDataCallBack(productskuid1, otype1);
//                }
            }
        });
        mLL2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLL2.setBackgroundResource(R.drawable.kuang_);
                mLL1.setBackgroundResource(R.drawable.white_kuang);
                mSkuid = productskuid2;
                mOtype = otype2;
//                if (mOnGetData != null) {
//                    mOnGetData.OnGetDataCallBack(productskuid2, otype2);
//                }
            }
        });


        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.AnimBottom);
        this.setOutsideTouchable(true);
        ColorDrawable dw = new ColorDrawable(0xb0000000);

        this.setBackgroundDrawable(dw);
    }

    public void setData(OnGetData m) {
        mOnGetData = m;
        mOnGetData.OnGetDataCallBack(mSkuid, mOtype);
    }


    public interface OnGetData {
        abstract void OnGetDataCallBack(String skuid, String otypeid);
    }

    String productskuid1;
    String otype1;
    String mSkuid;
    String mOtype;
    String productskuid2;
    String otype2;

    private void setResult(String result, View view) {
        try {
            JSONObject jo = new JSONObject(result);
            if (jo.getString("Result").equals("1")) {
                JSONArray ja = jo.getJSONArray("Data");
                JSONObject jo1 = ja.getJSONObject(0);
                ((TextView) view.findViewById(R.id.txt_title_1))
                        .setText("              " + jo1.getString("ProductSKUName"));
                ((TextView) view.findViewById(R.id.txt_price_1)).setText("¥" + jo1.getString("ScanPrice"));
                if (jo1.getString("CustomerInitiaQuantity").equals("0")) {
                    ((TextView) view.findViewById(R.id.buycount1)).setVisibility(View.INVISIBLE);
                } else {
                    ((TextView) view.findViewById(R.id.buycount1))
                            .setText("起订量:" + jo1.getString("CustomerInitiaQuantity"));
                }
                ImageOptions imageOptions = new ImageOptions.Builder()
                        .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                        // .setFailureDrawableId(R.drawable.ic_default_image)//
                        // 加载失败后默认显示图片
                        .build();
                x.image().bind((ImageView) view.findViewById(R.id.img_1), jo1.getString("ProductThumbnail"),
                        imageOptions);
                productskuid1 = jo1.getString("ProductSKUId");
                otype1 = jo1.getString("IsDirectMail");
                if (otype1.equals("0")) {
                    ((TextView) view.findViewById(R.id.btn_isdirectmail1)).setText("现货区");
                    ((TextView) view.findViewById(R.id.btn_isdirectmail1))
                            .setBackgroundResource(R.drawable.bg_order_state2);
                } else {
                    ((TextView) view.findViewById(R.id.btn_isdirectmail1)).setText("直邮区");
                    ((TextView) view.findViewById(R.id.btn_isdirectmail1))
                            .setBackgroundResource(R.drawable.bg_order_state1);
                }
                mSkuid = productskuid1;
                mOtype = otype1;

                JSONObject jo2 = ja.getJSONObject(1);
                ((TextView) view.findViewById(R.id.txt_title_2))
                        .setText("              " + jo2.getString("ProductSKUName"));
                ((TextView) view.findViewById(R.id.txt_price_2)).setText("¥" + jo2.getString("ScanPrice"));

                if (jo2.getString("CustomerInitiaQuantity").equals("0")) {
                    ((TextView) view.findViewById(R.id.buycount2)).setVisibility(View.INVISIBLE);
                } else {
                    ((TextView) view.findViewById(R.id.buycount2))
                            .setText("起订量:" + jo2.getString("CustomerInitiaQuantity"));
                }
                ImageOptions imageOption = new ImageOptions.Builder()
                        .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                        // .setFailureDrawableId(R.drawable.ic_default_image)//
                        // 加载失败后默认显示图片
                        .build();
                x.image().bind((ImageView) view.findViewById(R.id.img_2), jo2.getString("ProductThumbnail"),
                        imageOption);
                productskuid2 = jo2.getString("ProductSKUId");
                otype2 = jo2.getString("IsDirectMail");
                if (otype2.equals("0")) {
                    ((TextView) view.findViewById(R.id.btn_isdirectmail2)).setText("现货区");
                    ((TextView) view.findViewById(R.id.btn_isdirectmail2))
                            .setBackgroundResource(R.drawable.bg_order_state2);
                } else {
                    ((TextView) view.findViewById(R.id.btn_isdirectmail2)).setText("直邮区");
                    ((TextView) view.findViewById(R.id.btn_isdirectmail2))
                            .setBackgroundResource(R.drawable.bg_order_state1);
                }

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

