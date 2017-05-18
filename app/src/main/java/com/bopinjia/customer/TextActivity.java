package com.bopinjia.customer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class TextActivity extends AppCompatActivity {

    @ViewInject(R.id.recycle)
    private RecyclerView mRecycleView;
    private List<String> mDatas;
    private mAdapter listadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        x.view().inject(this);
        initData();
        mRecycleView.setLayoutManager(new GridLayoutManager(this, 3));
        final mAdapter adapter = new mAdapter(R.layout.my_test_item, mDatas);
        mRecycleView.setAdapter(adapter);


        BaseQuickAdapter.RequestLoadMoreListener m = new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                mRecycleView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (i < 3) {
                            i++;
                            adapter.addData(mDatas);
                        } else {
                            Toast.makeText(TextActivity.this, "没有更多了", Toast.LENGTH_SHORT).show();
                            adapter.loadComplete();
                        }
                    }
                }, 200);
            }
        };
        adapter.setOnLoadMoreListener(m);

    }


    private int i = 0;

    protected void initData() {
        mDatas = new ArrayList<String>();
        for (int i = 'A'; i < 'z'; i++) {
            mDatas.add("" + (char) i);
        }
    }


    class mAdapter extends BaseQuickAdapter<String> {

        public mAdapter(int layoutResId, List<String> data) {
            super(layoutResId, data);
        }

        @Override
        protected void convert(BaseViewHolder baseViewHolder, String s) {
            baseViewHolder.setText(R.id.text, s);
        }
    }


}
