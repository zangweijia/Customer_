package com.bopinjia.customer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.util.CreatQRCode;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.NetUtils;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ActivityFXQRcode extends BaseActivity {

    @ViewInject(R.id.iv_qrcode)
    private ImageView iv;

    @ViewInject(R.id.iv_wx)
    private ImageView mIVwx;

    @ViewInject(R.id.iv_downlode)
    private ImageView mIVDownLode;

    @ViewInject(R.id.iv_wxcircle)
    private ImageView mIVwxCircle;

    @ViewInject(R.id.scrollview)
    private ScrollView mScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wj_act_qr_code);
        x.view().inject(this);
        UMShareAPI.get(ActivityFXQRcode.this);

        setTitle();
        getDistributionInfo();
        mIVwx.setOnClickListener(this);
        mIVDownLode.setOnClickListener(this);
        mIVwxCircle.setOnClickListener(this);
        view = (LinearLayout) findViewById(R.id.ll_all);

//		http://wap.bopinwang.com?storeUserIdC=%@&shareuserIDC=%@MDGDSM_Number=%@
        String code = "http://wap.bopinwang.com?storeUserIdC=" + getBindingShop()
                + "&shareuserIDC=" + getLoginUserId()
                + "&MDGDSM_Number=" + getBopinjiaSharedPreference(Constants.KEY_FXS_NUMBER);

        iv.setImageBitmap(CreatQRCode.createQRImage(code, 200, 200));

    }

    /**
     * Activity screenCap
     *
     * @param activity
     * @return
     */
    public Bitmap activityShot(Activity activity) {

        int width = view.getWidth();
        int height = view.getHeight();
        Bitmap Bmp = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        // 2.获取屏幕
        View decorview = view;
        decorview.setDrawingCacheEnabled(true);

        Bmp = decorview.getDrawingCache();
        return Bmp;
    }

    private void setTitle() {
        View mTiTle = findViewById(R.id.include_title);
        TextView mTiTleBack = (TextView) mTiTle.findViewById(R.id.btn_return);
        mTiTleBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView mTiTleName = (TextView) mTiTle.findViewById(R.id.txt_page_title);
        mTiTleName.setText("店铺二维码");

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_wx:
                // 微信分享
                Bitmap bm = activityShot(this);
                if (bm == null) {
                    showToast("分享失败");
                } else {
                    Share(bm, 1);
                }
                break;
            case R.id.iv_downlode:
                // 下载
                saveImageToGallery(this, activityShot(this));
                showToast("已成功保存到本地");
                break;

            case R.id.iv_wxcircle:
                // 朋友圈分享
                Bitmap bm2 = activityShot(this);
                if (bm2 == null) {
                    showToast("分享失败");
                } else {

                    Share(bm2, 2);
                }
                break;

            default:
                break;
        }
    }

    public void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "Bpj");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 分享
     *
     * @param bm 分享出去bitmap
     * @param i  当i=1 微信分享 i=2 朋友圈分享
     */
    public void Share(Bitmap bm, int i) {
        // 对bitmap进行压缩
        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        // 友盟 生成分享对象
        UMImage thumb = new UMImage(this, bm);

        if (i == 1) {
            new ShareAction(ActivityFXQRcode.this).withMedia(thumb).setCallback(umShareListener)
                    .setDisplayList(SHARE_MEDIA.WEIXIN).open();
        } else if (i == 2) {
            new ShareAction(ActivityFXQRcode.this).withMedia(thumb).setCallback(umShareListener)
                    .setDisplayList(SHARE_MEDIA.WEIXIN_CIRCLE).open();
        }

    }

    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onResult(SHARE_MEDIA platform) {
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
        }

        @Override
        public void onStart(SHARE_MEDIA arg0) {
            // TODO Auto-generated method stub

        }

    };

    private LinearLayout view;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(ActivityFXQRcode.this).onActivityResult(requestCode, resultCode, data);

    }

    private void getDistributionInfo() {
        String s = getLoginUserId();
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new HashMap<String, String>();
        map.put("UserId", s);
        map.put("Key", Constants.WEBAPI_KEY);
        map.put("Ts", Ts);

        String url = Constants.WEBAPI_ADDRESS + "api/GDSUser/GetGDSUserInfo?UserId=" + s + "&Sign="
                + NetUtils.getSign(map) + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new CallBack(), this);
    }

    class CallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject Data = jo.getJSONObject("Data");

                    ((TextView) findViewById(R.id.tv_shop_name)).setText(Data.getString("MDGDSM_ShopName"));

                    ((TextView) findViewById(R.id.tv_shop_num)).setText("邀请码：" + Data.getString("MDGDSM_Number"));

                    ImageFromUrl(Data.getString("MDGDSM_ShopLogo"), R.id.iv_head);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
