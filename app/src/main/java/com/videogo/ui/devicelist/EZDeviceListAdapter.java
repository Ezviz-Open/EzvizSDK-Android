/**
 * 设备列表适配器
 */
package com.videogo.ui.devicelist;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.EZUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import ezviz.ezopensdk.R;

public class EZDeviceListAdapter extends BaseAdapter {

    private Context mContext = null;
    private List<EZDeviceInfo> mDeviceInfoList = null;
    private OnItemClickListener mListener;
    private ExecutorService mExecutorService = null;// 线程池
    public Map<String, EZDeviceInfo> mExecuteItemMap = null;


    public void clearAll(){
        mDeviceInfoList.clear();
        notifyDataSetChanged();
    }

    public List<EZDeviceInfo> getDeviceInfoList() {
        return mDeviceInfoList;
    }


    public static class ViewHolder {
        public ImageView iconIv;

        public ImageView playBtn;

        public ImageView offlineBtn;

        public TextView deviceNameTv;

        public ImageButton alarmListBtn;

        public ImageButton remoteplaybackBtn;

        public ImageButton setDeviceBtn;

        public ImageButton videoTalkBtn;

        public View itemIconArea;

        public ImageView offlineBgBtn;

    }

    public EZDeviceListAdapter(Context context) {
        mContext = context;
        mDeviceInfoList = new ArrayList<EZDeviceInfo>();
        mExecuteItemMap = new HashMap<String, EZDeviceInfo>();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        mListener = l;
    }

    public void addItem(EZDeviceInfo item) {
        mDeviceInfoList.add(item);
    }

    public void removeItem(EZDeviceInfo item) {
        for(int i = 0; i < mDeviceInfoList.size(); i++) {
            if(item == mDeviceInfoList.get(i)) {
                mDeviceInfoList.remove(i);
            }
        }
    }

    public void clearItem() {
        mDeviceInfoList.clear();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        return mDeviceInfoList.size();
    }

