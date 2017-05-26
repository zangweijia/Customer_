package com.bopinjia.customer.net;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.bopinjia.customer.R;
import com.bopinjia.customer.view.CustomProgressDialog;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.File;
import java.util.Map;

public class XutilsHttp {
	private volatile static XutilsHttp instance;
	private Handler handler;
	private ImageOptions options;

	private XutilsHttp() {
		handler = new Handler(Looper.getMainLooper());

	}

	/**
	 * 单例模式
	 * 
	 * @return
	 */
	public static XutilsHttp getInstance() {
		if (instance == null) {
			instance = new XutilsHttp();
		}
		return instance;
	}
	// public static XutilsHttp getInstance() {
	// if (instance == null) {
	// synchronized (XutilsHttp.class) {
	// if (instance == null) {
	// instance = new XutilsHttp();
	// }
	// }
	// }
	// return instance;
	// }
	//
	//

	/**
	 * 异步get请求
	 *
	 * @param url
	 * @param maps
	 * @param callBack
	 */
	public void get(String url, Map<String, String> maps, final XCallBack callBack, final Context context) {
		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addQueryStringParameter(entry.getKey(), entry.getValue());
			}
		}
		final CustomProgressDialog dialog = new CustomProgressDialog(context, "正在加载中", R.anim.frame);
