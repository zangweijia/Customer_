package com.bopinjia.customer.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bopinjia.customer.R;
import com.bopinjia.customer.util.ImageGet;
import com.bopinjia.customer.util.StorageUtil;

public class SelectModeFragment extends DialogFragment implements OnClickListener {

	private String mPhone;
	private int mCode;
	private String mImageType;
	private IOnSelectModeDismissListner mDismissListener;
	private String s;
	public SelectModeFragment(){
	}
	@SuppressLint({"NewApi", "ValidFragment"})
	public SelectModeFragment(String phone, int code, String imageType, IOnSelectModeDismissListner dismissListener) {
		super();
		mPhone = phone;
		mCode = code;
		mImageType = imageType;
		mDismissListener = dismissListener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_select_picture, container, false);
		v.findViewById(R.id.btn_camera).setOnClickListener(this);
		v.findViewById(R.id.btn_gallery).setOnClickListener(this);
		v.findViewById(R.id.btn_cancel).setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View v) {
		int viewId = v.getId();

		Intent intent = null;

		switch (viewId) {
		case R.id.btn_camera:
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(intent, 1);
			break;

		case R.id.btn_gallery:
			intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/jpeg");
			startActivityForResult(intent, 2);
			break;
		case R.id.btn_cancel:
			dismiss();
			break;
		default:
			break;
		}
	}

	/**
	 * 照片选择或者拍照后的回调方法
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			Bitmap bitmap = null;

			if (requestCode == 1) {
				Bundle bundle = data.getExtras();
//				s = ImageGet.getPath(getActivity(), data.getData());
				bitmap = (Bitmap) bundle.get("data");
				
				
				
				
			} else if (requestCode == 2) {
				try {
					// 获得图片的uri
					Uri originalUri = data.getData();
					// 显得到bitmap图片
//					String[] proj = { MediaStore.Images.Media.DATA };
//					Cursor cursor = getActivity().managedQuery(originalUri, proj, null, null, null);
//					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//					cursor.moveToFirst();
//					originalUri = Uri.parse(cursor.getString(column_index));
//
//					bitmap = StorageUtil.compressImageFromFile(originalUri.getPath());

					
					
					
					s = ImageGet.getPath(getActivity(), originalUri);

					bitmap = StorageUtil.compressImageFromFile(s);

					
				} catch (Exception e) {
					StorageUtil.saveFile(getActivity(), e.getMessage());
				}
			}

			String failname = StorageUtil.saveImage(this.getActivity(), bitmap, mPhone, mImageType);
			
		//	mDismissListener.onSelectModeDismiss(bitmap, mCode);
			mDismissListener.onSelectModeDismissGetString(bitmap,failname, mCode);
			
			dismiss();

		}
	}
	
	/**
	 * 对话框关闭时的监听器
	 */
	public interface IOnSelectModeDismissListner {
		public void onSelectModeDismiss(Bitmap bitmap, int code);
		public void onSelectModeDismissGetString(Bitmap bitmaps,String bitmap, int code);
	}

	
}
