package com.videogo.ui.cameralist;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.util.ArrayList;
import java.util.List;

import ezviz.ezopensdk.R;

public class EZCameraListInviteDeviceAdapter extends BaseAdapter {
    private static final String TAG = "CameraListAdapter";

    private Context mContext = null;
    private List<EZDeviceInfo> mCameraInfoList = null;
    private OnClickListener mListener;

    public void clearAll(){
        mCameraInfoList.clear();
        notifyDataSetChanged();
    }

    public List<EZDeviceInfo> getDeviceInfoList() {
        return mCameraInfoList;
    }

    public void addItems(List<EZDeviceInfo> items) {
        mCameraInfoList.addAll(items);
    }

    public void clearItem() {
        mCameraInfoList.clear();
    }

    public static class ViewHolder {
        public ImageView iconIv;
        public TextView cameraNameTv;
        public Button inviteDeviceBtn;
    }

    public EZCameraListInviteDeviceAdapter(Context context) {
        mContext = context;
        mCameraInfoList = new ArrayList<EZDeviceInfo>();
    }
    
    public void setOnClickListener(OnClickListener l) {
        mListener = l;
    }
    
    @Override
    public int getCount() {
        return mCameraInfoList.size();
    }

    @Override
    public EZDeviceInfo getItem(int position) {
        EZDeviceInfo item = null;
        if (position >= 0 && getCount() > position) {
            item = mCameraInfoList.get(position);
        }
        return item;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 自定义视图
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cameralist_invite_device_item, null);
            viewHolder.iconIv = (ImageView) convertView.findViewById(R.id.item_icon);
            viewHolder.cameraNameTv = (TextView) convertView.findViewById(R.id.camera_name_tv);
            viewHolder.inviteDeviceBtn = (Button) convertView.findViewById(R.id.btn_invite_device);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        
        viewHolder.inviteDeviceBtn.setTag(position);

        final EZDeviceInfo deviceInfo = getItem(position);
        if (deviceInfo != null){
            if (deviceInfo.getStatus() == 2) {
                viewHolder.inviteDeviceBtn.setEnabled(false);
            } else {
                viewHolder.inviteDeviceBtn.setEnabled(true);
            }
            viewHolder.cameraNameTv.setText(deviceInfo.getDeviceSerial());
            viewHolder.iconIv.setVisibility(View.VISIBLE);
            String imageUrl = deviceInfo.getDeviceCover();
            if(!TextUtils.isEmpty(imageUrl)) {
                Glide.with(mContext).load(imageUrl).placeholder(R.drawable.device_other).into(viewHolder.iconIv);
            }
            viewHolder.inviteDeviceBtn.setOnClickListener(v ->
                    mListener.onInviteDeviceClick(deviceInfo)
            );
        }
        return convertView;
    }
    
    public interface OnClickListener {
        public void onInviteDeviceClick(EZDeviceInfo mEZDeviceInfo);
    }
}
