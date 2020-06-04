package com.videogo.ui.LanDevice;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.ezviz.hcnetsdk.EZLoginDeviceInfo;
import ezviz.ezopensdk.R;
import java.util.ArrayList;

public class SelectLandeviceDialog extends DialogFragment{

    private ListView mListView;

    private EZLoginDeviceInfo mLoginDeviceInfo;

    private MyAdatpter mMyAdatpter;

    private LayoutInflater mInflater;

    private CameraItemClick mCameraItemClick;

    private ArrayList<Integer> mChannelNoList = new ArrayList<Integer>();

    public void setCameraItemClick(CameraItemClick cameraItemClick) {
        mCameraItemClick = cameraItemClick;
    }

    public CameraItemClick getCameraItemClick() {
        return mCameraItemClick;
    }

    public SelectLandeviceDialog(){

    }

    public interface CameraItemClick {
        public void onCameraItemClick(int playChannelNo);
    }

    public void setLoginDeviceInfo(EZLoginDeviceInfo ezLoginDeviceInfo) {
        mLoginDeviceInfo = ezLoginDeviceInfo;

        //Add a channel
        int chanelCount = mLoginDeviceInfo.getByChanNum();
        int chanelStart = mLoginDeviceInfo.getByStartChan();

        //channelNo
        for (int i = 0; i < chanelCount; i++) {
            mChannelNoList.add(chanelStart + i);
        }

        //ipc channelNo
        for (int i = 0; i < mLoginDeviceInfo.getByIPChanNum(); i++) {
            mChannelNoList.add(mLoginDeviceInfo.getByStartDChan() + i);
        }

        if (mMyAdatpter != null){
            mMyAdatpter.notifyDataSetChanged();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mInflater = inflater;
        View view = inflater.inflate(R.layout.select_camera_no_dialog, container,false);
        mListView = (ListView) view.findViewById(R.id.list_camera);
        MyAdatpter myAdatpter = new MyAdatpter();
        mListView.setAdapter(myAdatpter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCameraItemClick != null){
                    mCameraItemClick.onCameraItemClick(position);
                }
                dismiss();
            }
        });
        return view;
    }


    class MyAdatpter extends BaseAdapter{
        @Override
        public int getCount() {
            if (mChannelNoList == null || mChannelNoList.size() <= 0){
                return 0;
            }
            return mChannelNoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mChannelNoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = mInflater.inflate(R.layout.select_camera_no_dialog_item, null);
                holder = new ViewHolder();
                holder.mCameraNoTV = (TextView) convertView.findViewById(R.id.text_camerano);
                holder.mCameraNameTV = (TextView) convertView.findViewById(R.id.text_camera_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mCameraNoTV.setText(String.valueOf(mChannelNoList.get(position)));
            holder.mCameraNameTV.setText("Camera " + mChannelNoList.get(position));
            return convertView;
        }
    }

    class ViewHolder{
        TextView mCameraNoTV;
        TextView mCameraNameTV;
    }
}


