package com.videogo.ui.cameralist;

import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;

import ezviz.ezopensdk.R;

public class SelectCameraDialog extends DialogFragment {

    private ListView mListView;

    private EZDeviceInfo mEZDeviceInfo;

    private LayoutInflater mInflater;

    private CameraItemClick mCameraItemClick;

    public void setCameraItemClick(CameraItemClick cameraItemClick) {
        mCameraItemClick = cameraItemClick;
    }

    public CameraItemClick getCameraItemClick() {
        return mCameraItemClick;
    }

    public SelectCameraDialog() {

    }

    public interface CameraItemClick {
        public void onCameraItemClick(EZDeviceInfo deviceInfo, int camera_index);
    }

    public void setEZDeviceInfo(EZDeviceInfo EZDeviceInfo) {
        mEZDeviceInfo = EZDeviceInfo;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mInflater = inflater;
        View view = inflater.inflate(R.layout.select_camera_no_dialog, container, false);
        mListView = (ListView) view.findViewById(R.id.list_camera);
        MyAdatpter myAdatpter = new MyAdatpter();
        mListView.setAdapter(myAdatpter);
        mListView.setOnItemClickListener((parent, view1, position, id) -> {
            EZCameraInfo ezCameraInfo = getCameraInfo(position);
            if (!ezCameraInfo.isCamera()) {// 不是视频类设备|is not camera device
                Toast.makeText(getActivity(), R.string.not_camera_device, Toast.LENGTH_SHORT).show();
                return;
            }
            if (mCameraItemClick != null) {
                mCameraItemClick.onCameraItemClick(mEZDeviceInfo, position);
            }
            dismiss();
        });
        return view;
    }


    class MyAdatpter extends BaseAdapter {
        @Override
        public int getCount() {
            if (mEZDeviceInfo != null && mEZDeviceInfo.getCameraInfoList() != null && mEZDeviceInfo.getCameraInfoList().size() > 0) {
                return mEZDeviceInfo.getCameraInfoList().size();
            } else if (mEZDeviceInfo != null && mEZDeviceInfo.getSubDeviceInfoList() != null && mEZDeviceInfo.getSubDeviceInfoList().size() > 0) {
                return mEZDeviceInfo.getSubDeviceInfoList().size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return getCameraInfo(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.select_camera_no_dialog_item, null);
                holder = new ViewHolder();
                holder.mCameraNoTV = (TextView) convertView.findViewById(R.id.text_camerano);
                holder.mCameraNameTV = (TextView) convertView.findViewById(R.id.text_camera_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 获取通道信息 或 子设备信息 | get camera info
            EZCameraInfo ezCameraInfo = getCameraInfo(position);
            if (ezCameraInfo != null) {
                holder.mCameraNoTV.setText(String.valueOf(ezCameraInfo.getCameraNo()));
                holder.mCameraNameTV.setText(TextUtils.isEmpty(ezCameraInfo.getCameraName()) ?
                        getResources().getString(R.string.unnamed) : ezCameraInfo.getCameraName());
            }
            return convertView;
        }
    }

    class ViewHolder {
        TextView mCameraNoTV;
        TextView mCameraNameTV;
    }

    private EZCameraInfo getCameraInfo(int position) {
        if (mEZDeviceInfo != null && mEZDeviceInfo.getCameraInfoList() != null && mEZDeviceInfo.getCameraInfoList().size() > 0) {
            return mEZDeviceInfo.getCameraInfoList().get(position);
        } else if (mEZDeviceInfo != null && mEZDeviceInfo.getSubDeviceInfoList() != null && mEZDeviceInfo.getSubDeviceInfoList().size() > 0) {
            return mEZDeviceInfo.getSubDeviceInfoList().get(position);
        }

        return null;
    }

}


