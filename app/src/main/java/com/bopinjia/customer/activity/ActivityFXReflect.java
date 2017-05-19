package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ReflectAccountBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

public class ActivityFXReflect extends BaseActivity {

    private JSONArray jsonarray;

    private Boolean isShowAlipay = false;
    private Boolean isShowWX = false;

    private String alipayAccountId, WXAccountId;

    /**
     * 是否可提现
     */
    private Boolean isReflect = false;
    /**
     * 提现次数
     */
    private Boolean ReflectCount = false;

    private TextView mTiTleBack;
    private TextView mTiTleName, mTiTleReflect;
    /**
     * 绑定提现账号 按钮
     */
    @ViewInject(R.id.tv_bind_account)
    private TextView mBindAcount;
    /**
     * 申请提现按钮
     */
    @ViewInject(R.id.tv_tixian)
    private TextView mSQReflect;
    /**
     * 没有提现账号显示的控件
     */
    @ViewInject(R.id.ll_no_account)
    private LinearLayout mLLNoAccount;
    /**
     * 有提现账号显示的控件
     */
    @ViewInject(R.id.ll_pay)
    private LinearLayout mLLpay;
    /**
     * 微信
     */
    @ViewInject(R.id.ll_wx)
    private LinearLayout mLLWX;
    /**
     * 支付宝
     */
    @ViewInject(R.id.ll_alipay)
    private LinearLayout mLLAlipay;

    /**
     * 支付宝账号
     */
    @ViewInject(R.id.tv_alipay_number)
    private TextView mAlipayNum;

    /**
     * 微信账号
     */
    @ViewInject(R.id.tv_wx_number)
    private TextView mWXnum;

    /**
     * 可提现余额
     */
    @ViewInject(R.id.tv_reflect_account)
    private TextView mReflectAccount;

    /**
     * 余额
     */
    @ViewInject(R.id.tv_price)
    private TextView mTVprice;

    @ViewInject(R.id.chk_alipay)
    private RadioButton mChkAlipay;

    @ViewInject(R.id.chk_weixinpay)
    private RadioButton mChkWX;


    private List<RadioButton> radionButtonList = new ArrayList<RadioButton>();

