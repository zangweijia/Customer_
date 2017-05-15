package com.bopinjia.customer.activity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;
import org.xutils.x;
import org.xutils.image.ImageOptions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.util.ScreenManager;
import com.bopinjia.customer.util.StatusBarUtils;
import com.bopinjia.customer.util.StorageUtil;
import com.bopinjia.customer.util.StringUtils;
import com.bopinjia.customer.util.ToastUtils;
import com.bopinjia.customer.view.BadgeView;

/**
 * 页面基类
 * 
 * @author yushen 2015/12/23
 */
public class BaseActivity extends AppCompatActivity implements OnClickListener {

	/** 加载更多 */
	protected static final String FOOTER_STATUS_LOAD_MORE = "0";
	/** 加载中 */
	protected static final String FOOTER_STATUS_LOADING = "1";
	/** 没有更多了 */
	protected static final String FOOTER_STATUS_NO_MORE = "2";

	// /** 加载更多控件 */
	// protected LinearLayout mLoadMoreFooter;

	/** 页面管理工具类 */
	protected ScreenManager mScreenManager;
	private Toast mToast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mScreenManager = ScreenManager.getScreenManager();
		mScreenManager.pushActivity(this);

		// 锁竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
	}

	/**
	 * 页面跳转
	 * 
	 * @param cls
	 *            目标画面
	 */
	public <T> void forward(Class<T> cls) {
		forward(cls, new Intent());
	}

	/**
	 * 页面跳转
	 * 
	 * @param cls
	 *            目标画面
	 * @param intent
	 *            数据传递
	 */
	public <T> void forward(Class<T> cls, Intent intent) {
		intent.setClass(BaseActivity.this, cls);
		startActivity(intent);
	}

	/**
	 * 页面后退
	 */
	public void backward() {
		ScreenManager.getScreenManager().popActivity(this);
	}

	/**
	 * 返回到我的界面
	 */
	public void backmy() {
		Intent i = new Intent();
		i.putExtra("tabId", 5);
		forward(ActivityHome.class, i);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	/**
	 * 返回首页
	 */
	public void backHome(int tabid) {
		Intent i = new Intent();
		i.putExtra("tabId", tabid);
		forward(ActivityHome.class, i);
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		finish();
	}

	/**
	 * 当前页销毁
	 */
	public void finishCurrent() {
		ScreenManager.getScreenManager().popActivity(this);
	}

	/**
	 * 画面控件点击事件
	 */
	@Override
	public void onClick(View v) {
	}

	/**
	 * TOAST表示
	 * 
	 * @param message
	 *            消息
	 */
	public void showToast(String message) {

		// mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		// mToast.show();

		ToastUtils.showNOrmalToast(this, message);
	}

	/**
	 * 系统异常时的提醒
	 */
	public void showSysErr(Exception e) {

		if (Constants.IS_DEBUG) {
			StorageUtil.saveErrFile(this, e);
		}

		// showToast(getString(R.string.msg_err_system));
	}

	/**
	 * 系统异常时的提醒
	 */
	public void showErr() {
		showToast("亲，网络连接失败，请检查网络~");
	}

	/**
	 * 写调试信息
	 */
	public void debug(String msg) {
		if (Constants.IS_DEBUG) {
			StorageUtil.saveDebugMsg(this, msg);
		}
	}

	/**
	 * 按键事件响应
	 * 
	 * @param keyCode
	 *            按键代码 [KEYCODE_BACK:后退]
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backward();
			return false;
		}
		return false;
	}

	/**
	 * 关闭之前的所有页面
	 */
	protected void exitAllPreActivities(Class<Activity> cls) {
		ScreenManager.getScreenManager().popAllActivityExceptOne(cls);
	}

	/**
	 * 获取当前登录的用户ID
	 * 
	 * @return 用户ID
	 */
	public String getLoginUserId() {
		return getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_USER_ID);
	}

	/**
	 * 获取当前登录的用户头像地址
	 * 
	 * @return 用户头像地址
	 */
	public String getLoginHeadportrait() {
		return getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_HEADPORTRAIT);
	}

	/**
	 * 获取当前登录的用户注册手机号
	 * 
	 * @return 注册手机号
	 */
	public String getLoginPhone() {
		return getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_PHONE);
	}

	/**
	 * 获取当前登录的用户上次登录的密码
	 * 
	 * @return 上次登录的密码
	 */
	public String getPassword() {
		return getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_PASSWORD);
	}

	/**
	 * 获取当前登录用户绑定的店铺
	 * 
	 * @return 用户绑定的店铺
	 */
	public String getBindingShop() {
		return getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_BINDING_SHOP);
	}

	/**
	 * 获取上次浏览过的店铺
	 * 
	 * @return 上次浏览过的店铺
	 */
	public String getLastMerchantId() {
		String merchantId = getBindingShop();
		if (StringUtils.isNull(merchantId)) {
			merchantId = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_TEMP_SHOP);
			if (StringUtils.isNull(merchantId)) {
				merchantId = Constants.BOPINJIA_DEFAULT_ID;
			}
		}
		return merchantId;
	}

	/**
	 * 获取当前登录用户最近的搜索内容
	 * 
	 * @return 最近的搜索内容
	 */
	public List<String> getLatestSearch() {
		String latestSearch = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_LATEST_SEARCH);

		if (!StringUtils.isNull(latestSearch)) {
			String[] searches = latestSearch.split(",");
			ArrayList<String> searchList = new ArrayList<String>();

			for (String search : searches) {
				searchList.add(search);
			}

			return searchList;
		}
		return null;
	}

	/**
	 * 记录最近搜索内容
	 * 
	 * @param searchWord
	 */
	public void addLatestSearch(String searchWord) {
		String latestSearch = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_LATEST_SEARCH);

		if (!StringUtils.isNull(latestSearch)) {
			if (!latestSearch.equals(searchWord)) {
				if (latestSearch.indexOf(searchWord) != -1) {
					latestSearch = latestSearch.replace(searchWord + ",", "");
				}

				latestSearch = searchWord + "," + latestSearch;
			}
		} else {
			latestSearch = searchWord;
		}

		Editor shared = getSharedPreferences(Constants.KEY_PREFERENCE, 0).edit();
		shared.putString(Constants.KEY_PREFERENCE_LATEST_SEARCH, latestSearch);
		shared.commit();
	}

	/**
	 * 判断是否已经登录
	 * 
	 * @return
	 */
	public boolean isLogged() {

		String loginFlg = getBopinjiaSharedPreference(Constants.KEY_PREFERENCE_LOGIN_FLG);
		if (!StringUtils.isNull(loginFlg)) {
			if ("1".equals(loginFlg)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 设置首选项
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public void putSharedPreferences(String key, String value) {
		Editor shared = getSharedPreferences(Constants.KEY_PREFERENCE, 0).edit();
		shared.putString(key, value);
		shared.commit();
	}

	/**
	 * 删除首选项
	 * 
	 * @param key
	 *            键
	 */
	public void removeSharedPreferences(String key) {
		Editor shared = getSharedPreferences(Constants.KEY_PREFERENCE, 0).edit();
		shared.remove(key);
		shared.commit();
	}

	/**
	 * 获取本应用保存的首选项
	 * 
	 * @param key
	 *            首选项KEY
	 * @return 首选项
	 */
	public String getBopinjiaSharedPreference(String key) {
		SharedPreferences sp = getSharedPreferences(Constants.KEY_PREFERENCE, 0);
		if (sp != null && sp.contains(key)) {
			return sp.getString(key, "");
		}

		return null;
	}

	/**
	 * 获取当前手机的IMEI
	 * 
	 * @return
	 */
	protected String getIMEI() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * 获取当前手机的IMEI
	 * 
	 * @return
	 */
	public String getIMEI1() {
		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * 日志
	 * 
	 * @param operationPageAddress
	 *            当前画面名
	 * @return 日志(JSON)
	 */
	protected String getLogInfo(String operationPageAddress) {
		JSONObject logInfo = new JSONObject();

		try {
			logInfo.put("UserId", getLoginUserId());
			logInfo.put("IpAddress", getIMEI());
			logInfo.put("OperationSource", "3");
			logInfo.put("OperationPageAddress", operationPageAddress);
		} catch (Exception e) {
			showSysErr(e);
		}

		return logInfo.toString();
	}

	/**
	 * 获取数字角标控件
	 * 
	 * @param id
	 * @return
	 */
	protected BadgeView getBadgeView(int id) {
		BadgeView badge = new BadgeView(this);
		badge.setBackground(5, getResources().getColor(R.color.main_color));
		badge.setTargetView(findViewById(id));
		badge.setBadgeGravity(Gravity.TOP | Gravity.RIGHT);
		badge.setBadgeMargin(0, 0, 10, 0);

		return badge;
	}

	/**
	 * 加载图片
	 * 
	 * @param url
	 *            图片的地址
	 * @param id
	 *            图片控件的ID
	 */
	protected void setImageFromUrl(String url, int id) {
		ImageView iv = (ImageView) findViewById(id);
		setImageFromUrl(url, iv);
	}

	public void ImageFromUrl(String url, int id) {
		ImageView iv = (ImageView) findViewById(id);
		setImageFromUrl(url, iv);
	}

	/**
	 * 加载图片
	 * 
	 * @param viewId
	 * @param url
	 */
	public void setImageURl(int viewId, String url) {
		ImageView iv = (ImageView) findViewById(viewId);

		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(iv, url, imageOptions);
	}

	/**
	 * 加载图片
	 * 
	 * @param viewId
	 * @param url
	 */
	public void setImageURl(ImageView imageview, String url) {

		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(imageview, url, imageOptions);
	}

	/**
	 * 加载图片
	 * 
	 * @param url
	 *            图片的地址
	 * @param id
	 *            图片控件的ID
	 */
	public void setImageFromUrl(String url, ImageView imageView) {

		LoadImageTask task = new LoadImageTask(imageView);
		task.execute(url);

	}

	/**
	 * 图片加载的任务
	 */
	protected class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private ImageView mImageView;

		public LoadImageTask(ImageView imageView) {
			this.mImageView = imageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				URL picUrl = new URL(params[0]);
				Bitmap pngBM = BitmapFactory.decodeStream(picUrl.openStream());

				return pngBM;
			} catch (Exception e) {
			}

			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (result != null) {
				mImageView.setImageBitmap(result);
				onImageLoaded(mImageView, result);
			} else {
				// showToast(getString(R.string.msg_err_image_loading_failed));
			}
		}
	}

	/**
	 * 图片读取后的处理
	 * 
	 * @param view
	 * @param bitmap
	 */
	protected void onImageLoaded(ImageView view, Bitmap bitmap) {

	}

	/**
	 * dip To Px
	 * 
	 * @param dip
	 * @return Px
	 */
	public int dipToPixels(int dip) {
		Resources r = getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int) px;
	}

	/**
	 * 显示顺序
	 */
	class SortShowOrder implements Comparator<JSONObject> {

		private String mSortKey;

		public SortShowOrder() {
			mSortKey = "ShowOrder";
		}

		public SortShowOrder(String sortKey) {
			mSortKey = sortKey;
		}

		@Override
		public int compare(JSONObject lhs, JSONObject rhs) {
			try {
				int startShowOrder = lhs.getInt(mSortKey);
				int endShowOrder = rhs.getInt(mSortKey);

				return startShowOrder - endShowOrder;
			} catch (Exception e) {
				showSysErr(e);
			}

			return 0;
		}

	}

	// 取消Toast
	public void toastStop() {
		if (null != mToast) {
			mToast.cancel();
		}
	}

	@Override
	protected void onPause() {
		System.out.println("onPause() ");
		toastStop();
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		setContentView(R.layout.null_view);
		System.gc();
		super.onDestroy();
		ToastUtils.cancel();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		ToastUtils.cancel();
	}
}