    /* (non-Javadoc)
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public EZDeviceInfo getItem(int position) {
        EZDeviceInfo item = null;
        if (position >= 0 && getCount() > position) {
            item = mDeviceInfoList.get(position);
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
        // 自定义视图
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            // 获取list_item布局文件的视图
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_device_list_small, null);

            // 获取控件对象
            viewHolder.iconIv = (ImageView) convertView.findViewById(R.id.item_icon);
            viewHolder.playBtn = (ImageView) convertView.findViewById(R.id.item_play_btn);

            viewHolder.offlineBtn = (ImageView) convertView.findViewById(R.id.item_offline);
            viewHolder.deviceNameTv = (TextView) convertView.findViewById(R.id.device_name_tv);
            viewHolder.alarmListBtn = (ImageButton) convertView.findViewById(R.id.tab_alarmlist_btn);
            viewHolder.remoteplaybackBtn = (ImageButton) convertView.findViewById(R.id.tab_remoteplayback_btn);
            viewHolder.setDeviceBtn = (ImageButton) convertView.findViewById(R.id.tab_setdevice_btn);
            viewHolder.videoTalkBtn = (ImageButton) convertView.findViewById(R.id.tab_video_talk_btn);
            viewHolder.offlineBgBtn = (ImageView) convertView.findViewById(R.id.offline_bg);
            viewHolder.itemIconArea = convertView.findViewById(R.id.item_icon_area);

            // 设置点击图标的监听响应函数
            viewHolder.playBtn.setOnClickListener(mOnClickListener);

            // 设置报警列表的监听响应函数
            viewHolder.alarmListBtn.setOnClickListener(mOnClickListener);

            // 设置历史回放的监听响应函数
            viewHolder.remoteplaybackBtn.setOnClickListener(mOnClickListener);

            // 设置设备设置的监听响应函数
            viewHolder.setDeviceBtn.setOnClickListener(mOnClickListener);

            // 视频通话的监听响应函数
            viewHolder.videoTalkBtn.setOnClickListener(mOnClickListener);

            // 设置控件集到convertView
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // 设置position
        viewHolder.playBtn.setTag(position);
        viewHolder.remoteplaybackBtn.setTag(position);
        viewHolder.alarmListBtn.setTag(position);
        viewHolder.setDeviceBtn.setTag(position);
        viewHolder.videoTalkBtn.setTag(position);

        final EZDeviceInfo deviceInfo = getItem(position);
        final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo,0);
        if (deviceInfo != null) {
            if (deviceInfo.getStatus() == 2) {
                viewHolder.offlineBtn.setVisibility(View.VISIBLE);
                viewHolder.offlineBgBtn.setVisibility(View.GONE);
                viewHolder.playBtn.setVisibility(View.GONE);
            } else {
                viewHolder.offlineBtn.setVisibility(View.GONE);
                viewHolder.offlineBgBtn.setVisibility(View.GONE);
                viewHolder.playBtn.setVisibility(View.VISIBLE);
            }
            viewHolder.deviceNameTv.setText(TextUtils.isEmpty(deviceInfo.getDeviceName()) ?
                    String.format("%s(%s)", deviceInfo.getCategory(), deviceInfo.getDeviceSerial()) : deviceInfo.getDeviceName());
            viewHolder.iconIv.setVisibility(View.VISIBLE);
            String imageUrl = deviceInfo.getDeviceCover();
            if (!TextUtils.isEmpty(imageUrl)) {
                Glide.with(mContext).load(imageUrl).asBitmap().placeholder(R.drawable.device_other).into(viewHolder.iconIv);
            }
        }
        // 如果是分享设备，隐藏消息列表按钮和设置按钮
        if (cameraInfo != null && (cameraInfo.getIsShared() == 1 || cameraInfo.getIsShared() == 0)) {
            viewHolder.alarmListBtn.setVisibility(View.VISIBLE);
            viewHolder.setDeviceBtn.setVisibility(View.VISIBLE);
        } else {
            viewHolder.alarmListBtn.setVisibility(View.GONE);
            viewHolder.setDeviceBtn.setVisibility(View.GONE);
        }
        // S10设备视频通话功能按钮
        if (deviceInfo != null && deviceInfo.isSupportVideoMeeting() && deviceInfo.getStatus() == 1) {
            viewHolder.videoTalkBtn.setVisibility(View.VISIBLE);
        } else {
            viewHolder.videoTalkBtn.setVisibility(View.GONE);
        }

        return convertView;
    }

    public void shutDownExecutorService() {
        if (mExecutorService != null) {
            if (!mExecutorService.isShutdown()) {
                mExecutorService.shutdown();
            }
            mExecutorService = null;
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = (Integer) v.getTag();
                int id = v.getId();
                if (id == R.id.item_play_btn) {
                    mListener.onPlayClick(EZDeviceListAdapter.this, v, position);
                } else if (id == R.id.tab_remoteplayback_btn) {
                    mListener.onPlayBackClick(EZDeviceListAdapter.this, v, position);
                } else if (id == R.id.tab_alarmlist_btn) {
                    mListener.onAlarmListClick(EZDeviceListAdapter.this, v, position);
                } else if (id == R.id.tab_setdevice_btn) {
                    mListener.onSetDeviceClick(EZDeviceListAdapter.this, v, position);
                } else if (id == R.id.tab_video_talk_btn) {
                    mListener.onVideoTalkClick(EZDeviceListAdapter.this, v, position);
                }
            }
        }

    };

    public interface OnItemClickListener {
        public void onPlayClick(BaseAdapter adapter, View view, int position);
        public void onPlayBackClick(BaseAdapter adapter, View view, int position);
        public void onAlarmListClick(BaseAdapter adapter, View view, int position);
        public void onSetDeviceClick(BaseAdapter adapter, View view, int position);
        public void onVideoTalkClick(BaseAdapter adapter, View view, int position);
    }
}
