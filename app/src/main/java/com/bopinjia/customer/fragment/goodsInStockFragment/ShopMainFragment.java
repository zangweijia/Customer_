package com.bopinjia.customer.fragment.goodsInStockFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.activity.ActivityProductDetailsNew;
import com.bopinjia.customer.activity.BaseActivity;
import com.bopinjia.customer.adapter.AdapterProductGridViewClassSub;
import com.bopinjia.customer.bean.ProductGridviewClassSubBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.LazyFragment;
import com.bopinjia.customer.fragment.shopmainClassify.GridViewAdapter;
import com.bopinjia.customer.fragment.shopmainClassify.Model;
import com.bopinjia.customer.fragment.shopmainClassify.ViewPagerAdapter;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@ContentView(R.layout.fragment_shop_main)
public class ShopMainFragment extends LazyFragment {


    private GridView gridview;
    private List<ProductGridviewClassSubBean> mList;
    private AdapterProductGridViewClassSub mAdapter;
    // 标志位，标志已经初始化完成。
    private boolean isPrepared;
    CirclePageIndicator _Tabs;
    /**
     * 检索
     */
    private int PageIndex = 1;
    /**
     * 一共多少页
     */
    private String mAllPages;
    private ViewPager viewpager;
    private List<View> mPagerList;
    private List<Model> mDatas = new ArrayList<>();
    private LayoutInflater inflater;
    /**
     * 总的页数
     */
    private int pageCount;
    /**
     * 每一页显示的个数
     */
    private int pageSize = 6;
    /**
     * 当前显示的是第几页
     */
    private int curIndex = 0;
    NestedScrollView scroller;
    List<ProductGridviewClassSubBean> dataList;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop_main, null);
        initView(view);
        isPrepared = true;
        lazyLoad();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("IsFreeShipping", "0");
                intent.putExtra("ProductSKUId", mList.get(i).getId());
                intent.setClass(getActivity(), ActivityProductDetailsNew.class);
                startActivity(intent);
            }
        });
        scroller.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    if (PageIndex < Integer.parseInt(mAllPages)) {
                        PageIndex += 1;
                        getProductList(1);
                    } else if (PageIndex >= Integer.parseInt(mAllPages)) {
                        gridview.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                ((BaseActivity) getActivity()).showToast("没有更多了~");
                            }
                        }, 0);
                    }
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                init();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //设置样式刷新显示的位置
        mSwipeRefreshLayout.setProgressViewOffset(true, -20, 100);
        gridview.setFocusable(false);
    }

    private void initView(View view) {
        gridview = (GridView) view.findViewById(R.id.gridview);
        viewpager = (ViewPager) view.findViewById(R.id.viewpager);
        _Tabs = (CirclePageIndicator) view.findViewById(R.id._indicator);
        scroller = (NestedScrollView) view.findViewById(R.id.main_scrollview);

    }

    @Override
    protected void lazyLoad() {
        if (!isPrepared || !isVisible) {
            return;
        } else {
            init();
        }
    }

    /**
     * 加载数据
     */
    private void init() {
        curIndex = 0;
        mDatas.clear();
        getGridViewData();
        getProductList(0);

    }

    private void getSubCategoryNext() {
        inflater = LayoutInflater.from(getActivity());
        //总的页数=总数/每页数量，并取整
        pageCount = (int) Math.ceil(mDatas.size() * 1.0 / pageSize);
        mPagerList = new ArrayList<View>();
        for (int i = 0; i < pageCount; i++) {
            // 每个页面都是inflate出一个新实例
            GridView gridView = (GridView) inflater.inflate(R.layout.gridview, viewpager, false);
            gridView.setAdapter(new GridViewAdapter(getActivity(), mDatas, i, pageSize));
            mPagerList.add(gridView);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int pos = position + curIndex * pageSize;
                    Intent intent = new Intent();
                    intent.putExtra("IsFreeShipping", "0");
                    intent.putExtra("ProductSKUId", mDatas.get(pos).getId());
                    intent.setClass(getActivity(), ActivityProductDetailsNew.class);
                    startActivity(intent);
                }
            });
        }
        //设置适配器
        viewpager.setAdapter(new ViewPagerAdapter(mPagerList));
        _Tabs.setViewPager(viewpager);
        viewpager.setCurrentItem(0);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageSelected(int position) {
                curIndex = position;
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            public void onPageScrollStateChanged(int arg0) {
            }
        });

    }


    /**
     * 获取底部商品列表
     *
     * @param id
     */
    private void getProductList(final int id) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        String MuserId = ((BaseActivity) getActivity()).getBindingShop();
        map.put("UserId", MuserId);
        map.put("ZY", "0");
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

        String url = Constants.WEBAPI_ADDRESS + "api/Product/GetZyNewtodayProductlist?UserId=" + MuserId + "&ZY=" + "0"
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
                dataList = new ArrayList<ProductGridviewClassSubBean>();
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject data = dataArray.getJSONObject(i);
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
                    dataList.add(m);
                }
                switch (id) {
                    case 0:
                        mList = dataList;
                        mAdapter = new AdapterProductGridViewClassSub(mList, getActivity(), R.layout.wj_item_class_sub);
                        gridview.setAdapter(mAdapter);
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
     * 获取店长推荐GridView 数据
     */
    private void getGridViewData() {
        String Mid = ((BaseActivity) getActivity()).getBindingShop();
        Map<String, String> map = new HashMap<String, String>();

        String Ts = MD5.getTimeStamp();
        map.put("UserId", Mid);
        map.put("ZY", "0");
        map.put("PageIndex", "1");
        map.put("pageSize", "24");

        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);
        String url = Constants.WEBAPI_ADDRESS + "api/Product/GetMDtopProductlist?UserId=" + Mid + "&ZY=0&PageIndex=1&pageSize=24" + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new getGridViewDataCallBack(), getActivity());
    }

    class getGridViewDataCallBack implements XutilsHttp.XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {

                    JSONArray dataArray = jo.getJSONObject("Data").getJSONArray("Records");

                    if (dataArray != null && dataArray.length() > 0) {
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject data = dataArray.getJSONObject(i);
                            Model m = new Model();
                            m.setIconRes(data.getString("ProductThumbnail"));
                            m.setName(data.getString("ProductSKUName"));
                            m.setPrice("¥" + data.getString("ScanPrice"));
                            m.setId(data.getString("ProductSKUId"));
                            mDatas.add(m);
                        }
                        getSubCategoryNext();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
