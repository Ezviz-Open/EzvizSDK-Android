/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright null
 * 
 * @FileName DeviceDiscoverAdapter.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-5-13
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.devicelist;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdkcommon.R;

public class DeviceDiscoverAdapter extends BaseAdapter {
    private Context mContext = null;
    private List<DeviceDiscoverInfo> mDeviceDiscoverInfoList = null;
    private OnClickListener mOnClickListener;
    
    public static class ViewHolder {
        public TextView serialTv;
        public TextView wifiTv;
        public TextView platTv;
        public TextView ipTv;
        public Button addBtn;
        public Button localRealPlayBtn;
    }
    
    public DeviceDiscoverAdapter(Context context) {
        mContext = context;
        mDeviceDiscoverInfoList = new ArrayList<DeviceDiscoverInfo>();
    }
    
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }
    
    public void addItem(DeviceDiscoverInfo item) {
        mDeviceDiscoverInfoList.add(item);
    }

    public void removeItem(DeviceDiscoverInfo item) {
        mDeviceDiscoverInfoList.remove(item);
    }
    
    public void clearItem() {
        mDeviceDiscoverInfoList.clear();
    }
    
    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mDeviceDiscoverInfoList.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public DeviceDiscoverInfo getItem(int position) {
        DeviceDiscoverInfo item = null;
        if (position >= 0 && getCount() > position) {
            item = mDeviceDiscoverInfoList.get(position);
        }
        return item;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            // 获取list_item布局文件的视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.device_discover_item, null);

            // 获取控件对象
            viewHolder.serialTv = (TextView) convertView.findViewById(R.id.serial_tv);
            viewHolder.wifiTv = (TextView) convertView.findViewById(R.id.wifi_tv);
            viewHolder.platTv = (TextView) convertView.findViewById(R.id.plat_tv);
            viewHolder.ipTv = (TextView) convertView.findViewById(R.id.ip_tv);
            viewHolder.addBtn = (Button) convertView.findViewById(R.id.add_btn);
            viewHolder.localRealPlayBtn = (Button) convertView.findViewById(R.id.local_realplay_btn);
            viewHolder.addBtn.setOnClickListener(mOnClickListener);
            viewHolder.localRealPlayBtn.setOnClickListener(mOnClickListener);
            
            // 设置控件集到convertView
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        DeviceDiscoverInfo deviceDiscoverInfo = getItem(position);
        if(deviceDiscoverInfo != null) {
            viewHolder.serialTv.setText(deviceDiscoverInfo.deviceSerial);
            viewHolder.addBtn.setTag(deviceDiscoverInfo.deviceSerial);
            if(deviceDiscoverInfo.isAdded) {
                viewHolder.addBtn.setText(deviceDiscoverInfo.mEZProbeDeviceInfo!=null?R.string.added_by_me:R.string.added_by_other);
            } else {
                viewHolder.addBtn.setText(R.string.add_device);
            }
            viewHolder.addBtn.setEnabled(!deviceDiscoverInfo.isAdded);
            viewHolder.wifiTv.setVisibility(deviceDiscoverInfo.isWifiConnected?View.VISIBLE:View.GONE);
            viewHolder.platTv.setVisibility(deviceDiscoverInfo.isPlatConnected?View.VISIBLE:View.GONE);
            viewHolder.ipTv.setText(deviceDiscoverInfo.localIP);
            viewHolder.localRealPlayBtn.setEnabled(!TextUtils.isEmpty(deviceDiscoverInfo.localIP));
            viewHolder.localRealPlayBtn.setTag(deviceDiscoverInfo);
        }
        
        return convertView;
    }

}
