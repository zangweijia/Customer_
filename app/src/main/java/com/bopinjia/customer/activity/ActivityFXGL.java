package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.bopinjia.customer.view.NoScrollGridView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityFXGL extends BaseActivity {

    private TextView mTiTleBack;
    private TextView mTiTleName;
    private GridView mGrid;
    private ScrollView sv_container;
    /**
     * 收入金额
     */
    private TextView mAmount;
    /**
     * 收益
     */
    private TextView mProfit;
    /**
     * 交易额
     */
    private TextView mTranstance;
    /**
     * 订单数
     */
    private TextView mOrder;
    /**
     * 续费
     */
    private TextView mXUFEI;
    /**
     * 会员中心
     */
    private TextView tv_hyzx;

    private String typename;
    private TextView mReflect;

    private String level;
    /**
     * 累计收益金额
     */
    private String cumulativeMoney;
    /**
     * 可提现金额
     */
    private String mDGDSM_ToMyCashMoney;
    /**
     * 我的余额
     */
    private String mDGDSM_ToMyMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_activity_fxgl);
        x.view().inject(this);
        setTitle();
        init();
        getDistributionInfo();
    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);
        mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
        mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
        mTiTleName.setText("分销管理");

    }

    @Override
    protected void onResume() {
        super.onResume();
        getDistributionInfo();
        if (getIntent().hasExtra("shengji")) {

        }

    }

    @Event(value = {R.id.btn_return, R.id.tv_xf, R.id.tv_hyzx, R.id.tv_tixian, R.id.ll_shop_info})
    private void getEvent(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.tv_xf:
                Intent i = new Intent();
                i.putExtra("type", "2");
                i.putExtra("id", "1");
                // 续费标识
                i.putExtra("mytype", "1");
                i.putExtra("name", typename);
                forward(ActivityFXDisPay.class, i);
                break;
            case R.id.tv_hyzx:
                forward(ActivityFXSPersonalCenter.class);
                break;
            case R.id.ll_shop_info:
                forward(ActivityFXSStoreInfo.class);
                break;
            case R.id.tv_tixian:
                Intent ii = new Intent();
                ii.putExtra("reflectaccount", mDGDSM_ToMyCashMoney);
                forward(ActivityFXReflect.class, ii);
                break;
            default:
                break;
        }
    }

    private void init() {

        mAmount = (TextView) findViewById(R.id.tv_amount);
        mProfit = (TextView) findViewById(R.id.tv_profit);
        mTranstance = (TextView) findViewById(R.id.tv_transtance);
        mOrder = (TextView) findViewById(R.id.tv_order);

        mXUFEI = (TextView) findViewById(R.id.tv_xf);
        tv_hyzx = (TextView) findViewById(R.id.tv_hyzx);

        // ScrollView
        sv_container = (ScrollView) findViewById(R.id.scrollview);
        // gridView
        mGrid = (NoScrollGridView) findViewById(R.id.grid);

        SimpleAdapter mAdapter = new SimpleAdapter(this, getData(), R.layout.wj_itm_fxgl,
                new String[]{"image", "name"}, new int[]{R.id.image, R.id.tv_name});
        mGrid.setAdapter(mAdapter);

        mGrid.post(new Runnable() {
            public void run() {
                // sv_container.fullScroll(ScrollView.FOCUS_UP);
                // 解决scrollview 嵌套gridview 自动到底部
                sv_container.scrollTo(0, 0);
            }
        });

        mGrid.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (arg2 == 0) {
                    // 高佣金商品
                    Intent i = new Intent();
                    i.putExtra("level", level);
                    forward(ActivityFXCommissionProduct.class, i);
                } else if (arg2 == 1) {
                    // 爆款促销
                    Intent i = new Intent();
                    i.putExtra("level", level);
                    forward(ActivityFXExplosionProduct.class, i);
                } else if (arg2 == 2) {
                    // 订单管理
                    forward(ActivityFXOrder.class);
                } else if (arg2 == 3) {
                    // 客户管理
                    Intent i = new Intent();
                    i.putExtra("cumulativeMoney", cumulativeMoney);
                    forward(ActivityFXClientList.class, i);
                } else if (arg2 == 4) {
                    // 店铺管理
                    forward(ActivityFXSStoreInfo.class);
                } else if (arg2 == 5) {
                    // 红包设置
                    showToast("敬请期待更多功能");
                }

            }
        });

    }

    /**
     * SimpleAdapter数据源
     *
     * @return
     */
    private ArrayList<HashMap<String, Object>> getData() {
        ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
        tempHashMap.put("image", R.drawable.ic_fxgl_gyj);
        tempHashMap.put("name", getString(R.string.fxgl_gyj));
        arrayList.add(tempHashMap);

        tempHashMap = new HashMap<String, Object>();
        tempHashMap.put("image", R.drawable.ic_fxgl_bkcx);
        tempHashMap.put("name", getString(R.string.fxgl_bkcx));
        arrayList.add(tempHashMap);

        tempHashMap = new HashMap<String, Object>();
        tempHashMap.put("image", R.drawable.ic_fxgl_ddgl);
        tempHashMap.put("name", getString(R.string.fxgl_ddgl));
        arrayList.add(tempHashMap);

        tempHashMap = new HashMap<String, Object>();
        tempHashMap.put("image", R.drawable.ic_fxgl_khgl);
        tempHashMap.put("name", getString(R.string.fxgl_khgl));
        arrayList.add(tempHashMap);

        tempHashMap = new HashMap<String, Object>();
        tempHashMap.put("image", R.drawable.ic_fxgl_dpgl);
        tempHashMap.put("name", getString(R.string.fxgl_dpgl));
        arrayList.add(tempHashMap);

        tempHashMap = new HashMap<String, Object>();
        tempHashMap.put("image", R.drawable.ic_more);
        tempHashMap.put("name", getString(R.string.fxgl_jqqd));
        arrayList.add(tempHashMap);

        return arrayList;

    }

    /**
     * 获取分销商信息
     */
    private void getDistributionInfo() {
        String s = getLoginUserId();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", s);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GetGDSUserInfo?UserId=" + s + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;
        getDistributionInfocallback c = new getDistributionInfocallback();
        XutilsHttp.getInstance().get(url, null, c, this);

    }

    class getDistributionInfocallback implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject Data = jo.getJSONObject("Data");

                    level = Data.getString("GDSType_Level");

                    ((TextView) findViewById(R.id.tv_shopname)).setText(Data.getString("MDGDSM_ShopName"));
                    typename = Data.getString("GDSType_Name");

                    ((TextView) findViewById(R.id.tv_hyzx)).setText(typename);
                    ((TextView) findViewById(R.id.tv_endtime)).setText("有效期至：" + Data.getString("MDGDSR_EndDate"));
                    setImageURl(R.id.iv_fximg, Data.getString("GDSType_Img"));

                    ImageFromUrl(Data.getString("MDGDSM_ShopLogo"), R.id.iv_img);

                    // 收入金额
                    mAmount.setText(Data.getString("MDGDSM_ToDayMoney"));
                    cumulativeMoney = Data.getString("MDGDSM_CumulativeMoney");
                    mProfit.setText(cumulativeMoney);
                    // 交易额
                    mTranstance.setText(Data.getString("MDGDSM_ToMonthMoney"));
                    // 订单数
                    mOrder.setText(Data.getString("MDGDSM_ToMonthOrderCount"));

                    mDGDSM_ToMyCashMoney = Data.getString("MDGDSM_ToMyCashMoney");
                    mDGDSM_ToMyMoney = Data.getString("MDGDSM_ToMyMoney");

                    putSharedPreferences(Constants.KEY_FXS_LEVEL, Data.getString("GDSType_Level"));

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