//		final ProgressDialog dialog = new ProgressDialog(context);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();

		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {

				onSuccessResponse(result, callBack);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
				dialog.dismiss();
			}
		});

	}

	/**
	 * 异步get请求
	 *
	 * @param url
	 * @param maps
	 * @param callBack
	 */
	public void get(String url, Map<String, String> maps, final XCallBackID callBack, final int id, final String str,
			final Context context) {
		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addQueryStringParameter(entry.getKey(), entry.getValue());
			}
		}
		final CustomProgressDialog dialog = new CustomProgressDialog(context, "正在加载中", R.anim.frame);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		x.http().get(params, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {

				onSuccessResponseID(result, callBack, id, str);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
				dialog.dismiss();
			}
		});

	}

	/**
	 * 异步post请求
	 *
	 * @param url
	 * @param maps
	 * @param callback
	 */
	public void post(String url, Map<String, String> maps, final XCallBack callback, final Context context) {
		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addBodyParameter(entry.getKey(), entry.getValue());
			}
			params.setAsJsonContent(true);
		}
		final CustomProgressDialog dialog = new CustomProgressDialog(context, "正在加载中", R.anim.frame);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		x.http().post(params, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				onSuccessResponse(result, callback);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 异步post请求
	 *
	 * @param url
	 * @param maps
	 * @param callback
	 */
	public void post(String url, Map<String, String> maps, final XCallBackID callback, final int id, final String str,
			final Context context) {
		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addBodyParameter(entry.getKey(), entry.getValue());
			}
			params.setAsJsonContent(true);
		}
		final CustomProgressDialog dialog = new CustomProgressDialog(context, "正在加载中", R.anim.frame);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		x.http().post(params, new Callback.CommonCallback<String>() {

			@Override
			public void onSuccess(String result) {
				onSuccessResponseID(result, callback, id, str);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
				dialog.dismiss();
			}
		});
	}

	/**
	 * 带缓存数据的异步 get请求
	 *
	 * @param url
	 * @param maps
	 * @param pnewCache
	 * @param callback
	 */
	public void getCache(String url, Map<String, String> maps, final boolean pnewCache, final XCallBack callback,
			final Context context) {

		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addQueryStringParameter(entry.getKey(), entry.getValue());
			}
		}
		final CustomProgressDialog dialog = new CustomProgressDialog(context, "正在加载中", R.anim.frame);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		x.http().get(params, new Callback.CacheCallback<String>() {
			@Override
			public void onSuccess(String result) {
				onSuccessResponse(result, callback);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
				dialog.dismiss();
			}

			@Override
			public boolean onCache(String result) {
				boolean newCache = pnewCache;
				if (newCache) {
					newCache = !newCache;
				}
				if (!newCache) {
					newCache = !newCache;
					onSuccessResponse(result, callback);
				}
				return newCache;
			}
		});
	}

	/**
	 * 带缓存数据的异步 post请求
	 *
	 * @param url
	 * @param maps
	 * @param pnewCache
	 * @param callback
	 */
	public void postCache(String url, Map<String, String> maps, final boolean pnewCache, final XCallBack callback,
			final Context context) {
		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addBodyParameter(entry.getKey(), entry.getValue());
			}
		}
		final CustomProgressDialog dialog = new CustomProgressDialog(context, "正在加载中", R.anim.frame);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		x.http().post(params, new Callback.CacheCallback<String>() {
			@Override
			public void onSuccess(String result) {
				onSuccessResponse(result, callback);
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				dialog.dismiss();
			}

			@Override
			public void onCancelled(CancelledException cex) {
				dialog.dismiss();
			}

			@Override
			public void onFinished() {
				dialog.dismiss();
			}

			@Override
			public boolean onCache(String result) {
				boolean newCache = pnewCache;
				if (newCache) {
					newCache = !newCache;
				}
				if (!newCache) {
					newCache = !newCache;
					onSuccessResponse(result, callback);
				}
				return newCache;
			}
		});
	}

	/**
	 * 正常图片显示
	 *
	 * @param iv
	 * @param url
	 * @param option
	 * @param id
	 *            默认图Id
	 */
	public void bindCommonImage(ImageView iv, String url, boolean option, int id) {
		if (option) {
			options = new ImageOptions.Builder().setFailureDrawableId(id).build();
			x.image().bind(iv, url, options);
		} else {
			x.image().bind(iv, url);
		}
	}

	/**
	 * 圆形图片显示
	 *
	 * @param iv
	 * @param url
	 * @param option
	 * @param id
	 *            默认图Id
	 */
	public void bindCircularImage(ImageView iv, String url, boolean option, int id) {
		if (option) {
			options = new ImageOptions.Builder().setFailureDrawableId(id).setCircular(true).build();
			x.image().bind(iv, url, options);
		} else {
			x.image().bind(iv, url);
		}
	}

	/**
	 * 文件上传
	 *
	 * @param url
	 * @param maps
	 * @param file
	 * @param callback
	 */
	public void upLoadFile(String url, Map<String, String> maps, Map<String, File> file, final XCallBack callback,
			Context context) {
		RequestParams params = new RequestParams(url);
		if (maps != null && !maps.isEmpty()) {
			for (Map.Entry<String, String> entry : maps.entrySet()) {
				params.addBodyParameter(entry.getKey(), entry.getValue());
			}
		}
		if (file != null) {
			for (Map.Entry<String, File> entry : file.entrySet()) {
				params.addBodyParameter(entry.getKey(), entry.getValue().getAbsoluteFile());
			}
		}
		// 有上传文件时使用multipart表单, 否则上传原始文件流.
		params.setMultipart(true);
		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				onSuccessResponse(result, callback);
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
	 * 异步get请求返回结果,json字符串
	 *
	 * @param result
	 * @param callBack
	 */
	private void onSuccessResponse(final String result, final XCallBack callBack) {
//		handler.post(new Runnable() {
//			@Override
//			public void run() {
				if (callBack != null) {
					callBack.onResponse(result);
				}
//			}
//		});
	}

	public interface XCallBack {
		void onResponse(String result);
	}

	/**
	 * 异步get请求返回结果,json字符串
	 *
	 * @param result
	 * @param callBack
	 */
	private void onSuccessResponseID(final String result, final XCallBackID callBack, final int id, final String str) {
//		handler.post(new Runnable() {
//			@Override
//			public void run() {
				if (callBack != null) {
					callBack.onResponse(result, id, str);
				}
//			}
//		});
	}

	public interface XCallBackID {
		void onResponse(String result, int id, String str);
	}

	public interface XDownLoadCallBack extends XCallBack {
		void onResponse(File result);

		void onLoading(long total, long current, boolean isDownloading);

		void onFinished();
	}

}
