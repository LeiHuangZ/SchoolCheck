package com.example.huang.myapplication.system;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.huang.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * recyclerView适配器
 *
 * @author huang
 * @date 2017/10/24
 */

class RcAdapter extends RecyclerView.Adapter<RcAdapter.MyViewHolder> {
    private String TAG = RcAdapter.class.getSimpleName();
    private WifiControl mWifiControl;

    private List<ScanResult> mList = new ArrayList<>();

    RcAdapter(Context context, List<ScanResult> list) {
        mList.addAll(list);
        mWifiControl = WifiControl.getInstance(context);
    }

    List<ScanResult> getList() {
        return mList;
    }

    void setList(List<ScanResult> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_rc_wifi, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int getPosition) {
        // 绑定数据
        ScanResult scanResult = mList.get(holder.getAdapterPosition());
        String ssid = scanResult.SSID;
        holder.mTextView.setText(ssid.trim());
        if (mWifiControl.isConnected(ssid)) {
            holder.mState.setText("已连接");
        } else if (mWifiControl.isSaved(ssid)) {
            holder.mState.setText("已保存");
        }else {
            holder.mState.setText("");
        }

        //根据信号强度，显示信号图标
        if (-100 <= scanResult.level && scanResult.level < -85) {
            holder.img.setImageResource(R.mipmap.wifi_level1);
        } else if (-85 <= scanResult.level && scanResult.level < -75) {
            holder.img.setImageResource(R.mipmap.wifi_level2);
        } else if (-75 <= scanResult.level && scanResult.level < -60) {
            holder.img.setImageResource(R.mipmap.wifi_level3);
        } else if (-60 <= scanResult.level && scanResult.level <= 0) {
            holder.img.setImageResource(R.mipmap.wifi_level4);
        }

        //条目点击事件，通过接口回掉，返回给主界面
        holder.mLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;
        LinearLayout mLL;
        TextView mState;
        ImageView img;

        MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(R.id.setting_rc_wifi_name);
            mLL = (LinearLayout) itemView.findViewById(R.id.rc_ll);
            mState = (TextView) itemView.findViewById(R.id.setting_rc_wifi_state);
            img = (ImageView) itemView.findViewById(R.id.setting_rc_wifi_level);
        }
    }

    private RcItemClickListener mListener;

    void setRcItemClickListener(RcItemClickListener listener) {
        mListener = listener;
    }

    interface RcItemClickListener {
        /**
         * RecyclerView条目点击回掉
         *
         * @param position 点击位置
         */
        void onItemClick(int position);
    }

}
