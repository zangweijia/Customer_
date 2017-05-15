package com.bopinjia.customer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


import java.util.List;

import com.bopinjia.customer.util.ViewHolderUtils;

public abstract class CommonAdapter<T> extends BaseAdapter {
	protected List<T> list;
	protected Context context;
	protected LayoutInflater inflater;
	private int layoutId;
	
	public CommonAdapter(List<T> list, Context context, int layoutId){
		super();
		this.list=list;
		this.context=context;
		this.layoutId=layoutId;
		inflater=LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public T getItem(int arg0) {
		// TODO Auto-generated method stub
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2){
		ViewHolderUtils holder= ViewHolderUtils.get(context, arg1, arg2, layoutId, arg0);
		convert(holder, getItem(arg0),arg0);
		return holder.getConvertView();
	}

	public abstract void convert(ViewHolderUtils holder,T t,int position);
}
