package com.videogo.ui.videotalk.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.videogo.ui.videotalk.VideoTalkBean;
import com.ezviz.sdk.videotalk.sdk.EZMeetingCall;

import java.util.List;

public class VideoTalkAdapter extends RecyclerView.Adapter<VideoTalkAdapter.MyViewHolder>{

    //private List<View> clientList;
    private EZMeetingCall ezMeetingCall;
    private Context mContext;
    //    private List<String> userNameList;
//    private List<Integer> clientIdList;
    private List<VideoTalkBean> list;
    private View hostView;

    private List<VideoTalkBean> videoTalkBeanList;


    public VideoTalkAdapter(EZMeetingCall ezMeetingCall, Context mContext, List<VideoTalkBean> videoTalkBeanList, View hostView){
        //this.clientList = clientList;
        this.ezMeetingCall = ezMeetingCall;
        this.mContext = mContext;
        this.videoTalkBeanList = videoTalkBeanList;
        this.hostView = hostView;
    }

    public VideoTalkAdapter(EZMeetingCall ezMeetingCall, Context mContext, List<VideoTalkBean> videoTalkBeanList){
        //this.clientList = clientList;
        this.ezMeetingCall = ezMeetingCall;
        this.mContext = mContext;
        this.videoTalkBeanList = videoTalkBeanList;

    }

    public void setData(List<VideoTalkBean> videoTalkBeans ){
        videoTalkBeanList = videoTalkBeans;
//        notifyDataSetChanged();
        notifyItemInserted(videoTalkBeans.size()-1);
    }

    public void changeData(List<VideoTalkBean> videoTalkBeans, int position){
        videoTalkBeanList = videoTalkBeans;
//        notifyItemChanged(position, "aaa");
        notifyItemChanged(position);
    }

    public void deleteData(List<VideoTalkBean> videoTalkBeans, int position){
        videoTalkBeanList = videoTalkBeans;
        notifyItemRemoved(position);
    }

    //@NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(i==1){
            VideoTalkView videoTalkView = new VideoTalkView(mContext, ezMeetingCall);
            MyViewHolder myViewHolder = new MyViewHolder(videoTalkView, videoTalkView.getRootView());
            return myViewHolder;
        }else{
            MyViewHolder myViewHolder = new MyViewHolder(hostView);
            return myViewHolder;
//            return null;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        //1表示进入房间，-1准备表示退出房间，-2表示已经退出房间
        if(videoTalkBeanList.get(i).getIsJoinRoom()==1){
            myViewHolder.videoTalkView.reset();
            videoTalkBeanList.get(i).setIsJoinRoom(0);
        }else if (videoTalkBeanList.get(i).getIsJoinRoom()==-1){

            myViewHolder.videoTalkView.leaveRoom();
            videoTalkBeanList.get(i).setIsJoinRoom(-2);  //设置已经退出房间的标志
//            myViewHolder = null;
            return;
        }
        myViewHolder.videoTalkView.joinRoom(videoTalkBeanList.get(i).getClientId(), videoTalkBeanList.get(i).getUserName(), videoTalkBeanList.get(i).getVolume());

    }

//    @Override
//    public void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull List<Object> payloads) {
//        if(payloads==null){
//            onBindViewHolder(holder, position);
//        }else{
//            TextView usernameTv  = holder.itemView.findViewById(R.id.username_tv);
//            usernameTv.setText("音量:"+videoTalkBeanList.get(position).getVolume());
//        }
//    }

    @Override
    public int getItemViewType(int position) {
//        videoTalkBeans.get(position)
//        if(videoTalkBeanList.get(position).isHost()){
//            return 0;
//        }else{
        return 1;
//        }

    }


    @Override
    public int getItemCount() {
        return videoTalkBeanList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        public VideoTalkView videoTalkView;
        MyViewHolder(VideoTalkView videoTalkView, View view){
            super(view);
            this.videoTalkView = videoTalkView;

        }
        MyViewHolder(View view){
            super(view);
        }
    }
}