    private String pricr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_act_reflect);
        x.view().inject(this);

        setTitle();
        init();
        getUserBillRecords();
    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);
        mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
        mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
        mTiTleReflect = (TextView) mTiTle.findViewById(R.id.btn_edit);

        mTiTleReflect.setText("提现记录");
        mTiTleName.setText("提现");
    }

    private void init() {
        radionButtonList.add(mChkAlipay);
        radionButtonList.add(mChkWX);
        pricr = getIntent().getStringExtra("reflectaccount");
        mReflectAccount.setText(pricr);
        mTVprice.setText(pricr);
        getAccountList();

    }

    @Event(value = {R.id.btn_return, R.id.btn_edit, R.id.tv_bind_account, R.id.chk_alipay,
            R.id.chk_weixinpay, R.id.tv_tixian})
    private void getEvent(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.btn_edit:
                // 提现记录
                forward(ActivityFXReflectList.class);
                break;
            case R.id.tv_bind_account:
                forward(ActivityFXBindCashAccount.class);
                break;
            case R.id.chk_alipay:
                // 支付宝chk
            case R.id.chk_weixinpay:
                // 微信 chk
                for (RadioButton button : radionButtonList) {
                    if (button.getId() != v.getId()) {
                        button.setChecked(false);
                    }
                }
                break;
            case R.id.tv_tixian:
                // 申请提现
                if (ReflectCount == true) {
                    if (isReflect == true) {
                        float fprice = Float.parseFloat(pricr);
                        int iii = (int) fprice;
                        if (iii > 10) {
                            String typeid = null;
                            String accountid = null;
                            if (mChkAlipay.isChecked()) {
                                typeid = "1";
                                accountid = alipayAccountId;
                            } else if (mChkWX.isChecked()) {
                                typeid = "2";
                                accountid = WXAccountId;
                            }
                            submitReflect(accountid, typeid, pricr);
                        } else {
                            showToast("金额必须大于10元");
                        }

                    } else {
                        showToast("每月的10号才能提现");
                    }
                } else {
                    showToast("您已申请提现，请耐心等待");
                }

                break;

            default:
                break;
        }
    }

    /**
     * 获取账号列表
     */
    private void getAccountList() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", getLoginUserId());
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

        String url = Constants.WEBAPI_ADDRESS + "api/UserAccount/List?UserId=" + getLoginUserId() + "&Sign=" + Sign
                + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new AccountListCallback(), this);
    }

    /**
     * 获取账号列表 回调
     *
     * @author ZWJ
     */
    class AccountListCallback implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    mLLNoAccount.setVisibility(View.GONE);
                    mLLpay.setVisibility(View.VISIBLE);
                    jsonarray = jo.getJSONArray("Data");
                    Gson gson = new Gson();

                    for (int i = 0; i < jsonarray.length(); i++) {
                        ReflectAccountBean mReflectBean = new ReflectAccountBean();
                        mReflectBean = gson.fromJson(jsonarray.get(i).toString(), ReflectAccountBean.class);

                        if (mReflectBean.getUserAccountTypeId().equals("1")) {
                            isShowAlipay = true;
                            mAlipayNum.setText(mReflectBean.getUserAccounts());
                            alipayAccountId = mReflectBean.getUserAccountId();
                        } else if (mReflectBean.getUserAccountTypeId().equals("2")) {
                            isShowWX = true;
                            mWXnum.setText(mReflectBean.getUserAccounts());
                            WXAccountId = mReflectBean.getUserAccountId();
                        }

                    }
                    if (isShowAlipay == true) {
                        mLLAlipay.setVisibility(View.VISIBLE);
                    } else {
                        mLLAlipay.setVisibility(View.GONE);
                    }

                    if (isShowWX == true) {
                        mLLWX.setVisibility(View.VISIBLE);
                    } else {
                        mLLWX.setVisibility(View.GONE);
                    }

                } else if (jsonresult.equals("2")) {
                    mLLNoAccount.setVisibility(View.VISIBLE);
                    mLLpay.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 获取提现次数和当前日期
     */
    private void getUserBillRecords() {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("UserId", getLoginUserId());
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

        String url = Constants.WEBAPI_ADDRESS + "api/UserBillRecords/BRVerification?UserId=" + getLoginUserId()
                + "&Sign=" + Sign + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new UserBillRecordsCallBack(), this);
    }

    /**
     * 获取提现次数和当前日期 回调
     *
     * @author ZWJ
     */
    class UserBillRecordsCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject date = jo.getJSONObject("Data");
                    String timeState = date.getString("TimeState");
                    String BRCount = date.getString("BRCount");
                    if (timeState.equals("0")) {
                        isReflect = false;
                    } else if (timeState.equals("1")) {
                        isReflect = true;
                    }
                    int brcount = Integer.parseInt(BRCount);
                    if (brcount > 0) {
                        ReflectCount = false;
                    } else {
                        ReflectCount = true;
                    }

                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 申请提现
     *
     * @param accountid
     * @param typeid
     * @param account
     */
    private void submitReflect(String accountid, String typeid, String account) {
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        String Ts = MD5.getTimeStamp();
        map.put("UserId", getLoginUserId());
        map.put("BR_Amount", account);
        map.put("UserAccountId", accountid);
        map.put("Br_TypeId", typeid);
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

        Map<String, String> maps = new HashMap<String, String>();
        maps.put("UserId", getLoginUserId());
        maps.put("BR_Amount", account);
        maps.put("UserAccountId", accountid);
        maps.put("Br_TypeId", typeid);
        maps.put("Sign", Sign);
        maps.put("Ts", Ts);
        XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/UserBillRecords/Add", maps,
                new submitReflectCallBack(), this);
    }

    /**
     * 申请提现 回调
     *
     * @author ZWJ
     */
    class submitReflectCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    // 成功
                    //	forward(ActivityReflectSuccess.class);

                    Intent toScan = new Intent(ActivityFXReflect.this, ActivityReflectSuccess.class);
                    startActivityForResult(toScan, 1);


                } else {
                    // 失败
                    showToast("申请提现失败");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mReflectAccount.setText("0.00");
        mTVprice.setText("0.00");
        getUserBillRecords();
    }

}
