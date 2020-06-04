/* 
 * @ProjectName VideoGoJar
 * @Copyright null
 * 
 * @FileName CameraListAdapter.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-7-14
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.ddns;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.videogo.openapi.bean.EZHiddnsDeviceInfo;
import ezviz.ezopensdk.R;
import java.util.ArrayList;
import java.util.List;

public class EZDDNSListAdapter extends BaseAdapter {
    private static final String TAG = "CameraListAdapter";

    private Context mContext = null;
    private List<EZHiddnsDeviceInfo> mCameraInfoList = null;


    private OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(OnItemClickListener l) {
        mOnItemClickListener = l;
    }


    interface OnItemClickListener {

        void onItemClick(int  position);
    }
    public void clearAll(){
        mCameraInfoList.clear();
        notifyDataSetChanged();
    }

    public List<EZHiddnsDeviceInfo> getDeviceInfoList() {
        return mCameraInfoList;
    }


    public static class ViewHolder {


        public TextView mDeviceSerialTextView;
        private TextView mDeviceIpTextView;

    }

    public EZDDNSListAdapter(Context context) {
        mContext = context;
        mCameraInfoList = new ArrayList<EZHiddnsDeviceInfo>();
    }

    
    public void addItem(EZHiddnsDeviceInfo item) {
        mCameraInfoList.add(item);
    }

    public void removeItem(EZHiddnsDeviceInfo item) {
        for(int i = 0; i < mCameraInfoList.size(); i++) {
            if(item == mCameraInfoList.get(i)) {
                mCameraInfoList.remove(i);
            }
        }
    }
    
    public void clearItem() {
        //mExecuteItemMap.clear();
        mCameraInfoList.clear();
    }
    
    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mCameraInfoList.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public EZHiddnsDeviceInfo getItem(int position) {
        EZHiddnsDeviceInfo item = null;
        if (position >= 0 && getCount() > position) {
            item = mCameraInfoList.get(position);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 自定义视图
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            // 获取list_item布局文件的视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ddns_device_list_item, null);
            viewHolder.mDeviceSerialTextView = (TextView) convertView.findViewById(R.id.text_serial);
            viewHolder.mDeviceIpTextView = (TextView) convertView.findViewById(R.id.text_device_ip);
            // 设置控件集到convertView
            convertView.setTag(viewHolder);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(position);
                    }
                }
            });
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        EZHiddnsDeviceInfo ezHiddnsDeviceInfo = mCameraInfoList.get(position);
        viewHolder.mDeviceSerialTextView.setText(ezHiddnsDeviceInfo.getSubSerial());
        viewHolder.mDeviceIpTextView.setText(ezHiddnsDeviceInfo.getDeviceIp());
        return convertView;
    }
}
