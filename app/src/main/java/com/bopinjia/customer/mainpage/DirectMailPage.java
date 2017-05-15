package com.bopinjia.customer.mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityCategory;
import com.bopinjia.customer.activity.ActivityFXGL;
import com.bopinjia.customer.activity.ActivityFXIntroduce;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.ActivitySearch;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.activityhome.ActivityPersonalCare;
import com.bopinjia.customer.adapter.AdapterHomeCategoryGrid;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.adapter.MyImageViewPagerAdapter;
import com.bopinjia.customer.bean.HomeCategoryBean;
import com.bopinjia.customer.bean.HorizontallistViewBean;
import com.bopinjia.customer.bean.ImageViewListBean;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.MyScrollView;
import com.bopinjia.customer.view.MyScrollView.OnScrollListener;
import com.bopinjia.customer.view.MyScrollView.OnScrollToBottomListener;
import com.bopinjia.customer.view.NoScrollGridView;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@ContentView(R.layout.fragment_directmailpage)
public class DirectMailPage extends Fragment {

    @ViewInject(R.id.scrollview)
    private MyScrollView mScrollview;

    @ViewInject(R.id.ll_title)
    private LinearLayout mTitle;

    @ViewInject(R.id.ll_hlist)
    private LinearLayout mHList;

    @ViewInject(R.id.rl)
    private RelativeLayout mTitleImg;

    @ViewInject(R.id._indicator)
    private CirclePageIndicator mIndicator;

    @ViewInject(R.id._pager)
    private ViewPager mPager;

    @ViewInject(R.id.bottom_gridview)
    private NoScrollGridView mBottomGridView;

    @ViewInject(R.id.category_gridview)
    private NoScrollGridView mCategoryGridView;

    @ViewInject(R.id.tvmore)
    private TextView tvmore;

    private List<ProductGridviewClassSubBean> mList;
    private AdapterProductGridViewClassSub mAdapter;
    /**
     * 检索
     */
    private int PageIndex = 1;
    /**
     * 一共多少页
     */
    private String mAllPages;

    private List<ImageViewListBean> piclists;

    // 首页分类栏
    private List<HomeCategoryBean> lista;

