package com.bopinjia.customer.activity;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.bopinjia.customer.R;
import com.bopinjia.customer.constants.Constants;
import com.bopinjia.customer.fragment.SelectModeFragment;
import com.bopinjia.customer.fragment.SelectModeFragment.IOnSelectModeDismissListner;
import com.bopinjia.customer.util.MD5;
import com.bopinjia.customer.view.NoScrollGridView;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;

public class ActivityRetractRequest extends BaseActivity implements IOnSelectModeDismissListner {

	private int mRetractCount = 1;
	private int mMaxCount;

	private String mOrderSn;
	private JSONObject mProductModel;
	private NoScrollGridView mGridView;

	private Bitmap bmp;
	// 存储Bmp图像
	private ArrayList<HashMap<String, Object>> imageItem;

	private ArrayList<HashMap<String, Object>> imageItemDX;

	// 适配器
	private SimpleAdapter simpleAdapter;

	private List<String> urlList;
	private List<String> urlListDX;
	private String name;
	private String phone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wj_activity_retract_request);
		urlList = new ArrayList<String>();
		urlListDX = new ArrayList<String>();
		// 返回
		findViewById(R.id.btn_return).setOnClickListener(this);
		// 返品数量加减按钮
		findViewById(R.id.btn_reduce).setOnClickListener(this);
		findViewById(R.id.btn_add).setOnClickListener(this);

		// 下一步
		findViewById(R.id.btn_next).setOnClickListener(this);

		display(getIntent().getStringExtra("product"));
		mOrderSn = getIntent().getStringExtra("OrderSn");
		name = getIntent().getStringExtra("name");
		phone = getIntent().getStringExtra("phone");

		((TextView) findViewById(R.id.name)).setText(name);
		((TextView) findViewById(R.id.phone)).setText(phone);

		mGridView = (NoScrollGridView) findViewById(R.id.grid);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.addpic); // 加号
		imageItem = new ArrayList<HashMap<String, Object>>();

		imageItemDX = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("itemImage", bmp);
		map.put("pathImage", "add_pic");
		imageItem.add(map);
		for (int i = imageItem.size() - 1; i >= 0; i--) {
			imageItemDX.add(imageItem.get(i));
		}
		simpleAdapter = new SimpleAdapter(this, imageItemDX, R.layout.griditem_addpic, new String[] { "itemImage" },
				new int[] { R.id.imageView1 });

		/*
		 * HashMap载入bmp图片在GridView中不显示,但是如果载入资源ID能显示 如 map.put("itemImage",
		 * R.drawable.img); 解决方法: 1.自定义继承BaseAdapter实现 2.ViewBinder()接口实现 参考
		 * http://blog.csdn.net/admin_/article/details/7257901
		 */
		simpleAdapter.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				// TODO Auto-generated method stub
				if (view instanceof ImageView && data instanceof Bitmap) {
					ImageView i = (ImageView) view;
					i.setImageBitmap((Bitmap) data);
					return true;
				}
				return false;
			}
		});
		mGridView.setAdapter(simpleAdapter);

		/*
		 * 监听GridView点击事件 报错:该函数必须抽象方法 故需要手动导入import android.view.View;
		 */
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				if (position == 4) { // 第一张为默认图片
					Toast.makeText(ActivityRetractRequest.this, "亲，最多添加3张照片", Toast.LENGTH_SHORT).show();

				} else if (position == imageItemDX.size() - 1) { // 点击图片位置为+
																	// 0对应0张图片
					SelectModeFragment fragment = new SelectModeFragment(getLoginPhone(), 0, Constants.IMAGE_TUI_HUO,
							ActivityRetractRequest.this);
					fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
					fragment.show(getFragmentManager(), "dialog");
				} else {
					DeleteDialog(position);
				}

			}
		});

	}

	/**
	 * 商品数据展示
	 * 
	 * @param product
	 *            商品数据
	 */
	private void display(String product) {
		try {
			JSONObject data = new JSONObject(product);
			mProductModel = data;
			((TextView) findViewById(R.id.txt_product_name)).setText(data.getString("ProductSKUName"));
			((TextView) findViewById(R.id.txt_price)).setText(
					MessageFormat.format(getString(R.string.txt_retract_price), data.getString("ProductPrice")));
			((TextView) findViewById(R.id.txt_count))
					.setText(MessageFormat.format(getString(R.string.txt_retract_count), data.getString("BuyCount")));

			setImageFromUrl(data.getString("ThumbnailsUrl"), R.id.iv_product);

			((TextView) findViewById(R.id.txt_req_max)).setText(MessageFormat
					.format(getString(R.string.txt_retract_req_count_max), data.getString("RetractableCount")));
			mMaxCount = Integer.parseInt(data.getString("RetractableCount"));
		} catch (Exception e) {
			showSysErr(e);
		}
	}

	/**
	 * 画面控件点击回调函数
	 */
	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		switch (viewId) {
		case R.id.btn_return:
			// 返回
			backward();
			break;
		case R.id.btn_next:
			// 下一步
			if (urlList.size() == 0) {
				showToast("请上传凭证~");
				return;
			}
			if (((EditText) findViewById(R.id.edt_reason)).getText().toString().trim().length() >= 2) {
				returnOrder();
			} else {
				showToast("请填写不少于2个字的退货原因~");
			}

			break;
		case R.id.btn_reduce:
			if (mRetractCount > 1) {
				mRetractCount--;
			}
			((TextView) findViewById(R.id.txt_retract_count)).setText(String.valueOf(mRetractCount));
			break;
		case R.id.btn_add:
			if (mRetractCount < mMaxCount) {
				mRetractCount++;
			}
			((TextView) findViewById(R.id.txt_retract_count)).setText(String.valueOf(mRetractCount));
			break;
		default:
			break;
		}
	}

	private void returnOrder() {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		// 申请的退款金额
		double price;
		String mEdtReason = null;
		String mProSKUId = null;
		double amount = 0;
		DecimalFormat df = null;
		try {
			price = new BigDecimal(mProductModel.getString("ProductPrice")).doubleValue();
			amount = price * mRetractCount;
			df = new DecimalFormat("########0.00");
			// 退款原因
			mEdtReason = ((EditText) findViewById(R.id.edt_reason)).getText().toString();
			mProSKUId = mProductModel.getString("ProductSKUId");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String mEdtreason = null;
		try {
			mEdtreason = URLEncoder.encode(mEdtReason, "utf-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		map.put("UserId", getLoginUserId());
		map.put("OrderSn", mOrderSn);
		map.put("ProductSKUId", mProSKUId);
		map.put("ReturnCount", String.valueOf(mRetractCount));
		map.put("ApplyReturnMoney", df.format(amount));
		map.put("ReturnOrderReason", mEdtreason);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/ReturnOrder/Add_Android?UserId="
				+ getLoginUserId() + "&OrderSn=" + mOrderSn + "&ProductSKUId=" + mProSKUId + "&ReturnCount="
				+ String.valueOf(mRetractCount) + "&ApplyReturnMoney=" + df.format(amount) + "&ReturnOrderReason="
				+ mEdtreason + "&Sign=" + Sign + "&Ts=" + Ts);

		params.setMultipart(true);
		for (int i = 0, len = urlListDX.size(); i < len; i++) {
			params.addBodyParameter("Filedata" + i, new File(urlListDX.get(i)));
		}

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {
						Toast toast = Toast.makeText(ActivityRetractRequest.this, "您已成功提交退货申请！", 0);
						toast.setGravity(Gravity.CENTER, 0, 0);
						LinearLayout toastView = (LinearLayout) toast.getView();
						ImageView imageCodeProject = new ImageView(ActivityRetractRequest.this);
						imageCodeProject.setImageResource(R.drawable.ic_success);
						toastView.addView(imageCodeProject, 0);
						toast.show();
						new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(2000);
									sendSMS(getString(R.string.msg_info_return_req_success));
									forward(ActivityRetractList.class);
									finish();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}).start();

					} else if (jsonresult.equals("3")) {
						showToast("亲，你已经错过了申请售后的时间段~");
					} else if (jsonresult.equals("4")) {
						showToast("亲，没有那么多可退的商品~");
					} else {
						showToast("亲，输入的订单信息有误~");
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				showToast("申请失败~");
			}

			@Override
			public void onCancelled(CancelledException cex) {
			}

			@Override
			public void onFinished() {
			}
		});
	}

	private void sendSMS(String message) {
		Map<String, String> map = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String obj1, String obj2) {
				return obj1.compareTo(obj2);
			}
		});
		String Ts = MD5.getTimeStamp();

		map.put("Phone", getLoginUserId());
		map.put("Message", message);
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

		RequestParams params = new RequestParams(Constants.WEBAPI_ADDRESS + "api/SMS/Send");
		params.addBodyParameter("Phone", getLoginUserId());
		params.addBodyParameter("Message", message);

		params.addBodyParameter("Sign", Sign);
		params.addBodyParameter("Ts", Ts);
		params.setAsJsonContent(true);

		x.http().post(params, new Callback.CommonCallback<String>() {
			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					String jsonresult = jo.getString("Result");
					if (jsonresult.equals("1")) {

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			private JSONObject JSONObject(String result) {
				// TODO Auto-generated method stub
				return null;
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

	/***
	 * 用不到
	 */
	@Override
	public void onSelectModeDismiss(Bitmap bitmap, int code) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSelectModeDismissGetString(Bitmap bitmaps, String bitmap, int code) {
		if (!TextUtils.isEmpty(bitmap)) {
			Bitmap addbmp = BitmapFactory.decodeFile(bitmap);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", addbmp);
			map.put("pathImage", bitmap);
			imageItem.add(map);
			imageItemDX.clear();
			for (int i = imageItem.size() - 1; i >= 0; i--) {
				imageItemDX.add(imageItem.get(i));
			}

			simpleAdapter.notifyDataSetChanged();
			urlList.add(bitmap);
			for (int i = urlList.size() - 1; i >= 0; i--) {
				urlListDX.add(urlList.get(i));
			}
			// 刷新后释放防止手机休眠后自动添加
			bitmap = null;

		}

	}

	private Dialog mDialog;
	private View dialogView;
	private Button confirmBt;
	private Button cancelBt;

	/*
	 * Dialog对话框提示用户删除操作 position为删除图片位置
	 */
	protected void DeleteDialog(final int position) {

		mDialog = new Dialog(this, R.style.CustomDialogTheme);
		dialogView = LayoutInflater.from(this).inflate(R.layout.send_tel_dailog, null);
		// 设置自定义的dialog布局
		mDialog.setContentView(dialogView);
		// false表示点击对话框以外的区域对话框不消失，true则相反
		mDialog.setCanceledOnTouchOutside(false);
		confirmBt = (Button) dialogView.findViewById(R.id.bt_send);
		cancelBt = (Button) dialogView.findViewById(R.id.bt_cancel);

		mDialog.show();
		// 获取自定义dialog布局控件
		((TextView) dialogView.findViewById(R.id.dialogcontent)).setText("确认移除已添加图片吗？");
		// 确定按钮点击事件
		confirmBt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				urlListDX.remove(position - 1);
				imageItemDX.remove(position);
				simpleAdapter.notifyDataSetChanged();
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

	}
}
