package com.ezviz.demo.videotalk;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ezviz.videotalk.videomeeting.ConstVideoMeeting;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import ezviz.ezopensdk.R;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.MyViewHolder> {

    private List<EZClientInfo> mDataList;
    private OnStatusChangedListener onStatusChangedListener;

    ClientAdapter(List<EZClientInfo> dataList){
        mDataList = dataList;
    }

    public void setOnStatusChangedListener(OnStatusChangedListener onStatusChangedListener) {
        this.onStatusChangedListener = onStatusChangedListener;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_client, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        final EZClientInfo clientInfo = mDataList.get(position);
        holder.radioGroup.setVisibility(clientInfo.mVideoAvailable == 0 ? View.GONE : View.VISIBLE);
        holder.radioButtonBig.setVisibility((clientInfo.mVideoAvailable & 1) == 1 ? View.VISIBLE : View.GONE);
        holder.radioButtonSmall.setVisibility(clientInfo.mVideoAvailable == 5 ? View.VISIBLE : View.GONE);
        holder.radioGroup.setOnCheckedChangeListener(null);
        holder.radioButtonNone.setChecked(clientInfo.subscribeType == ConstVideoMeeting.StreamState.BAV_STREAM_INVALID);
        holder.radioButtonBig.setChecked(clientInfo.subscribeType == ConstVideoMeeting.StreamState.BAV_SUB_STREAM_BIG_VIDEO);
        holder.radioButtonSmall.setChecked(clientInfo.subscribeType == ConstVideoMeeting.StreamState.BAV_SUB_STREAM_MIN_VIDEO);
        holder.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (onStatusChangedListener != null){
                    ConstVideoMeeting.StreamState type = ConstVideoMeeting.StreamState.BAV_STREAM_INVALID;
                    switch (checkedId){
                        case R.id.rb_big:
                            type = ConstVideoMeeting.StreamState.BAV_SUB_STREAM_BIG_VIDEO;
                            break;
                        case R.id.rb_small:
                            type = ConstVideoMeeting.StreamState.BAV_SUB_STREAM_MIN_VIDEO;
                            break;
                        case R.id.rb_none:
                            break;
                    }
//      
                    onStatusChangedListener.onSurfaceSet((Integer) group.getTag(), clientInfo.subscribeType, null);
                    clientInfo.subscribeType = type;
                    onStatusChangedListener.onSubscribe((Integer) group.getTag());
                }
            }
        });
        holder.radioGroup.setTag(clientInfo.id);

        StringBuffer sb = new StringBuffer();
        sb.append(clientInfo.id);
        if (!TextUtils.isEmpty(clientInfo.name)){
            sb.append("-");
            sb.append(clientInfo.name);
        }
        if (clientInfo.mAudioAvailable == 1){
            sb.append("[说话中][音量");
            sb.append(String.valueOf(clientInfo.volume));
            sb.append("]");
        }

        sb.append("[网络");
        sb.append(clientInfo.netQuality.getDesc());
        sb.append("]");
        holder.tvName.setText(sb.toString());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView tvName;
        public RadioGroup radioGroup;
        public RadioButton radioButtonBig;
        public RadioButton radioButtonSmall;
        public RadioButton radioButtonNone;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            radioButtonBig = itemView.findViewById(R.id.rb_big);
            radioButtonSmall = itemView.findViewById(R.id.rb_small);
            radioButtonNone = itemView.findViewById(R.id.rb_none);
            radioGroup = itemView.findViewById(R.id.radio_group);
        }
    }

}
