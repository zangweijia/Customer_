package com.bopinjia.customer.activity;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.net.XutilsHttp;
import com.bopinjia.customer.net.XutilsHttp.XCallBack;
import com.bopinjia.customer.qrcode.CaptureActivity;
import com.bopinjia.customer.qrcode.camera.PlanarYUVLuminanceSource;
import com.bopinjia.customer.qrcode.decoding.RGBLuminanceSource;
import com.bopinjia.customer.qrcode.decoding.Utils;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.util.SecurityUtil;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ActivityShopList extends BaseActivity implements BDLocationListener {

    /**
     * 定位用客户端
     */
    private LocationClient mLocationClient;
    private BaiduMap mBaiduMap;
    /**
     * 地图控件
     */
    private MapView mMvShops;
    boolean isFirstLoc = true;// 是否首次定位

    /**
     * 判断从哪个界面进入的 1 启动界面 2 我的界面
     */
    private int type;

    private Dialog mDialog;
    private View dialogView;
    private Button confirmBt;
    private Button cancelBt;
    private String shopMobile;
    private String userId;
    String mlatitude;
    String mlongitude;

    private String photo_path;
    private Bitmap scanBitmap;
    EditText etCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(this.getApplication());
        setContentView(R.layout.wj_activity_shop_list);

        type = getIntent().getIntExtra("type", 1);

        findViewById(R.id.btn_return).setOnClickListener(this);
        findViewById(R.id.tv_phone).setOnClickListener(this);
        findViewById(R.id.ll_shop_enter).setOnClickListener(this);
        findViewById(R.id.tv_sub).setOnClickListener(this);

        findViewById(R.id.tv_photo).setOnClickListener(this);
        findViewById(R.id.iv_scan_code).setOnClickListener(this);

        etCode = (EditText) findViewById(R.id.et_code);
        etCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    etCode.setHint("我有邀请码");
                } else {
                    etCode.setHint("");
                }
            }
        });

        // ------------------------------
        mDialog = new Dialog(this, R.style.CustomDialogTheme);
        dialogView = LayoutInflater.from(this).inflate(R.layout.send_tel_dailog, null);
        // 设置自定义的dialog布局
        mDialog.setContentView(dialogView);
        // false表示点击对话框以外的区域对话框不消失，true则相反
        mDialog.setCanceledOnTouchOutside(false);
        confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
        cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);
        // ------------------------------

        // 地图控件
        mMvShops = (MapView) findViewById(R.id.mv_shops);
        mMvShops.showZoomControls(false);
        mMvShops.showScaleControl(false);

        initMapView();
    }

    /**
     * 画面控件点击回调函数
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        switch (viewId) {
            case R.id.btn_return:
                if (type == 1) {
                    backward();
                } else if (type == 2) {
                    finish();
                }
                break;
            case R.id.tv_phone:
                mDialog.show();
                // 获取自定义dialog布局控件
                ((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("是否拨打电话联系店铺?");
                // 确定按钮点击事件
                confirmBt.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + shopMobile));
                        startActivity(intent);
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
                break;

            case R.id.ll_shop_enter:
                putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_GDSUSERID, "0");
                putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_SHOP, userId);
                if (type == 1) {
                } else if (type == 2) {
                    ActivityHome.instance.finish();
                }
                forward(ActivityHome.class);
                showToast("成功进入店铺");
                finish();
                break;

            case R.id.tv_sub:
                String mCode = ((EditText) findViewById(R.id.et_code)).getText().toString().trim();
                useCode(mCode);
                break;
            case R.id.iv_scan_code:
                Intent toScan = new Intent(this, CaptureActivity.class);
                startActivityForResult(toScan, 1);
                break;

            case R.id.tv_photo:
                photo();
                break;
            default:
                break;
        }
    }

    private void photo() {
        // 激活系统图库，选择一张图片
        Intent innerIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");

        startActivityForResult(wrapperIntent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == 1) {
                String scanResult = SecurityUtil.decode(data.getStringExtra(Constants.INTENT_EXTRA_SCAN_RESULT));

                useCode(getCode(scanResult));
            } else if (requestCode == 2) {

                String[] proj = {MediaStore.Images.Media.DATA};
                // 获取选中图片的路径
                Cursor cursor = getContentResolver().query(data.getData(), proj, null, null, null);

                if (cursor.moveToFirst()) {

                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    photo_path = cursor.getString(column_index);
                    if (photo_path == null) {
                        photo_path = Utils.getPath(getApplicationContext(), data.getData());
                        Log.i("123path  Utils", photo_path);
                    }
                    Log.i("123path", photo_path);

                }

                cursor.close();

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        Result result = scanningImage(photo_path);
                        if (result == null) {
                            Looper.prepare();
                            showToast("图片格式有误");
                            Looper.loop();
                        } else {
                            // 扫描结果
                            String recode = recode(result.toString());
                            useCode(getCode(recode));
                        }
                    }
                }).start();
            }

        }
    }


    /**
     * 解析网址 取得 MDGDSM_Number
     *
     * @param str
     * @return
     */
    private String getCode(String str) {
        Uri uri = Uri.parse(str);
        String code = uri.getQueryParameter("MDGDSM_Number");
        return code;
    }


    /**
     * 设置地图控件
     */
    private void initMapView() {
        // 地图属性设置
        mBaiduMap = mMvShops.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 缩放比例
        MapStatusUpdate status = MapStatusUpdateFactory.zoomTo(13);
        mBaiduMap.setMapStatus(status);
        // mBaiduMap.getUiSettings().setAllGesturesEnabled(false);
        try {
            // 定位初始化
            mLocationClient = new LocationClient(getApplicationContext());

            // 设定获取定位信息后的回调方法
            mLocationClient.registerLocationListener(this);

            // 客户端设定
            LocationClientOption option = new LocationClientOption();
            option.setOpenGps(true); // 打开gps
            option.setLocationMode(LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll"); // 设置坐标类型
            option.setLocationNotify(true);
            option.setIgnoreKillProcess(true);

            mLocationClient.setLocOption(option);

        } catch (Exception e) {

        }

    }

    @Override
    protected void onStart() {
        mLocationClient.start();
        super.onStart();
    }

    @Override
    protected void onPause() {
        mMvShops.onPause();
        mLocationClient.stop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMvShops.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出时销毁定位
        mLocationClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMvShops.onDestroy();
        mMvShops = null;
        super.onDestroy();
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        // map view 销毁后不在处理新接收的位置
        if (location == null || mMvShops == null) {
            return;
        }

        // 此处设置开发者获取到的方向信息，顺时针0-360
        MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius()).direction(100)
                .latitude(location.getLatitude()).longitude(location.getLongitude()).build();
        mBaiduMap.setMyLocationData(locData);
        if (isFirstLoc) {
            isFirstLoc = false;
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);

            mBaiduMap.animateMapStatus(u);

            DecimalFormat df = new DecimalFormat("##0.000000");
            String latitude = df.format(location.getLatitude());
            String longitude = df.format(location.getLongitude());

            // test();

            isHasStore(longitude, latitude);
        }

    }

    @Override
    public void onConnectHotSpotMessage(String s, int i) {

    }

    /**
     * 周边是否有店铺
     *
     * @param longitude
     * @param latitude
     */
    private void isHasStore(String longitude, String latitude) {
        mlatitude = latitude;
        mlongitude = longitude;
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("Longitude", longitude);
        map.put("Latitude", latitude);
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

        String url = Constants.WEBAPI_ADDRESS + "api/Store/List?Longitude=" + longitude + "&Latitude=" + latitude
                + "&Sign=" + Sign + "&Ts=" + Ts;

        XutilsHttp.getInstance().get(url, null, new isHasStoreCallBack(), this);
    }

    class isHasStoreCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {

                    if (mlongitude.equals("0.000000") && mlatitude.equals("0.000000")) {
                        findViewById(R.id.ll_shop_enter).setVisibility(View.GONE);
                        findViewById(R.id.ll_code_enter).setVisibility(View.VISIBLE);
                    } else {
                        // 有店铺
                        findViewById(R.id.ll_shop_enter).setVisibility(View.VISIBLE);
                        findViewById(R.id.ll_code_enter).setVisibility(View.GONE);
                        parse(jo);
                    }

                } else if (jsonresult.equals("2")) {
                    findViewById(R.id.ll_shop_enter).setVisibility(View.GONE);
                    findViewById(R.id.ll_code_enter).setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void parse(JSONObject jo) {
        JSONArray shopArray;
        try {
            shopArray = jo.getJSONArray("Data");

            JSONObject jsonData = shopArray.getJSONObject(0);

            ((TextView) findViewById(R.id.tv_shop_name)).setText(jsonData.getString("ShopName"));
            ((TextView) findViewById(R.id.txt_shop_location))
                    .setText("距您当前位置： " + Integer.parseInt(jsonData.getString("Distance")) / 1000 + "公里");
            ((TextView) findViewById(R.id.tv_introduce)).setText(jsonData.getString("PrimaryBusiness"));

            ImageView iv = (ImageView) findViewById(R.id.iv_shop_head);
            ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .setCircular(true).setCrop(true).build();
            x.image().bind(iv, jsonData.getString("ShopThumbnail"), imageOptions);

            userId = jsonData.getString("UserId");

            // 店铺电话
            shopMobile = jsonData.getString("ShopMobile");
            ((TextView) findViewById(R.id.tv_phone)).setText("联系电话:" + jsonData.getString("ShopMobile"));

            double latitude = Double.parseDouble(jsonData.getString("Latitude"));
            double longitude = Double.parseDouble(jsonData.getString("Longitude"));
            LatLng local = new LatLng(latitude, longitude);
            MarkerOptions localOptions = new MarkerOptions().position(local)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_local));
            mBaiduMap.addOverlay(localOptions);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (type == 1) {
                backward();
            } else if (type == 2) {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        return localIntent;
    }

    private void useCode(String code) {
        String Ts = MD5.getTimeStamp();
        Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
            public int compare(String obj1, String obj2) {
                return obj1.compareTo(obj2);
            }
        });
        map.put("GDSUser_Num", code);
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

        String url = Constants.WEBAPI_ADDRESS + "api/Store/GetGDSUser_Num?GDSUser_Num=" + code + "&Sign=" + Sign
                + "&Ts=" + Ts;
        XutilsHttp.getInstance().get(url, null, new useCodeCallBack(), this);
    }

    class useCodeCallBack implements XCallBack {

        @Override
        public void onResponse(String result) {
            try {
                JSONObject jo = new JSONObject(result);
                String jsonresult = jo.getString("Result");
                if (jsonresult.equals("1")) {
                    JSONObject data = jo.getJSONObject("Data");
                    String mGDSUserId = data.getString("GDSUserId");
                    putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_SHOP, data.getString("MDUserId"));

                    if (mGDSUserId == null) {
                        putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_GDSUSERID, "");
                    } else {
                        putSharedPreferences(Constants.KEY_PREFERENCE_BINDING_GDSUSERID, mGDSUserId);
                    }

                    if (type == 1) {
                    } else if (type == 2) {
                        ActivityHome.instance.finish();
                    }
                    forward(ActivityHome.class);
                    showToast("成功进入店铺");
                    finish();

                } else {
                    showToast("邀请码不正确");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 中文乱码
     * <p>
     * 暂时解决大部分的中文乱码 但是还有部分的乱码无法解决 .
     *
     * @return
     */
    private String recode(String str) {
        String formart = "";

        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
                Log.i("1234      ISO8859-1", formart);
            } else {
                formart = str;
                Log.i("1234      stringExtra", str);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formart;
    }

    /**
     * //TODO: TAOTAO 将bitmap由RGB转换为YUV //TOOD: 研究中
     *
     * @param bitmap 转换的图形
     * @return YUV数据
     */
    public byte[] rgb2YUV(Bitmap bitmap) {
        // 该方法来自QQ空间
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int len = width * height;
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[i * width + j] & 0x00FFFFFF;

                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;

                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

                y = y < 16 ? 16 : (y > 255 ? 255 : y);
                u = u < 0 ? 0 : (u > 255 ? 255 : u);
                v = v < 0 ? 0 : (v > 255 ? 255 : v);

                yuv[i * width + j] = (byte) y;
                // yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
                // yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }

    // TODO: 解析部分图片
    protected Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {

            return null;

        }
        // DecodeHintType 和EncodeHintType
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小

        int sampleSize = (int) (options.outHeight / (float) 200);

        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);

        // --------------测试的解析方法---PlanarYUVLuminanceSource-这几行代码对project没作功----------

        LuminanceSource source1 = new PlanarYUVLuminanceSource(rgb2YUV(scanBitmap), scanBitmap.getWidth(),
                scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source1));
        MultiFormatReader reader1 = new MultiFormatReader();
        Result result1;
        try {
            result1 = reader1.decode(binaryBitmap);
            String content = result1.getText();
            Log.i("123content", content);
        } catch (NotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // ----------------------------

        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {

            return reader.decode(bitmap1, hints);

        } catch (NotFoundException e) {

            e.printStackTrace();

        } catch (ChecksumException e) {

            e.printStackTrace();

        } catch (FormatException e) {

            e.printStackTrace();

        }

        return null;

    }

}
