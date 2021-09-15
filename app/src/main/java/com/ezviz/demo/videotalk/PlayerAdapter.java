package com.ezviz.demo.videotalk;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import ezviz.ezopensdk.R;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.MyViewHolder> {
    private List<EZClientInfo> mDataList;

    private OnStatusChangedListener onStatusChangedListener;

    public void setOnStatusChangedListener(OnStatusChangedListener onStatusChangedListener) {
        this.onStatusChangedListener = onStatusChangedListener;
    }

    class TagSurfaceTextureListener implements TextureView.SurfaceTextureListener{

        private EZClientInfo clientInfo;


        TagSurfaceTextureListener(EZClientInfo ezClientInfo){
            this.clientInfo = ezClientInfo;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            if (onStatusChangedListener != null){
                onStatusChangedListener.onSurfaceSet(clientInfo.id, clientInfo.subscribeType, new Surface(surfaceTexture));
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    }

    PlayerAdapter(List<EZClientInfo> dataList){
        mDataList = dataList;
    }

    @NonNull
    @NotNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_player, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull MyViewHolder holder, int position) {
        EZClientInfo clientInfo = mDataList.get(position);
        holder.tvName.setText(clientInfo.name);
        if (holder.playerView.isAvailable()){
            if (onStatusChangedListener != null){
                onStatusChangedListener.onSurfaceSet(clientInfo.id, clientInfo.subscribeType, new Surface(holder.playerView.getSurfaceTexture()));
            }
        }else {
            holder.playerView.setSurfaceTextureListener(new TagSurfaceTextureListener(clientInfo));
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        public TextureView playerView;
        public TextView tvName;

        public MyViewHolder(View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.texture_view);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}
