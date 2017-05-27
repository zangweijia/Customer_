package com.bopinjia.customer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.bean.LoginBean;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.StringUtils;
import com.google.gson.Gson;
import com.umeng.message.PushAgent;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityLogin extends BaseActivity {

    @ViewInject(R.id.tv_back)
    private TextView mBack;

    @ViewInject(R.id._tv_register)
    private TextView mRegister;

    @ViewInject(R.id.et_phone)
    private EditText mEtPhone;

    @ViewInject(R.id.et_password)
    private EditText mEtPassword;

    @ViewInject(R.id.tv_forget_password)
    private TextView mForget;

    @ViewInject(R.id.tvbtn_login)
    private TextView mLogin;

    @ViewInject(R.id.iv_password_isshow)
    private ImageView mYan;

    @ViewInject(R.id.iv_delete_phone)
    private ImageView mDeletePhone;

    @ViewInject(R.id.xl)
    private ImageView mXinLang;
    @ViewInject(R.id.wx)
    private ImageView mWX;
    @ViewInject(R.id.qq)
    private ImageView mQQ;

    private String phone;
    private String pass;
    private boolean isHidden = true;

    private int mLoginType;
    public static final int LOGIN_TYPE_XL = 3;
    public static final int LOGIN_TYPE_WX = 1;
    public static final int LOGIN_TYPE_QQ = 2;

    public static ActivityLogin instance = null;

    private UMShareAPI mShareAPI;
    private String uid = null;
    private Map<String, String> myData = new HashMap<String, String>();
    private PushAgent mPushAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_activity_login);
        x.view().inject(this);
        instance = this;
        mPushAgent = PushAgent.getInstance(this);
        mPushAgent.onAppStart();
        mShareAPI = UMShareAPI.get(ActivityLogin.this);

        // UMShareConfig config = new UMShareConfig();
        // config.isNeedAuthOnGetUserInfo(true);
        // mShareAPI.setShareConfig(config);

        mEtPhone.addTextChangedListener(myPhoneTextWatcher);
    }

    /**
     * 监听mEtCode 如果有输入显示，否则隐藏
     */
    TextWatcher myPhoneTextWatcher = new TextWatcher() {
        String s;

        @Override
        public void onTextChanged(CharSequence text, int start, int before, int count) {
            s = text.toString().trim();
        }

        @Override
        public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable edit) {
            if (s.length() > 0) {
                mDeletePhone.setVisibility(View.VISIBLE);
            } else {
                mDeletePhone.setVisibility(View.GONE);
            }
            if (s.length() == 11) {
                mEtPassword.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mEtPassword, InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
    };

    @Event(value = {R.id.tv_back, R.id.tvbtn_login, R.id.tv_forget_password, R.id.duanxinlogin, R.id.xl, R.id.wx,
            R.id.qq, R.id.iv_password_isshow, R.id._tv_register, R.id.iv_delete_phone})
    private void getEvent(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                backward();
                break;
            case R.id.tvbtn_login:
                phone = mEtPhone.getText().toString();
                pass = MD5.Md5(mEtPassword.getText().toString());

                if (StringUtils.isNull(phone)) {
                    showToast(MessageFormat.format(getString(R.string.msg_err_empty), "手机号"));
                    break;
                }

                if (StringUtils.isInteger(phone)) {
                    showToast(MessageFormat.format(getString(R.string.msg_err_not_exsist), "手机号"));
                    break;
                }
                if (StringUtils.isNull(pass)) {
                    showToast(MessageFormat.format(getString(R.string.msg_err_empty), "密码"));
                    break;
                }
                Login();
                break;
            case R.id.tv_forget_password:
                // 忘记密码
                forward(ActivityForGetPass.class);
                break;
            case R.id.duanxinlogin:
                // 短信登录
                forward(ActivityDuanxinLogin.class);
                break;
            case R.id.xl:
                mShareAPI.getPlatformInfo(ActivityLogin.this, SHARE_MEDIA.SINA,
                        umListener);
                mLoginType = LOGIN_TYPE_XL;
                break;
            case R.id.wx:
                UMShareAPI.get(ActivityLogin.this).getPlatformInfo(ActivityLogin.this,
                        SHARE_MEDIA.WEIXIN, umListener);
                mLoginType = LOGIN_TYPE_WX;
                break;
            case R.id.qq:
                mShareAPI.getPlatformInfo(ActivityLogin.this, SHARE_MEDIA.QQ, umListener);
                mLoginType = LOGIN_TYPE_QQ;
                break;
            case R.id.iv_password_isshow:
                mYan.setSelected(!mYan.isSelected());
                if (isHidden) {
                    // 设置EditText文本为可见的
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    // 设置EditText文本为隐藏的
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                isHidden = !isHidden;
                mEtPassword.postInvalidate();
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = mEtPassword.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
                break;
            case R.id._tv_register:
                forward(ActivityRegister.class);
                break;
            case R.id.iv_delete_phone:
                mEtPhone.setText("");
                mEtPassword.setText("");
                break;
            default:
                break;
        }

    }

    /**
     * 获取信息
     */
    UMAuthListener umListener = new UMAuthListener() {

        @Override
        public void onCancel(SHARE_MEDIA arg0, int arg1) {

        }

        @Override
        public void onComplete(SHARE_MEDIA arg0, int arg1, Map<String, String> data) {
            myData = data;
            BindLogin(mLoginType);
        }

        @Override
        public void onError(SHARE_MEDIA arg0, int arg1, Throwable arg2) {
            showToast("error" + arg2.getMessage());

        }

        @Override
        public void onStart(SHARE_MEDIA arg0) {

        }

    };
    private String UserUnionID;

    private void BindLogin(int mtype) {

        String type = mtype + "";

        if (mLoginType == LOGIN_TYPE_XL) {
            uid = myData.get("uid");
            UserUnionID = uid;
        } else if (mLoginType == LOGIN_TYPE_WX) {
            uid = myData.get("uid");
            UserUnionID = uid;
        } else if (mLoginType == LOGIN_TYPE_QQ) {
            uid = myData.get("uid");
            UserUnionID = myData.get("unionid");
        }

        String Ts = MD5.getTimeStamp();

        String uuid = getIMEI();

        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });

        map.put("UserType", type);
        map.put("UserBindingAccount", uid);

        map.put("UserUnionID", UserUnionID);
        map.put("UUID", uuid);
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

        String url = Constants.WEBAPI_ADDRESS + "api/UserBinding/Quicklogin_New?UserType=" + type + "&UserBindingAccount="
                + uid + "&UserUnionID=" + UserUnionID + "&UUID=" + uuid + "&Sign=" + Sign + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new BindLoginCallBack(), this);

    }

    class BindLoginCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {

                    Gson gson = new Gson();
                    LoginBean mLogBean = new LoginBean();
                    mLogBean = gson.fromJson(result, LoginBean.class);
                    putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "1");
                    String userId = mLogBean.getData().getUserId().toString();
                    putSharedPreferences(Constants.KEY_PREFERENCE_USER_ID, userId);
                    putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, mLogBean.getData().getRegisterPhone());
                    putSharedPreferences(Constants.KEY_PREFERENCE_USER_INFO, result);
                    finish();

                } else {
                    Intent i = new Intent();
                    i.putExtra("url", myData.get("iconurl"));
                    i.putExtra("name", myData.get("name"));
                    i.putExtra("uid", uid);
                    if (mLoginType == LOGIN_TYPE_XL) {
                        i.putExtra("type", "xl");
                        i.putExtra("unionid", uid);
                    } else if (mLoginType == LOGIN_TYPE_WX) {
                        i.putExtra("type", "wx");
                        i.putExtra("unionid", uid);

                    } else if (mLoginType == LOGIN_TYPE_QQ) {
                        i.putExtra("type", "qq");
                        i.putExtra("unionid", UserUnionID);
                    }
                    forward(ActivityOtherLogin.class, i);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 登录
     */
    public void Login() {
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        String Ts = MD5.getTimeStamp();
        map.put("RegisterPhone", phone);
        map.put("Password", pass);
        map.put("DeviceType", "0");
        map.put("DeviceToken", "0");
        map.put("UUID", getIMEI());
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

        maps.put("RegisterPhone", phone);
        maps.put("Password", pass);
        maps.put("DeviceType", "0");
        maps.put("DeviceToken", "0");
        maps.put("UUID", getIMEI());
        maps.put("Sign", Sign);
        maps.put("Ts", Ts);

        XutilsHttp.getInstance().post(Constants.WEBAPI_ADDRESS + "api/login", maps, new LoginCallBack(), this);

    }

    class LoginCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    parse(result);
                } else if (jsonresult.equals("5")) {
                    showToast("用户不存在");
                } else if (jsonresult.equals("4")) {
                    showToast("密码不正确");
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    public void parse(String str) {
        Gson gson = new Gson();
        LoginBean mLogBean = new LoginBean();
        mLogBean = gson.fromJson(str, LoginBean.class);

        putSharedPreferences(Constants.KEY_PREFERENCE_LOGIN_FLG, "1");

        String userId = mLogBean.getData().getUserId().toString();
        putSharedPreferences(Constants.KEY_PREFERENCE_USER_ID, userId);

//		mPushAgent.addAlias(userId,  ALIAS_TYPE.SINA_WEIBO, new UTrack.ICallBack() {
//			@Override
//			public void onMessage(boolean isSuccess, String message) {
//
//			}
//		});
        putSharedPreferences(Constants.KEY_PREFERENCE_PHONE, mLogBean.getData().getRegisterPhone());
        putSharedPreferences(Constants.KEY_PREFERENCE_PASSWORD, MD5.Md5(mEtPassword.getText().toString()));
        putSharedPreferences(Constants.KEY_PREFERENCE_USER_INFO, str);

        backward();

    }

    @Override
    protected void onDestroy() {
        setContentView(R.layout.null_view);
        super.onDestroy();
        UMShareAPI.get(this).release();
    }
}
