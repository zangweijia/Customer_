package com.bopinjia.customer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.ReflectAccountBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.google.gson.Gson;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityFXBindCashAccount extends BaseActivity {

    private TextView mTiTleBack;
    private TextView mTiTleName, mTiTleReflect;

    /**
     * 支付宝账号
     */
    @ViewInject(R.id.tv_alipay_number)
    private TextView mTVAlipayNum;
    /**
     * 微信账号
     */
    @ViewInject(R.id.tv_wx_number)
    private TextView mTVWXNum;


    /**
     * 支付宝按钮
     */
    @ViewInject(R.id.rl_alipay)
    private RelativeLayout mBtnalipay;
    /**
     * 微信按钮
     */
    @ViewInject(R.id.ll_wx)
    private LinearLayout mBtnWX;

    private int mLoginType;
    private UMShareAPI mShareAPI;
    public static final int LOGIN_TYPE_XL = 3;
    public static final int LOGIN_TYPE_WX = 1;
    public static final int LOGIN_TYPE_QQ = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_act_fxbind_cash_account);
        x.view().inject(this);
        mShareAPI = UMShareAPI.get(ActivityFXBindCashAccount.this);
        setTitle();
        init();
    }

    private void init() {
        mBtnalipay.setOnClickListener(this);
        mBtnWX.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccountList();
    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);
        mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
        mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);

        mTiTleName.setText("提现账号");
        mTiTleBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                finish();
                break;
            case R.id.rl_alipay:
//                i.putExtra("type", "1");
//                i.putExtra("json", mjsonarray.toString());
//                forward(ActivityFXSubmitAccount.class, i);
                break;
            case R.id.ll_wx:
                mShareAPI.getPlatformInfo(ActivityFXBindCashAccount.this, SHARE_MEDIA.WEIXIN, umListener);
                break;
            default:
                break;
        }
    }

    /**
     * 获取信息
     */
    private UMAuthListener umListener = new UMAuthListener() {

        @Override
        public void onCancel(SHARE_MEDIA arg0, int arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onComplete(SHARE_MEDIA arg0, int arg1, Map<String, String> data) {
            // BindAccount
            //BindAccount("1", data.get("unionid"));绑定微信账号
            showToast(data.get("unionid"));
        }

        @Override
        public void onError(SHARE_MEDIA arg0, int arg1, Throwable arg2) {
        }

        @Override
        public void onStart(SHARE_MEDIA arg0) {
        }

    };

    private String mjsonarray;

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

        XutilsHttp.getInstance().get(url, null, new getAccountListCallBack(), this);
    }

    class getAccountListCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                mjsonarray = result;
                if (jsonresult.equals("1")) {
                    JSONArray jsonarray = jo.getJSONArray("Data");
                    Gson gson = new Gson();
                    for (int i = 0; i < jsonarray.length(); i++) {
                        ReflectAccountBean mReflectBean = new ReflectAccountBean();
                        mReflectBean = gson.fromJson(jsonarray.get(i).toString(), ReflectAccountBean.class);
                        if (mReflectBean.getUserAccountTypeId().equals("1")) {
                            mTVAlipayNum.setText(mReflectBean.getUserAccounts());
                        } else if (mReflectBean.getUserAccountTypeId().equals("2")) {
                            mTVWXNum.setText(mReflectBean.getUserAccounts());
                        }
                    }
                } else {

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

    }
}
