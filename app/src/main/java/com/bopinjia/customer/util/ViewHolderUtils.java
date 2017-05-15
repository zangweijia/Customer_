package com.bopinjia.customer.util;

import org.xutils.x;
import org.xutils.image.ImageOptions;

import com.bopinjia.customer.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

public class ViewHolderUtils {
	private SparseArray<View> mView;
	private int mPosition;
	private View mConvertView;

	public ViewHolderUtils(Context context, ViewGroup parent, int layoutId, int position) {
		this.mPosition = position;
		this.mView = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
		mConvertView.setTag(this);
	}

	public static ViewHolderUtils get(Context context, View converView, ViewGroup parent, int layoutId, int position) {
		if (converView == null) {
			return new ViewHolderUtils(context, parent, layoutId, position);
		} else {
			ViewHolderUtils holder = (ViewHolderUtils) converView.getTag();
			holder.mPosition = position;
			return holder;
		}
	}

	public <T extends View> T getView(int viewId) {
		View view = mView.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mView.put(viewId, view);
		}
		return (T) view;
	}

	public View getConvertView() {
		return mConvertView;
	}

	/**
	 * 设置textview的值
	 * 
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolderUtils setText(int viewId, String text) {
		TextView textView = getView(viewId);
		textView.setText(text);
		return this;
	}

	/**
	 * 设置textview背景
	 */
	public ViewHolderUtils setTextImageResource(int viewId, int resId) {
		getView(viewId).setBackgroundResource(resId);
		return this;
	}

	/**
	 * 设置图片
	 * 
	 * @param viewId
	 * @param resId
	 * @return
	 */
	public ViewHolderUtils setImageResource(int viewId, int resId) {
		ImageView view = getView(viewId);
		view.setImageResource(resId);
		return this;
	}

	/**
	 * 设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolderUtils setImageBitmap(int viewId, Bitmap bitmap) {
		ImageView view = getView(viewId);
		view.setImageBitmap(bitmap);
		return this;
	}

	/**
	 * 设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolderUtils setImageURl(int viewId, String url) {
		ImageView view = getView(viewId);
		// ImageLoader.getInstance().loadImg(view,url);
		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)// 加载失败后默认显示图片
				.build();

		x.image().bind(view, url, imageOptions);
		return this;
	}
	
	/**
	 * 设置图片
	 * 
	 * @param viewId
	 * @return
	 */
	public ViewHolderUtils setCircleImageURl(int viewId, String url) {
		ImageView view = getView(viewId);
		// ImageLoader.getInstance().loadImg(view,url);
		ImageOptions imageOptions = new ImageOptions.Builder().setImageScaleType(ImageView.ScaleType.CENTER_CROP)
				.setFailureDrawableId(R.drawable.ic_default_image)
				.setCircular(true)// 加载失败后默认显示图片
				.build();

		x.image().bind(view, url, imageOptions);
		return this;
	}

	 

	/**
	 * 设置RatingBar显示
	 */
	public ViewHolderUtils setRatingBarText(int viewId, int number) {
		RatingBar ratingBar = getView(viewId);
		ratingBar.setRating(number);
		return this;
	}
}
