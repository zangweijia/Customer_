package com.bopinjia.customer.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.popupwindow.AddCartSuccess;
import com.bopinjia.customer.popupwindow.AddScanResult;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.util.SecurityUtil;
import com.bopinjia.customer.util.SetPriceSize;
import com.bopinjia.customer.util.StringUtils;
import com.bopinjia.customer.view.NoScrollGridView;
import com.bopinjia.customer.view.NoScrollListview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityCart extends BaseActivity {

    private TextView mEdit, mAllAmount;
    private TextView mBack;
    private TextView mNext, mDelete;
    private NoScrollGridView mGridNew;
    private CheckBox mAllCheck;
    private boolean editMode = false;
    private boolean mIsCheckAll = false;
    private List<JSONObject> mCartList;
    private List<JSONObject> dataList;
    private CartListAdapter cartListAdapter;
    private JSONArray mShopList;
    private List<ProductGridviewClassSubBean> list;
    private String userid;
    // 给支付宝传参1
    private String ProductName;
    private String uuid;
    // 自定义的dialog
    private Dialog mDialog;
    // 自定义dialog绑定的布局
    private View dialogView;

    private String addressId;
    private AddCartSuccess AddCartPopWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_activity_cart);
        init();
        getCartContent();
        GetDefultAddress();
        if (getIntent().hasExtra("isScan")) {
            String scanCode = getIntent().getStringExtra("ScanCode");
            search(getBindingShop(), scanCode);
        } else {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        GetDefultAddress();
        init();
        getCartContent();
    }

    private void getCartContent() {
        String Ts = MD5.getTimeStamp();
        String str = "Key=" + Constants.WEBAPI_KEY + "&PageIndex=1&Ts=" + Ts + "&UserId=" + userid + "&UUID=" + uuid;
        String Sign = MD5.Md5(str);
        String url = Constants.WEBAPI_ADDRESS + "api/CSC/BpwList?UserId=" + userid + "&UUID=" + uuid + "&PageIndex="
                + "1" + "&Sign=" + Sign + "&Ts=" + Ts;
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        parse(jo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    private void getTuiJian() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId",
                getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP));
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);
        map.put("PageIndex", "1");
        map.put("pageSize", "20");

        String url = Constants.WEBAPI_ADDRESS + "api/CSC/BpwCarRecommend_New?UserId="
                + getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP)
                + "&Key=" + Constants.WEBAPI_KEY + "&Ts=" + Ts + "&PageIndex=1&pageSize=20" + "&Sign="
                + NetUtils.getSign(map);
        RequestParams params = new RequestParams(url);

        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        ParseTuiJian(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    private void ParseTuiJian(String result) {

        try {
            JSONObject jo = new JSONObject(result);
            JSONArray jDList = jo.getJSONObject("Data").getJSONArray("Records");

            if (jDList.length() > 0) {

                if (jDList != null) {
                    list = new ArrayList<ProductGridviewClassSubBean>();
                    for (int i = 0; i < jDList.length(); i++) {
                        JSONObject data = jDList.getJSONObject(i);
                        ProductGridviewClassSubBean m = new ProductGridviewClassSubBean();

                        m.setIsfexiao(data.getString("BCP_IsFX"));
                        m.setCommissionPrice(data.getString("CommissionPrice"));


                        m.setImg(data.getString("ProductThumbnail"));
                        m.setMarketprice(data.getString("MarketPrice"));
                        m.setIsshiping("1");
                        m.setNumber(data.getString("CustomerInitiaQuantity"));
                        m.setName(data.getString("ProductSKUName"));
                        m.setPrice(data.getString("ScanPrice"));
                        m.setId(data.getString("ProductSKUId"));
                        m.setCountry(data.getString("CountryName"));
                        m.setCountryimg(data.getString("CountryImageUrl"));
                        m.setRealStock(data.getString("RealStock"));
                        list.add(m);
                    }

                    AdapterProductGridViewClassSub mCartTJ = new AdapterProductGridViewClassSub(list, this, R.layout.wj_item_class_sub);
                    mGridNew.setAdapter(mCartTJ);
                    mGridNew.setOnItemClickListener(new OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent ii = new Intent();
                            ii.putExtra("IsFreeShipping", list.get(position).getIsshiping());
                            ii.putExtra("ProductSKUId", list.get(position).getId());
                            ii.setClass(ActivityCart.this, ActivityProductDetailsNew.class);
                            startActivity(ii);

                        }
                    });
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void parse(JSONObject jo) {

        try {
            DecimalFormat df = new DecimalFormat("##0.00");
            JSONObject jD = jo.getJSONObject("Data");
            JSONObject Total = jD.getJSONObject("Total");
            String mQuantity = Total.getString("TotalQuantity");
            String mTotalAmount = Total.getString("TotalAmount");
            String mSelectAll = Total.getString("Payselect");
            if (mSelectAll.equals("1")) {
                mAllCheck.setChecked(true);
                mIsCheckAll = true;
            } else if (mSelectAll.equals("0")) {
                mAllCheck.setChecked(false);
                mIsCheckAll = false;
            }

            String check = "去结算 " + "( " + mQuantity + " )";
            int endnumber = check.length();
            SpannableString styledText = new SpannableString(check);
            styledText.setSpan(new TextAppearanceSpan(ActivityCart.this, R.style.style0), 0, 4,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledText.setSpan(new TextAppearanceSpan(ActivityCart.this, R.style.style1), 4, endnumber,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) findViewById(R.id.tv_next)).setText(styledText, TextView.BufferType.SPANNABLE);

            mShopList = jD.getJSONArray("ShopList");
            double dbSellPrice = new BigDecimal(mTotalAmount).doubleValue();
            mAllAmount.setText(df.format(dbSellPrice));
            if (mShopList.length() > 0) {
                findViewById(R.id.ll_main).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_no_product).setVisibility(View.GONE);
                findViewById(R.id.tv_edit).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_checkout).setVisibility(View.VISIBLE);
                if (mShopList != null) {
                    dataList = new ArrayList<JSONObject>();

                    for (int i = 0; i < mShopList.length(); i++) {
                        JSONObject data = mShopList.getJSONObject(i);
                        dataList.add(data);
                    }
                    this.mCartList = dataList;
                    NoScrollListview ll = (NoScrollListview) findViewById(R.id.ll_main);
                    cartListAdapter = new CartListAdapter(this);
                    ll.setAdapter(cartListAdapter);
                }


            } else {
                findViewById(R.id.tv_edit).setVisibility(View.GONE);
                findViewById(R.id.ll_main).setVisibility(View.GONE);
                findViewById(R.id.ll_no_product).setVisibility(View.VISIBLE);
                findViewById(R.id.ll_checkout).setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {

        findViewById(R.id._scrollview).scrollTo(0, 0);
        uuid = getIMEI1();
        boolean isLogged = isLogged();
        if (isLogged) {
            userid = getLoginUserId();
        } else {
            userid = "0";
        }
        findViewById(R.id.btn_return).setOnClickListener(this);
        // 编辑
        findViewById(R.id.go_xh).setOnClickListener(this);
        mEdit = (TextView) findViewById(R.id.tv_edit);
        mEdit.setOnClickListener(this);
        // 下一步
        mNext = (TextView) findViewById(R.id.tv_next);
        mNext.setOnClickListener(this);
        // 全选
        mAllCheck = (CheckBox) findViewById(R.id.checkbox_all);
        mAllCheck.setOnClickListener(this);
        // 删除
        mDelete = (TextView) findViewById(R.id.tv_delete);
        mDelete.setOnClickListener(this);
        // 总
        mAllAmount = (TextView) findViewById(R.id.tv_all_amount);

        mGridNew = (NoScrollGridView) findViewById(R.id.grid_tuijian);
        getTuiJian();
    }

    /**
     * 画面点击
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_xh:
                forward(ActivityHome.class);
                finish();
                break;
            case R.id.btn_return:
                // 默认隐藏 ， 当从商品详情页面跳转过来时显示
                backward();
                break;
            case R.id.tv_edit:
                editMode = !editMode;
                findViewById(R.id.ll_amount).setVisibility(editMode ? View.GONE : View.VISIBLE);
                mEdit.setText(editMode ? R.string.cmm_btn_wancheng : R.string.cmm_btn_edit);
                mNext.setVisibility(editMode ? View.GONE : View.VISIBLE);
                mDelete.setVisibility(editMode ? View.VISIBLE : View.GONE);

                if (editMode) {
                    changeState("0", "0", "3");
                } else {
                    changeState("1", "0", "3");
                }

                break;
            case R.id.tv_delete:
                // 删除
                double amount = Double.parseDouble(((mAllAmount.getText().toString())));

                if (amount > 0) {
                    mDialog = new Dialog(this, R.style.CustomDialogTheme);
                    dialogView = LayoutInflater.from(this).inflate(R.layout.send_tel_dailog, null);
                    // 设置自定义的dialog布局
                    mDialog.setContentView(dialogView);
                    // false表示点击对话框以外的区域对话框不消失，true则相反
                    mDialog.setCanceledOnTouchOutside(false);
                    // -----------------------------------

                    mDialog.show();
                    // 获取自定义dialog布局控件
                    ((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("确定要从购物车删除?");
                    Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
                    Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
                    // 确定按钮点击事件
                    confirmBt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            editMode = !editMode;
                            deleteProduct();
                            findViewById(R.id.ll_amount).setVisibility(editMode ? View.GONE : View.VISIBLE);
                            mEdit.setText(editMode ? R.string.cmm_btn_wancheng : R.string.cmm_btn_edit);
                            mNext.setVisibility(editMode ? View.GONE : View.VISIBLE);
                            mDelete.setVisibility(editMode ? View.VISIBLE : View.GONE);
                            mDialog.dismiss();
                        }
                    });
                    // 取消按钮点击事件
                    cancelBt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            mDialog.dismiss();
                        }
                    });
                } else {
                    showToast("请先选择想要删除的商品~");
                }

                break;
            case R.id.tv_next:
                double amounts = Double.parseDouble(((mAllAmount.getText().toString())));
                if (amounts > 0) {
                    if (isLogged()) {
                        CheckOut();
                    } else {
                        showToast("请先登录~");
                        forward(ActivityLogin.class);
                    }
                } else {
                    showToast("请先选择想要支付的商品~");
                }
                break;
            case R.id.checkbox_all:
                mIsCheckAll = !mIsCheckAll;
                if (mIsCheckAll) {
                    changeState("1", "0", "3");
                } else {
                    changeState("0", "0", "3");
                }
                break;
            default:
                break;
        }

    }

    /**
     * 购物车列表适配器
     *
     * @author yushen 2015/12/29
     */
    class CartListAdapter extends BaseAdapter {

        private Context mContext;

        public CartListAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mCartList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CartListItem viewItem = new CartListItem(mContext);
            try {
                // 数据模型
                JSONObject model = mCartList.get(position);
                // 自营现货区
                viewItem.setWareHouse(model.getString("TypeName"));
                // 设置分区是否选中
                viewItem.setChecked(model.getString("allstate"));
                // 设置仓库列表
                JSONArray mHuouseList = model.getJSONArray("Warehouse");
                viewItem.setHouseList(mHuouseList);

                viewItem.setPosition(position);

            } catch (Exception e) {
            }
            return viewItem;
        }
    }

    /**
     * 购物车列表控件
     */
    class CartListItem extends LinearLayout {

        private String mMainId;
        private List<LinearLayout> mViewList, mProductList;
        private DecimalFormat df;
        private CheckBox mRegionCKB;
        private int position;
        private boolean productAllChecked;

        public CartListItem(Context context) {
            super(context);
            View.inflate(getContext(), R.layout.wj_item_cart, this);
            mViewList = new ArrayList<LinearLayout>();
            mProductList = new ArrayList<LinearLayout>();
            mRegionCKB = (CheckBox) this.findViewById(R.id.checkbox_cartmain);

            mRegionCKB.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        changeState("1", "0", mMainId);
                        productAllChecked = true;
                        try {
                            mCartList.get(position).put("allstate", "1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        changeState("0", "0", mMainId);
                        productAllChecked = false;
                        try {
                            mCartList.get(position).put("allstate", "0");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public void setPosition(int position) {
            this.position = position;
        }

        // 设置区域是否选中
        public void setChecked(String state) {
            if (state.equals("1")) {
                this.productAllChecked = true;
                ((CheckBox) findViewById(R.id.checkbox_cartmain)).setChecked(true);
            } else if (state.equals("0")) {
                this.productAllChecked = false;
                ((CheckBox) findViewById(R.id.checkbox_cartmain)).setChecked(false);
            }
        }

        // 设置分区
        public void setWareHouse(String wareHouse) {
            if (wareHouse.equals("直邮区")) {
                mMainId = "1";
            } else if (wareHouse.equals("现货区")) {
                mMainId = "0";
            } else if (wareHouse.equals("自营区")) {
                mMainId = "2";
            }
            ((TextView) findViewById(R.id.cartmain_name)).setText(wareHouse);
        }

        // 设置仓库列表
        public void setHouseList(JSONArray warhousers) {
            try {
                df = new DecimalFormat("#,##0.00");
                LinearLayout lstHouses = (LinearLayout) findViewById(R.id.ll_cartmain);
                for (int i = 0; i < warhousers.length(); i++) {
                    final JSONObject house = warhousers.getJSONObject(i);
                    final LinearLayout HouseView = new LinearLayout(ActivityCart.this);
                    View.inflate(ActivityCart.this, R.layout.wj_item_cart_sub, HouseView);

                    if (mMainId.equals("1")) {
                        // 仓库名称
                        ((TextView) HouseView.findViewById(R.id.tv_housename))
                                .setText(house.getString("WarehouseName"));
                    } else {
                        ((TextView) HouseView.findViewById(R.id.tv_housename)).setVisibility(View.GONE);
                    }
                    // 销售价
                    double dbSellPrice = new BigDecimal(house.getString("WarehousePrice")).doubleValue();
                    ((TextView) HouseView.findViewById(R.id.txt_sell_price)).setText("¥" + df.format(dbSellPrice));

                    // --------------------

                    JSONArray products = house.getJSONArray("productdata");

                    // 设置商品列表
                    LinearLayout lstProducts = (LinearLayout) HouseView.findViewById(R.id.ll_cartmain_sub);
                    for (int j = 0; j < products.length(); j++) {
                        final JSONObject product = products.getJSONObject(j);

                        final LinearLayout productView = new LinearLayout(ActivityCart.this);
                        View.inflate(ActivityCart.this, R.layout.wj_item_cart_product, productView);
                        // 商品名称
                        ((TextView) productView.findViewById(R.id._tv_list_name)).setText(product.getString("SkuName"));
                        // 销售价
                        final double dbSellPric = new BigDecimal(product.getString("SellPrice")).doubleValue();
//						((TextView) productView.findViewById(R.id._tv_list_sale_price))
//								.setText("¥" + df.format(dbSellPric));

                        SetPriceSize.setPrice(((TextView) productView.findViewById(R.id._tv_list_sale_price)),
                                "¥" + df.format(dbSellPric), 25);
                        // 商品数量
                        ((TextView) productView.findViewById(R.id.tv_product_count))
                                .setText(product.getString("Quantity"));

                        // 商品缩略图
                        ImageOptions imageOptions = new ImageOptions.Builder()
                                .setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                                .setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
                                .build();

                        x.image().bind((ImageView) productView.findViewById(R.id._iv_list_thumbnails),
                                product.getString("ProductThumbnail"), imageOptions);
                        final String scid = product.getString("SCId");
                        productView.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                try {
                                    Intent ii = new Intent();
                                    ii.putExtra("IsFreeShipping", product.getString("producttype"));
                                    ii.putExtra("ProductSKUId", product.getString("SkuId"));
                                    ii.setClass(ActivityCart.this, ActivityProductDetailsNew.class);
                                    startActivity(ii);
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }
                        });

                        productView.setOnLongClickListener(new OnLongClickListener() {

                            @Override
                            public boolean onLongClick(View v) {
                                mDialog = new Dialog(ActivityCart.this, R.style.CustomDialogTheme);
                                dialogView = LayoutInflater.from(ActivityCart.this).inflate(R.layout.send_tel_dailog,
                                        null);
                                // 设置自定义的dialog布局
                                mDialog.setContentView(dialogView);
                                // false表示点击对话框以外的区域对话框不消失，true则相反
                                mDialog.setCanceledOnTouchOutside(false);
                                // -----------------------------------

                                mDialog.show();
                                // 获取自定义dialog布局控件
                                ((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("确定要从购物车删除?");
                                Button confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
                                Button cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
                                // 确定按钮点击事件
                                confirmBt.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        try {
                                            mDialog.dismiss();
                                            deleteOne(scid, product.getString("SkuId"));
                                        } catch (JSONException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                // 取消按钮点击事件
                                cancelBt.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        mDialog.dismiss();
                                    }
                                });
                                return false;
                            }
                        });

                        CheckBox productckb = (CheckBox) productView.findViewById(R.id.product_checkbox);
                        // 当paystate = 1时 复选款被选中
                        if ((product.getString("PayState")).equals("1")) {
                            productckb.setChecked(true);
                            ProductName = product.getString("SkuName");
                        } else {
                            productckb.setChecked(false);
                        }

                        productckb.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (((CheckBox) v).isChecked()) {
                                    try {
                                        changeStateOne("1", product.getString("SCId"), "4");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        changeStateOne("0", product.getString("SCId"), "4");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                        ((ImageView) productView.findViewById(R.id.iv_no_product)).setAlpha(0.6f);
                        int realstock = Integer.parseInt(product.getString("RealStock"));
                        if (realstock <= 10 && realstock != 0) {
                            ((TextView) productView.findViewById(R.id.tv_surplus_num))
                                    .setText("仅剩" + product.getString("RealStock") + "件");
                            ((TextView) productView.findViewById(R.id.tv_surplus_num)).setVisibility(View.VISIBLE);
                            ((ImageView) productView.findViewById(R.id.iv_no_product)).setVisibility(View.GONE);
                        } else if (realstock == 0) {
                            ((TextView) productView.findViewById(R.id.tv_surplus_num)).setVisibility(View.GONE);
                            ((ImageView) productView.findViewById(R.id.iv_no_product)).setVisibility(View.VISIBLE);
                        } else {
                            ((ImageView) productView.findViewById(R.id.iv_no_product)).setVisibility(View.GONE);
                            ((TextView) productView.findViewById(R.id.tv_surplus_num)).setVisibility(View.GONE);
                        }

                        // 减号点击事件
                        TextView mReduce = (TextView) productView.findViewById(R.id.tv_reduce);
                        mReduce.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    // 起订量
                                    int mMoq = Integer.parseInt(product.getString("MOQ"));
                                    // 显示的数量
                                    int mQuantity = Integer
                                            .parseInt(((TextView) productView.findViewById(R.id.tv_product_count))
                                                    .getText().toString());
                                    if (mQuantity > mMoq) {
                                        upDataCart("-1", product.getString("SkuId"), product.getString("producttype"));
                                    } else {
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // 加号点击事件
                        productView.findViewById(R.id.tv_add).setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    int realcount = Integer.parseInt(product.getString("RealStock"));
                                    int Quantity = Integer.parseInt(product.getString("Quantity"));
                                    if (Quantity < realcount) {
                                        upDataCart("1", product.getString("SkuId"), product.getString("producttype"));
                                    } else {
                                        showToast("您的购买数量已到最大库存量");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        productView.setLayoutParams(
                                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                        lstProducts.addView(productView);
                        mProductList.add(productView);

                        if (i < products.length() - 1) {
                            // 分割线
                            View view = new View(getContext());
                            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                    (int) getResources().getDimension(R.dimen.divider_line)));
                            view.setBackgroundColor(getResources().getColor(R.color.bg_divider_line_minus));
                            lstProducts.addView(view);
                        }

                    }
                    // ----------------------------

                    HouseView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    lstHouses.addView(HouseView);
                    mViewList.add(HouseView);

                    if (i < warhousers.length() - 1) {
                        // 分割线
                        View view = new View(getContext());
                        view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                (int) getResources().getDimension(R.dimen.divider_line)));
                        view.setBackgroundColor(getResources().getColor(R.color.bg_divider_line_minus));
                        lstHouses.addView(view);
                    }

                }
            } catch (

                    Exception e) {
            }

        }

    }

    /**
     * 更改区域状态
     *
     * @param Paystate
     * @param Scid
     * @param TypeID
     */
    private void changeState(String Paystate, String Scid, String TypeID) {
        String Ts = MD5.getTimeStamp();

        String str = "Key=" + Constants.WEBAPI_KEY + "&Paystate=" + Paystate + "&Scid=" + Scid + "&Ts=" + Ts
                + "&TypeID=" + TypeID + "&UserID=" + userid + "&UUID=" + getIMEI1();

        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwPayState");
        params.addBodyParameter("TypeID", TypeID);
        params.addBodyParameter("Scid", Scid);
        params.addBodyParameter("UUID", getIMEI1());
        params.addBodyParameter("Paystate", Paystate);
        params.addBodyParameter("UserID", userid);

        params.addBodyParameter("Sign", MD5.Md5(str));
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        getCartContent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    /**
     * 改变单个商品状态
     *
     * @param Paystate
     * @param Scid
     * @param TypeID
     */
    private void changeStateOne(String Paystate, String Scid, String TypeID) {
        String Ts = MD5.getTimeStamp();

        String str = "Key=" + Constants.WEBAPI_KEY + "&Paystate=" + Paystate + "&Scid=" + Scid + "&Ts=" + Ts
                + "&TypeID=" + TypeID + "&UserID=" + userid + "&UUID=" + getIMEI1();

        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwPayState");
        params.addBodyParameter("TypeID", TypeID);
        params.addBodyParameter("Scid", Scid);
        params.addBodyParameter("UUID", getIMEI1());
        params.addBodyParameter("Paystate", Paystate);
        params.addBodyParameter("UserID", userid);

        params.addBodyParameter("Sign", MD5.Md5(str));
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        getCartContent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    /**
     * 更新购物车
     *
     * @param Quantity
     * @param SkuId
     * @param ProductType
     */
    private void upDataCart(String Quantity, String SkuId, String ProductType) {

        String shopid = getBindingShop();

        String Ts = MD5.getTimeStamp();
        String str = "Key=" + Constants.WEBAPI_KEY + "&MUserId=" + shopid + "&Paystate=0" + "&ProductType="
                + ProductType + "&Quantity=" + Quantity + "&SkuId=" + SkuId + "&Ts=" + Ts + "&UserId=" + userid
                + "&UUID=" + getIMEI1() + "&WarehouseId=0";

        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwAdd");
        params.addBodyParameter("ProductType", ProductType);
        params.addBodyParameter("SkuId", SkuId);
        params.addBodyParameter("MUserId", shopid);
        params.addBodyParameter("Paystate", "0");
        params.addBodyParameter("WarehouseId", "0");
        params.addBodyParameter("UUID", getIMEI1());
        params.addBodyParameter("Quantity", Quantity);
        params.addBodyParameter("UserId", userid);

        params.addBodyParameter("Sign", MD5.Md5(str));
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        getCartContent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    private void deleteProduct() {
        String uuid = getIMEI1();
        String Ts = MD5.getTimeStamp();
        String str = "Key=" + Constants.WEBAPI_KEY + "&Ts=" + Ts + "&UserId=" + userid + "&UUID=" + uuid;
        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwDeleteList");
        params.addBodyParameter("UUID", uuid);
        params.addBodyParameter("UserId", userid);
        params.addBodyParameter("Sign", MD5.Md5(str));
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        getCartContent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    /**
     * 去结算
     */
    private void CheckOut() {


        Map<String, String> map = new HashMap<String, String>();
        String Ts = MD5.getTimeStamp();
        map.put("UserId", getLoginUserId());
        map.put("TypeId", "3");
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("AddressId", addressId);
        map.put("RPRID", "0");
        map.put("Ts", Ts);

        // RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS +
        // "api/CSC/BpwCheckOut");

        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwCheckOut_New");
        params.addBodyParameter("UserId", getLoginUserId());
        params.addBodyParameter("TypeId", "3");
        params.addBodyParameter("Sign", NetUtils.getSign(map));
        params.addBodyParameter("AddressId", addressId);
        params.addBodyParameter("RPRID", "0");
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        Intent i = new Intent();
                        i.putExtra("result", result);
                        i.putExtra("JsonSkuName", ProductName);
                        forward(ActivityConfirmOrder.class, i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 删除单个
     *
     * @param scid
     * @param skuid
     */
    private void deleteOne(String scid, String skuid) {
        String Ts = MD5.getTimeStamp();
        String str = "Key=" + Constants.WEBAPI_KEY + "&SCID=" + scid + "&Ts=" + Ts;

        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwDelete");
        params.addBodyParameter("SCID", scid);
        params.addBodyParameter("Sign", MD5.Md5(str));
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        getCartContent();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                showErr();
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });
    }

    /**
     * 根据条码搜索商品判断是否有直邮现货
     *
     * @param code
     */
    private void search(String shopid, final String code) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", shopid);
        map.put("EanCode", code);
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

        String url = Constants.WEBAPI_ADDRESS + "api/ProductNew/ProductListScanCodeBpw?UserId=" + shopid + "&EanCode="
                + code + "&Sign=" + Sign + "&Ts=" + Ts;
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        JSONArray ja = jo.getJSONArray("Data");
                        int i = ja.length();
                        if (i > 1) {
                            // 当i大于1 说明有直邮有现货 跳到选择界面
//                            Intent c = new Intent();
//                            c.putExtra("ScanCode", code);
//                            forward(ActivityScanResult.class, c);
                            getProduct(code);
                        } else if (i == 1) {
                            // 直接购买 然后跳到购物车
                            JSONObject jo1 = ja.getJSONObject(0);
                            String skuid = jo1.getString("ProductSKUId");
                            String otype = jo1.getString("IsDirectMail");
                            AddCart("1", skuid, otype);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }

    private void AddCart(String Quantity, String SkuId, String ProductType) {

        String shopid = getBindingShop();

        String Ts = MD5.getTimeStamp();
        String str = "Key=" + Constants.WEBAPI_KEY + "&MUserId=" + shopid + "&Paystate=0" + "&ProductType="
                + ProductType + "&Quantity=" + Quantity + "&SkuId=" + SkuId + "&Ts=" + Ts + "&UserId=" + userid
                + "&UUID=" + getIMEI1() + "&WarehouseId=0";

        RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/CSC/BpwAdd");
        params.addBodyParameter("ProductType", ProductType);
        params.addBodyParameter("SkuId", SkuId);
        params.addBodyParameter("MUserId", shopid);
        params.addBodyParameter("Paystate", "0");
        params.addBodyParameter("WarehouseId", "0");
        params.addBodyParameter("UUID", getIMEI1());
        params.addBodyParameter("Quantity", Quantity);
        params.addBodyParameter("UserId", userid);

        params.addBodyParameter("Sign", MD5.Md5(str));
        params.addBodyParameter("Ts", Ts);
        params.setAsJsonContent(true);

        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        showSuccess();
                        getCartContent();

//						 startActivity(new Intent(ActivityCart.this,ActivityAddCartSuccess.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }


    /**
     * 获取默认地址的Id
     */
    private void GetDefultAddress() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", getLoginUserId());
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/Address/BpwDefault?UserId=" + getLoginUserId() + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {

                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {

                        JSONObject mJsonData = jo.getJSONObject("Data");

                        addressId = mJsonData.getString("Id");

                    } else if (jsonresult.equals("0")) {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
            }

            @Override
            public void onCancelled(Callback.CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }
        });

    }


    /**
     * 显示扫码成功加入购物车界面
     */
    private void showSuccess() {
        AddCartPopWindow = new AddCartSuccess(ActivityCart.this, itemsOnClick);
        AddCartPopWindow.showAtLocation(findViewById(R.id.ll_cart_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    AddScanResult addScanResult;

    /**
     * 显示扫码成功 购物车商品选择界面
     */
    private void showScanResult(String string) {
        addScanResult = new AddScanResult(ActivityCart.this, SelectitemsOnclick, string);
        addScanResult.showAtLocation(findViewById(R.id.ll_cart_main), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }


    /**
     * 扫码成功加入购物车界面点击事件
     */
    private View.OnClickListener itemsOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_jixu:
                    AddCartPopWindow.dismiss();
                    Intent intent = new Intent(ActivityCart.this, CaptureActivity.class);
                    startActivityForResult(intent, 1);
                    break;
                case R.id.btn_go_cart:// 取消
                    AddCartPopWindow.dismiss();
                    break;
                case R.id.iv_close:// 取消
                    AddCartPopWindow.dismiss();
                    break;
            }
        }
    };

    private View.OnClickListener SelectitemsOnclick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.addCart:
                    addScanResult.dismiss();
                    addScanResult.setData(new AddScanResult.OnGetData() {
                        @Override
                        public void OnGetDataCallBack(String skuid, String otypeid) {
                            AddCart("1", skuid, otypeid);
                        }
                    });
                    break;
                case R.id.iv_close:
                    addScanResult.dismiss();
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            String scanResult = SecurityUtil.decode(data.getStringExtra(Constants.INTENT_EXTRA_SCAN_RESULT));

            // 2016/04/15 需求变更 修正
            // 店铺的情况
            if (scanResult.length() < 8) {
                showToast(getString(R.string.msg_err_no_product));
            } else {
                // 商品的情况
                String shopCd = getBindingShop();

                // 还没有扫描店铺的时候
                if (StringUtils.isNull(shopCd)) {
                    showToast(getString(R.string.msg_err_no_binding_shop));
                } else {
                    search(shopCd, scanResult);
                }
            }

        }
    }






    private void getProduct(String code) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", getBindingShop());
        map.put("EanCode", code);
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

        String str = "http://newapi.bopinwang.com/" + "api/ProductNew/ProductListScanCodeBpw?UserId=" + getBindingShop()
                + "&EanCode=" + code + "&Sign=" + Sign + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(str, null, new GetCallBack(), this);
    }

    class GetCallBack implements XutilsHttp.XCallBack {

        @Override
        public void onResponse(String result) {
            showScanResult(result);
        }

    }

}
