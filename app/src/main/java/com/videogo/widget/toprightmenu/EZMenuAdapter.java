package com.videogo.widget.toprightmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ezviz.ezopensdk.R;

public class EZMenuAdapter extends RecyclerView.Adapter<EZMenuAdapter.TRMViewHolder> {
    private Context mContext;
    private List<EZMenuItem> menuItemList;
    private boolean showIcon;
    private EZTopRightMenu mTopRightMenu;
    private EZTopRightMenu.OnMenuItemClickListener onMenuItemClickListener;
    private TRMViewHolder previousHolder;

    public EZMenuAdapter(Context context, EZTopRightMenu topRightMenu, List<EZMenuItem> menuItemList, boolean show) {
        this.mContext = context;
        this.mTopRightMenu = topRightMenu;
        this.menuItemList = menuItemList;
        this.showIcon = show;
    }

    public void setData(List<EZMenuItem> data) {
        menuItemList = data;
        notifyDataSetChanged();
    }

    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        notifyDataSetChanged();
    }

    @Override
    public TRMViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.trm_item_popup_menu_list, parent, false);
        return new TRMViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TRMViewHolder holder, int position) {
        final EZMenuItem menuItem = menuItemList.get(position);
        if (showIcon) {
            holder.icon.setVisibility(View.VISIBLE);
            int resId = menuItem.getIcon();
            holder.icon.setImageResource(resId < 0 ? 0 : resId);
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        holder.text.setText(menuItem.getText());

        if (position == 0) {
            holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_top_pressed));
        } else if (position == menuItemList.size() - 1) {
            holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_bottom_pressed));
        } else {
            holder.container.setBackgroundDrawable(addStateDrawable(mContext, -1, R.drawable.trm_popup_middle_pressed));
        }
        if (previousHolder == null && position == 0) {
            holder.container.setSelected(true);
            previousHolder = holder;
        }
        final int pos = holder.getAdapterPosition();
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousHolder != null) {
                    previousHolder.container.setSelected(false);
                }
                previousHolder = holder;
                holder.container.setSelected(true);
                if (onMenuItemClickListener != null) {
                    mTopRightMenu.dismiss();
                    onMenuItemClickListener.onMenuItemClick(pos);
                }
            }
        });
    }

    private StateListDrawable addStateDrawable(Context context, int normalId, int pressedId) {
        StateListDrawable sd = new StateListDrawable();
        Drawable normal = normalId == -1 ? null : context.getResources().getDrawable(normalId);
        Drawable pressed = pressedId == -1 ? null : context.getResources().getDrawable(pressedId);
        sd.addState(new int[]{android.R.attr.state_pressed}, pressed);
        sd.addState(new int[]{android.R.attr.state_selected}, pressed);
        sd.addState(new int[]{}, normal);
        return sd;
    }

    @Override
    public int getItemCount() {
        return menuItemList == null ? 0 : menuItemList.size();
    }

    class TRMViewHolder extends RecyclerView.ViewHolder {
        ViewGroup container;
        ImageView icon;
        TextView text;

        TRMViewHolder(View itemView) {
            super(itemView);
            container = (ViewGroup) itemView;
            icon = (ImageView) itemView.findViewById(R.id.trm_menu_item_icon);
            text = (TextView) itemView.findViewById(R.id.trm_menu_item_text);
        }
    }

    public void setOnMenuItemClickListener(EZTopRightMenu.OnMenuItemClickListener listener) {
        this.onMenuItemClickListener = listener;
    }
}
