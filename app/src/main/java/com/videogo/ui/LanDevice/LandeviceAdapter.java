package com.videogo.ui.LanDevice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ezviz.hcnetsdk.EZSADPDeviceInfo;

import java.util.List;

import ezviz.ezopensdk.R;


public class LandeviceAdapter extends BaseAdapter {

    // Context
    private Context mContext;
    // List data
    private List<EZSADPDeviceInfo> mLanDeviceInfoList;

    private LayoutInflater mLayoutInflater;
    // monitor
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }

    public LandeviceAdapter(Context context,List<EZSADPDeviceInfo> data) {
        mContext = context;
        mLanDeviceInfoList = data;
        mLayoutInflater = LayoutInflater.from(context);
    }


    public void  add(EZSADPDeviceInfo lanDeviceInfo){
        if (lanDeviceInfo != null && mLanDeviceInfoList != null){
            mLanDeviceInfoList.add(lanDeviceInfo);
            notifyDataSetChanged();
        }
    }
    public void setData(List<EZSADPDeviceInfo> data) {
        this.mLanDeviceInfoList = data;
    }

    @Override
    public int getCount() {
        return mLanDeviceInfoList == null ? 0 : mLanDeviceInfoList.size();
    }

    @Override
    public EZSADPDeviceInfo getItem(int position) {
        return mLanDeviceInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        ItemViewHolder itemViewHolder = null;
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.lan_device_adapter_item, viewGroup, false);
            itemViewHolder = new ItemViewHolder(view);
            view.setTag(itemViewHolder);
        } else {
            itemViewHolder = (ItemViewHolder) view.getTag();
        }

        EZSADPDeviceInfo temp = mLanDeviceInfoList.get(position);

        itemViewHolder.deviceName.setText(temp.getDeviceSerial());
        itemViewHolder.ipInfo.setText(temp.getLocalIp() + ":" + temp.getLocalPort());
        itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
        return view;
    }

    interface OnItemClickListener {

        void onItemClick(int position);
    }

    class ItemViewHolder {
        ImageView cover;
        TextView deviceName;
        TextView ipInfo;
        View itemView = null;

        public ItemViewHolder(View itemView) {
            this.itemView = itemView;
            cover = (ImageView) itemView.findViewById(R.id.cover);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
            ipInfo = (TextView) itemView.findViewById(R.id.ip_info);
        }
    }
}
