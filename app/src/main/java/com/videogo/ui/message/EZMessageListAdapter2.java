package com.videogo.ui.message;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.ui.util.DataManager;
import com.videogo.ui.util.EZUtils;
import com.videogo.ui.util.VerifyCodeInput;
import com.videogo.widget.PinnedSectionListView.PinnedSectionListAdapter;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ezviz.ezopensdk.R;

//import com.videogo.leavemessage.LeaveMessageItem;

public class EZMessageListAdapter2 extends BaseAdapter implements View.OnClickListener, OnCreateContextMenuListener,
        OnCheckedChangeListener, PinnedSectionListAdapter {



    public static final int MENU_DEL_ID = Menu.FIRST + 1;
    public static final int MENU_MORE_ID = Menu.FIRST + 2;

    private AlertDialog mAlertDialog;
    private MyVerifyCodeInputListener mMyVerifyCodeInputListener;
    private VerifyCodeInput.VerifyCodeErrorListener mMyVerifyCodeErrorListener;

    private boolean isShowVerifyCodeDialog = true;

    private class ViewHolder {
        CheckBox check;
        TextView timeText;
        ImageView image;
        TextView fromTip;
        TextView from;
        TextView type;
        ViewGroup layout;
        ImageView unread;
    }

    private final DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final String[] mWeekdayNames = DateFormatSymbols.getInstance(Locale.getDefault()).getWeekdays();

    private Context mContext;
    private List<Object> mObjects;
    private Map<String, Boolean> mCheckStates = new HashMap<String, Boolean>();

    private Calendar mLastDate;
    private String mDeviceSerial;


    private OnClickListener mOnClickListener;

    private boolean mCheckMode;
    private boolean mNoMenu;
    private boolean mDataSetChanged;


    private EZMessageListAdapter2(Context context, String deviceSerial, VerifyCodeInput.VerifyCodeErrorListener verifyCodeErrorListener) {
        mContext = context;
        mDeviceSerial = deviceSerial;
        mMyVerifyCodeInputListener = new MyVerifyCodeInputListener();
        mMyVerifyCodeErrorListener = verifyCodeErrorListener;
    }

    public EZMessageListAdapter2(Context context, List<? extends Object> list, String deviceSerial,VerifyCodeInput.VerifyCodeErrorListener verifyCodeErrorListener) {
        this(context, deviceSerial,verifyCodeErrorListener);
        mDeviceSerial = deviceSerial;
        setList(list);
    }

    public void setList(List<? extends Object> list) {
        if (list == null) {
            return;
        }
        List<Object> objects = new ArrayList<Object>();

        Map<String, Boolean> preCheckStates = mCheckStates;
        mCheckStates = new HashMap<String, Boolean>();

        mLastDate = null;
        Calendar tempDate = Calendar.getInstance();
        try {
//            if(list.size() == 1)//mj
//                return;
            for (Object item : list) {
//                String id = item instanceof LeaveMessageItem ? ((LeaveMessageItem) item).getMessageId()
//                        : ((EZAlarmInfo) item).getAlarmLogId();
//                String time = item instanceof LeaveMessageItem ? ((LeaveMessageItem) item).getCreateTime()
//                        : ((EZAlarmInfo) item).getAlarmStartTime();
                String id = ((EZAlarmInfo) item).getAlarmId();
                //mj String time = ((EZAlarmInfo) item).getAlarmStartTime();
                String time = ((EZAlarmInfo) item).getAlarmStartTime();

                try {
                tempDate.setTime(mDateFormat.parse(time));
                } catch (ParseException e) {
                    //tempDate.setTime(mDateFormat.parse(time));
                    e.printStackTrace();
                }
                if (mLastDate == null || !isSameDate(mLastDate, tempDate)) {
                    mLastDate = (Calendar) tempDate.clone();
                    objects.add(mLastDate);
                }
                objects.add(item);

                Boolean check = preCheckStates.get(id);
                if (check != null && check)
                    mCheckStates.put(id, true);
            }
        } catch (Exception e) {
        }

        mObjects = objects;
    }

    private boolean isSameDate(Calendar firstDate, Calendar secondDate) {
        return (firstDate.get(Calendar.DAY_OF_YEAR) == secondDate.get(Calendar.DAY_OF_YEAR) && firstDate
                .get(Calendar.YEAR) == secondDate.get(Calendar.YEAR));
    }

    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }

    public void setNoMenu(boolean noMenu) {
        mNoMenu = noMenu;
    }

    public void setCheckMode(boolean checkMode) {
        if (mCheckMode != checkMode) {
            mCheckMode = checkMode;
            if (!checkMode) {
                uncheckAll();
            }
        }
    }

    public boolean isCheckAll() {
        for (Object item : mObjects) {
            String id = null;
            if (item instanceof EZAlarmInfo)
                id = ((EZAlarmInfo) item).getAlarmId();
//            else if (item instanceof LeaveMessageItem)
//                id = ((LeaveMessageItem) item).getMessageId();

            if (id != null) {
                Boolean check = mCheckStates.get(id);
                if (check == null || !check)
                    return false;
            }
        }
        return true;
    }

    public void checkAll() {
        for (Object item : mObjects) {
            String id = null;
            if (item instanceof EZAlarmInfo)
                id = ((EZAlarmInfo) item).getAlarmId();
//            else if (item instanceof LeaveMessageItem)
//                id = ((LeaveMessageItem) item).getMessageId();

            if (id != null)
                mCheckStates.put(id, true);
        }
        notifyDataSetChanged();
    }

    public void uncheckAll() {
        mCheckStates.clear();
        notifyDataSetChanged();
    }

    public List<String> getCheckedIds() {
        List<String> ids = new ArrayList<String>();
        Set<Entry<String, Boolean>> entries = mCheckStates.entrySet();
        for (Entry<String, Boolean> entry : entries) {
            if (entry.getValue() != null && entry.getValue())
                ids.add(entry.getKey());
        }
        return ids;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (mObjects.get(position) instanceof Calendar) ? 0 : 1;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == 0;
    }

    @Override
    public Object getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        final ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();

            if (viewType == 0) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.ez_message_list_section, parent, false);

                viewHolder.timeText = (TextView) convertView.findViewById(R.id.message_time);

            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.ez_message_list_item, parent, false);

                viewHolder.check = (CheckBox) convertView.findViewById(R.id.message_check);
                viewHolder.timeText = (TextView) convertView.findViewById(R.id.message_time);
                viewHolder.layout = (ViewGroup) convertView.findViewById(R.id.message_layout);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.message_image);
                viewHolder.fromTip = (TextView) convertView.findViewById(R.id.message_from_tip);
                viewHolder.from = (TextView) convertView.findViewById(R.id.message_from);
                viewHolder.type = (TextView) convertView.findViewById(R.id.message_type);
                viewHolder.unread = (ImageView) convertView.findViewById(R.id.message_unread);

                viewHolder.layout.setOnCreateContextMenuListener(this);
                viewHolder.layout.setOnClickListener(this);
                viewHolder.check.setOnClickListener(this);
                viewHolder.check.setOnCheckedChangeListener(this);
            }

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (viewType == 0) {
            Calendar date = (Calendar) getItem(position);
            String displayText;
            if (isSameDate(date, Calendar.getInstance())) {
                displayText = mContext.getString(R.string.today);
            } else {
                displayText = (date.get(Calendar.MONTH) + 1) + mContext.getString(R.string.month)
                        + date.get(Calendar.DAY_OF_MONTH) + mContext.getString(R.string.day) + ' '
                        + mWeekdayNames[date.get(Calendar.DAY_OF_WEEK)];
            }
            viewHolder.timeText.setText(displayText);

        } else {
            viewHolder.layout.setTag(R.id.tag_key_position, position);
            viewHolder.check.setTag(R.id.tag_key_position, position);

            viewHolder.check.setVisibility(mCheckMode ? View.VISIBLE : View.GONE);

            Object item = getItem(position);

            if (item instanceof EZAlarmInfo) {
                EZAlarmInfo alarmLogInfo = (EZAlarmInfo) item;

                if (mCheckMode) {
                    Boolean checked = mCheckStates.get(alarmLogInfo.getAlarmId());
                    viewHolder.check.setChecked(checked == null ? false : checked);
                }

//                AlarmType alarmType = alarmLogInfo.getEnumAlarmType();
//
//                viewHolder.type.setText(alarmType == AlarmType.DOORBELL_ALARM ? alarmLogInfo.getSampleName() : mContext
//                        .getString(alarmType.getTextResId()));
                AlarmType alarmType = AlarmType.BODY_ALARM;

                viewHolder.type.setText(mContext.getString(alarmType.getTextResId()));

                viewHolder.from.setText(alarmLogInfo.getAlarmName());

                if (alarmLogInfo.getAlarmStartTime() != null)
                    viewHolder.timeText.setText(alarmLogInfo.getAlarmStartTime().split(" ")[1]);
                else
                    viewHolder.timeText.setText(null);

                viewHolder.unread.setVisibility(alarmLogInfo.getIsRead() == 0 ? View.VISIBLE : View.INVISIBLE);

                //mj AlarmLogInfo relAlarm = alarmLogInfo.getRelationAlarms();
                EZAlarmInfo relAlarm = null;
                //boolean detector_ipc_link = relAlarm.getEnumAlarmType() == AlarmType.DETECTOR_IPC_LINK;
                boolean detector_ipc_link = false;
                boolean alarm_has_camera = true;
                if (detector_ipc_link || alarm_has_camera) {
                    if (!mDataSetChanged) {
                        EZUtils.loadImage(mContext, viewHolder.image, alarmLogInfo, mMyVerifyCodeErrorListener);
                    }
                } else {
                    viewHolder.image.setBackgroundResource(R.drawable.message_a1_bg);
                    viewHolder.image.setImageResource(alarmType.getDrawableResId());
                }

            } /*else if (item instanceof LeaveMessageItem) {
                LeaveMessageItem leaveMessage = (LeaveMessageItem) item;

                if (mCheckMode) {
                    Boolean checked = mCheckStates.get(leaveMessage.getMessageId());
                    viewHolder.check.setChecked(checked == null ? false : checked);
                }

                // 消息类型
                viewHolder.type.setText(R.string.video_leave_message);

                // 消息来源
                viewHolder.from.setText(leaveMessage.getDeviceModel());

                // 消息时间
                if (leaveMessage.getCreateTime() != null)
                    viewHolder.timeText.setText(leaveMessage.getCreateTime().split(" ")[1]);
                else
                    viewHolder.timeText.setText(null);

                // 消息查看状态
                viewHolder.unread.setVisibility(leaveMessage.getStatus() == 0 ? View.VISIBLE : View.INVISIBLE);

                // 消息图片
                mImageLoader.cancelDisplayTask(viewHolder.image);
                viewHolder.image.setImageResource(R.drawable.message_f1);
                viewHolder.imageProgress.setVisibility(View.GONE);
            }*/
        }

        return convertView;
    }


    class MyVerifyCodeInputListener implements VerifyCodeInput.VerifyCodeInputListener{

        @Override
        public void onInputVerifyCode(String verifyCode) {
            DataManager.getInstance().setDeviceSerialVerifyCode(mDeviceSerial,verifyCode);
            notifyDataSetChanged();
        }
    }

    public void setVerifyCodeDialog(){
        DataManager.getInstance().setDeviceSerialVerifyCode(mDeviceSerial,null);
        if (isShowVerifyCodeDialog){
            isShowVerifyCodeDialog = false;
            if (mAlertDialog == null){
                mAlertDialog = VerifyCodeInput.VerifyCodeInputDialog(mContext,mMyVerifyCodeInputListener);
            }
            if (!mAlertDialog.isShowing()){
                mAlertDialog.show();
            }
        }
    }
    @Override
    public void onClick(View v) {
        int position;
        switch (v.getId()) {
            case R.id.message_layout:
                position = (Integer) v.getTag(R.id.tag_key_position);

                if (mCheckMode) {
                    CheckBox checkBox = (CheckBox) v.findViewById(R.id.message_check);
                    checkBox.toggle();
                    if (mOnClickListener != null)
                        mOnClickListener
                                .onCheckClick(EZMessageListAdapter2.this, checkBox, position, checkBox.isChecked());
                } else {
                    if (mOnClickListener != null)
                        mOnClickListener.onItemClick(EZMessageListAdapter2.this, v, position);
                }
                break;

            case R.id.message_check:
                position = (Integer) v.getTag(R.id.tag_key_position);
                boolean check = ((CheckBox) v).isChecked();

                if (mOnClickListener != null)
                    mOnClickListener.onCheckClick(EZMessageListAdapter2.this, v, position, check);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (!mCheckMode && !mNoMenu) {
            menu.add(Menu.NONE, MENU_DEL_ID, Menu.NONE, mContext.getString(R.string.delete));

            int position = (Integer) v.getTag(R.id.tag_key_position);
//            menu.add(Menu.NONE, MENU_MORE_ID, Menu.NONE, mContext.getString(R.string.tab_more));

            if (mOnClickListener != null)
                mOnClickListener.onItemLongClick(EZMessageListAdapter2.this, v, position);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int position = (Integer) buttonView.getTag(R.id.tag_key_position);
        Object item = getItem(position);
        if (item instanceof EZAlarmInfo)
            mCheckStates.put(((EZAlarmInfo) item).getAlarmId(), isChecked);
    }


    public interface OnClickListener {

        public void onCheckClick(BaseAdapter adapter, View view, int position, boolean checked);

        public void onItemLongClick(BaseAdapter adapter, View view, int position);

        public void onItemClick(BaseAdapter adapter, View view, int position);
    }
}