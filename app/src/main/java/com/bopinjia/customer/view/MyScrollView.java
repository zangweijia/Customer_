package com.bopinjia.customer.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * 博客地址:http://blog.csdn.net/xiaanming
 * 
 * @author xiaanming
 * 
 */
public class MyScrollView extends ScrollView {
	private OnScrollListener onScrollListener;
	/**
	 * 主要是用在用户手指离开MyScrollView，MyScrollView还在继续滑动，我们用来保存Y的距离，然后做比较
	 */
	private int lastScrollY;

	public MyScrollView(Context context) {
		this(context, null);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mGestureDetector = new GestureDetector(new YScrollDetector());
		setFadingEdgeLength(0);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置滚动接口
	 * 
	 * @param onScrollListener
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	/**
	 * 用于用户手指离开MyScrollView的时候获取MyScrollView滚动的Y距离，然后回调给onScroll方法中
	 */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			int scrollY = MyScrollView.this.getScrollY();

			// 此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息
			if (lastScrollY != scrollY) {
				lastScrollY = scrollY;
				handler.sendMessageDelayed(handler.obtainMessage(), 5);
			}
			if (onScrollListener != null) {
				onScrollListener.onScroll(scrollY);
			}

		};

	};

	/**
	 * 重写onTouchEvent， 当用户的手在MyScrollView上面的时候，
	 * 直接将MyScrollView滑动的Y方向距离回调给onScroll方法中，当用户抬起手的时候，
	 * MyScrollView可能还在滑动，所以当用户抬起手我们隔5毫秒给handler发送消息，在handler处理
	 * MyScrollView滑动的距离
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (onScrollListener != null) {
			onScrollListener.onScroll(lastScrollY = this.getScrollY());
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			handler.sendMessageDelayed(handler.obtainMessage(), 5);
			handler.sendMessageDelayed(handler.obtainMessage(), 10);
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 
	 * 滚动的回调接口
	 * 
	 * @author xiaanming
	 * 
	 */
	public interface OnScrollListener {
		/**
		 * 回调方法， 返回MyScrollView滑动的Y方向距离
		 * 
		 * @param scrollY
		 *            、
		 */
		public void onScroll(int scrollY);
	}

	// ----------------------------解决滑动冲突 viewpager
	// 滑动距离及坐标
	private float xDistance, yDistance, xLast, yLast;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance) {
				return false;
			}
		}

		return super.onInterceptTouchEvent(ev);
	}
	// ----------------------------解决滑动冲突

	// --------------添加 判断HorizontallistView 是否到底
	private OnScrollToBottomListener onScrollToBottom;

	@Override
	protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
		super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
		if (scrollY != 0 && null != onScrollToBottom) {
			onScrollToBottom.onScrollBottomListener(clampedY);
		}
	}

	public void setOnScrollToBottomLintener(OnScrollToBottomListener listener) {
		onScrollToBottom = listener;
	}

	public interface OnScrollToBottomListener {
		public void onScrollBottomListener(boolean isBottom);
	}
	// --------------添加 判断HorizontallistView 是否到底

	// -----------scrollview 嵌套 HorizontallistView
	private GestureDetector mGestureDetector;
	View.OnTouchListener mGestureListener;

	// Return false if we're scrolling in the x direction
	class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (Math.abs(distanceY) > Math.abs(distanceX)) {
				return true;
			}
			return false;
		}
	}
	// ----------------------

	
	
	//---------------------- 判断scrollview  滑动方向
	private ScrollListener mListener;  
	  
    public static interface ScrollListener {  
        public void scrollOritention(int oritention);  
    }  
  
    /** 
     * ScrollView正在向上滑动 
     */  
    public static final int SCROLL_UP = 0;  
  
    /** 
     * ScrollView正在向下滑动 
     */  
    public static final int SCROLL_DOWN = 1;  
  
    /** 
     * 最小的滑动距离 
     */  
    private static final int SCROLLLIMIT = 40;  
	
    @Override  
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {  
        super.onScrollChanged(l, t, oldl, oldt);  
  
        if (oldt > t && oldt - t > SCROLLLIMIT) {// 向下  
            if (mListener != null)  
                mListener.scrollOritention(SCROLL_DOWN);  
        } else if (oldt < t && t - oldt > SCROLLLIMIT) {// 向上  
            if (mListener != null)  
                mListener.scrollOritention(SCROLL_UP);  
        }  
    }  
  
    public void setScrollListener(ScrollListener l) {  
        this.mListener = l;  
    }  
    
    
    //----------------------------------
}