    private boolean isLogged;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return x.view().inject(this, inflater, container);
    }

    /**
     * 当界面重新展示时（fragment.show）,调用onrequest刷新界面
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            // onResume
//			隐藏HomeActivity 顶部
            LinearLayout mTitle = (LinearLayout) getActivity().findViewById(R.id.title);
            mTitle.setVisibility(View.GONE);
        } else {
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTitle.getBackground().setAlpha(0);
        getProductList(0, "0");
        initScrollviewListener();
        init();
        viewSetOnClick();
    }

    /**
     * 初始化数据
     */
    private void init() {
        ProductList();
        isLogged = ((BaseActivity) getActivity()).isLogged();
        List<HorizontallistViewBean> list = new ArrayList<HorizontallistViewBean>();
        for (int i = 0; i < 20; i++) {
            HorizontallistViewBean m = new HorizontallistViewBean();
            m.setImg("http://images.bopinjia.com//ProductMSJ/20161014/image/357X357_201610141148150391.png");
            m.setMarketprice("¥" + "0000");
            m.setPrice("¥" + "12345");
            list.add(m);
        }
        setHlist(list);
        getAppMenu();

        mCategoryGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Intent intent = new Intent();
                intent.putExtra("type", 1);
                intent.putExtra("code", lista.get(arg2).getCcode());
                intent.putExtra("titlename", lista.get(arg2).getName());
                intent.putExtra("id", lista.get(arg2).getIndex());
                intent.setClass(getActivity(), ActivityPersonalCare.class);
                startActivity(intent);


//                if (lista.get(arg2).getIndex().equals("5")) {
//                    // 超级会员
//                    if (isLogged) {
//                        getIsDistribution();
//                    } else {
//                        ((BaseActivity) getActivity()).forward(ActivityLogin.class);
//                    }
//
//                } else {
//                    Intent intent = new Intent();
//                    intent.putExtra("type", 1);
//                    // type == 0 原生分类 type==1 H5 活动界面
//                    if (lista.get(arg2).getApp_type().equals("0")) {
//                        // 原生分类
//                        // code值
//                        intent.putExtra("code", lista.get(arg2).getCcode());
//                        intent.putExtra("titlename", lista.get(arg2).getName());
//                        intent.putExtra("id", lista.get(arg2).getIndex());
//                        intent.setClass(getActivity(), ActivityPersonalCare.class);
//                        startActivity(intent);
//                    } else if (lista.get(arg2).getApp_type().equals("1")) {
//                        // 活动界面
//                        Toast.makeText(getActivity(), lista.get(arg2).getCcode(), Toast.LENGTH_SHORT).show();
//                    }
//                }
            }
        });

        mCategoryGridView.setFocusable(false);
        mBottomGridView.setFocusable(false);

    }

    private void viewSetOnClick() {
        mBottomGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Intent i = new Intent();
                i.putExtra("IsFreeShipping", "1");
                i.putExtra("ProductSKUId", mList.get(arg2).getId());
                i.setClass(getActivity(), ActivityProductDetailsNew.class);
                startActivity(i);
            }
        });

    }

    /**
     * 设置横向商品列表
     *
     * @param list
     */
    private void setHlist(List<HorizontallistViewBean> list) {
        for (int i = 0; i < list.size(); i++) {
            LinearLayout productView = new LinearLayout(getActivity());
            View.inflate(getActivity(), R.layout.wj_item_miaosha_list, productView);

            ((TextView) productView.findViewById(R.id.txt_sell_price)).setText(list.get(i).getPrice());
            ((TextView) productView.findViewById(R.id.txt_market_price)).setText(list.get(i).getMarketprice());
            ((TextView) productView.findViewById(R.id.txt_market_price)).getPaint().setStrikeThruText(true);
            // 商品缩略图
            ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
                    .build();

            x.image().bind((ImageView) productView.findViewById(R.id.iv), list.get(i).getImg(), imageOptions);
            mHList.addView(productView);
        }
    }

    private void getAppMenu() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/AppMenu/GetAppMenu?Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new getAppMenuCallBack(), getActivity());

    }

    class getAppMenuCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {

            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {

                    JSONArray jsonarray = jo.getJSONArray("Data");

                    lista = new ArrayList<HomeCategoryBean>();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject date = jsonarray.getJSONObject(i);
                        HomeCategoryBean m = new HomeCategoryBean();
                        m.setImg(date.getString("App_Ico"));
                        m.setName(date.getString("App_Name"));
                        m.setCcode(date.getString("App_Url"));
                        m.setApp_type(date.getString("App_Type"));
                        m.setIndex(date.getString("App_Order"));
                        lista.add(m);
                    }

                    AdapterHomeCategoryGrid adapter = new AdapterHomeCategoryGrid(lista, getActivity(),
                            R.layout.wj_item_home_category, false);
                    mCategoryGridView.setAdapter(adapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void ProductList() {
        String MuserId = ((BaseActivity) getActivity()).getBindingShop();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("SkuId", MuserId);
        map.put("ZY", "1");
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/Product/GetIndexadlist?UserId=" + MuserId + "&ZY=" + "1" + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new ProductListCallBack(), getActivity());

    }

    class ProductListCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {

                    JSONArray piclist = jo.getJSONObject("Data").getJSONArray("indexChannelAD");
                    piclists = new ArrayList<ImageViewListBean>();
                    for (int i = 0; i < piclist.length(); i++) {
                        ImageViewListBean m = new ImageViewListBean();
                        m.setImg(piclist.getJSONObject(i).getString("ModuleImg"));
                        m.setName(piclist.getJSONObject(i).getString("ModuleName"));
                        m.setUrl(piclist.getJSONObject(i).getString("ModuleURL"));
                        piclists.add(m);
                    }

                    if (piclists.size() == 1) {
                        mIndicator.setVisibility(View.GONE);
                    }

                    List<View> images = new ArrayList<View>();
                    for (int i = 0; i < piclists.size(); i++) {
                        String pic = piclists.get(i).getImg();
                        ImageView imageView = new ImageView(getActivity());
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                        ((BaseActivity) getActivity()).setImageFromUrl(pic, imageView);
                        images.add(imageView);

                    }
                    mPager.setAdapter(new MyImageViewPagerAdapter(getActivity(), images, piclists));
                    mIndicator.setViewPager(mPager);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取底部商品列表
     *
     * @param id
     * @param OrderBy
     */
    private void getProductList(final int id, String OrderBy) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        String MuserId = ((BaseActivity) getActivity()).getBindingShop();
        map.put("UserId", MuserId);
        map.put("ZY", "1");
        map.put("PageIndex", String.valueOf(PageIndex));
        map.put("pageSize", "20");
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

        String url = Constants.WEBAPI_ADDRESS + "api/Product/GetZyNewtodayProductlist?UserId=" + MuserId + "&ZY=" + "1"
                + "&PageIndex=" + String.valueOf(PageIndex) + "&pageSize=" + "20" + "&Sign=" + Sign + "&Ts=" + Ts;

        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override

            public void onSuccess(String result) {
                try {
                    JSONObject jo = new JSONObject(result);
                    String jsonresult = jo.getString("Result");
                    if (jsonresult.equals("1")) {
                        parseList(result, id);
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
     * 解析商品列表
     *
     * @param jsonarray
     * @param id
     */
    private void parseList(String jsonarray, int id) {
        try {

            JSONObject jo = new JSONObject(jsonarray);
            JSONObject jsondata = jo.getJSONObject("Data");

            JSONArray dataArray = jsondata.getJSONArray("Records");
            JSONObject Paging = jsondata.getJSONObject("Paging");
            mAllPages = Paging.getString("Pages");
            if (dataArray != null && dataArray.length() > 0) {
                List<ProductGridviewClassSubBean> dataList = new ArrayList<ProductGridviewClassSubBean>();

                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
                    ProductGridviewClassSubBean m = new ProductGridviewClassSubBean();

                    m.setImg(data.getString("ProductThumbnail"));
                    m.setMarketprice(data.getString("MarketPrice"));
                    m.setIsshiping("1");
                    m.setNumber(data.getString("CumulativeSales"));
                    m.setName(data.getString("ProductSKUName"));
                    m.setPrice(data.getString("ScanPrice"));
                    m.setId(data.getString("ProductSKUId"));
                    m.setCountry(data.getString("CountryName"));
                    m.setCountryimg(data.getString("CountryImageUrl"));
                    m.setRealStock(data.getString("RealStock"));
                    dataList.add(m);
                }
                switch (id) {
                    case 0:
                        mList = dataList;
                        mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
                        mBottomGridView.setAdapter(mAdapter);

                        break;
                    case 1:
                        if (dataList != null && !dataList.isEmpty()) {
                            mList.addAll(dataList);
                            mAdapter.notifyDataSetChanged();
                        }
                        break;

                    default:
                        break;
                }

            } else {
            }
        } catch (Exception e) {

        }

    }

    /**
     * 设置srrollview 底部监听 与滑动监听
     */
    private void initScrollviewListener() {
        mScrollview.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScroll(int scrollY) {
                int[] ints = new int[2];
                mTitleImg.getLocationOnScreen(ints);
                // int scrollY = -ints[1] + scrollYQ;
                // mImage这个view的高度
                int imageHeight = mTitleImg.getHeight();
                if (mTitleImg != null && imageHeight > 0) {
                    // 如果“图片”没有向上滑动，设置为全透明
                    if (scrollY < 10) {
                        mTitle.getBackground().setAlpha(0);
                    } else {
                        // “图片”已经滑动，而且还没有全部滑出屏幕，根据滑出高度的比例设置透明度的比例
                        if (scrollY < imageHeight) {
                            int progress = (int) (new Float(scrollY) / new Float(imageHeight) * 255);// 255
                            mTitle.getBackground().setAlpha(progress);
                        } else {
                            // “图片”全部滑出屏幕的时候，设为完全不透明
                            mTitle.getBackground().setAlpha(255);
                        }
                    }
                }

            }
        });
        mScrollview.setOnScrollToBottomLintener(new OnScrollToBottomListener() {

            @Override
            public void onScrollBottomListener(boolean isBottom) {
                if (isBottom) {
                    if (PageIndex < Integer.parseInt(mAllPages)) {
                        PageIndex += 1;
                        getProductList(1, "0");
                        tvmore.setText("加载更多");
                    } else if (PageIndex >= Integer.parseInt(mAllPages)) {
                        tvmore.setText("没有更多了");
                    }
                }

            }
        });

    }

    /**
     * 判断是否为经销商
     */
    private void getIsDistribution() {
        String s = ((BaseActivity) getActivity()).getLoginUserId();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", s);
        map.put("MDUserId", ((BaseActivity) getActivity()).getBindingShop());

        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GDSExists?UserId=" + s + "&MDUserId="
                + ((BaseActivity) getActivity()).getBindingShop() + "&Sign=" + NetUtils.getSign(map) + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new IsDistributionCallBack(), getActivity());

    }

    /**
     * 判断是否为经销商回调
     *
     * @author ZWJ
     */
    class IsDistributionCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    String Data = jo.getString("Data");

                    if (Data.equals("0")) {
                        // 一般客户
                        ((BaseActivity) getActivity()).forward(ActivityFXIntroduce.class);
                    } else if (Data.equals("1")) {
                        // 分销商
                        ((BaseActivity) getActivity()).forward(ActivityFXGL.class);
                    } else if (Data.equals("2")) {
                        // 一般用户 可申请分销商
                        ((BaseActivity) getActivity()).forward(ActivityFXIntroduce.class);
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    @Event(value = {R.id._zhiyou_classify, R.id._zhiyou_search, R.id._zhiyou_scan})
    private void getEvent(View v) {
        switch (v.getId()) {
            case R.id._zhiyou_classify:
                // 分类
                Intent toClass = new Intent();
                toClass.putExtra("type", 1);
                toClass.setClass(getActivity(), ActivityCategory.class);
                startActivity(toClass);
                break;
            case R.id._zhiyou_search:
                // 搜索
                Intent toSearch = new Intent();
                toSearch.putExtra("type", 1);
                toSearch.setClass(getActivity(), ActivitySearch.class);
                startActivity(toSearch);
                break;
            case R.id._zhiyou_scan:
                // 扫码
                Intent toScan = new Intent(getActivity(), CaptureActivity.class);
                startActivityForResult(toScan, 1);
                break;

            // case R.id.iv_red:
            //
            // ((BaseActivity) getActivity()).forward(ActivityLogin.class);
            // break;

            default:
                break;
        }

    }

}
