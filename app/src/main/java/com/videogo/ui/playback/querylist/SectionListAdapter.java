package com.videogo.ui.playback.querylist;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.ui.playback.RemoteListContant;
import com.videogo.ui.playback.RemoteListUtil;
import com.videogo.ui.playback.bean.ClickedListItem;
import com.videogo.ui.playback.bean.CloudPartInfoFileEx;
import com.videogo.util.DataManager;
import com.videogo.util.EZUtils;
import com.videogo.util.Utils;
import com.videogo.widget.MySectionIndexer;
import com.videogo.widget.PinnedHeaderListView;
import com.videogo.widget.PinnedHeaderListView.PinnedHeaderAdapter;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ezviz.ezopensdk.R;

public class SectionListAdapter implements ListAdapter, OnItemClickListener, PinnedHeaderAdapter, SectionIndexer,
        OnScrollListener {
    private SectionIndexer mIndexer;
    private String[] mSections;
    private int[] mCounts;
    private int mSectionCounts = 0;
    // 当前选中的item，默认没选中
    private int selPosition = -1;
    // 本地是否展开
    private boolean isExpand = false;
    // 是否编辑状态
    private boolean isEdit = false;

    private OnHikItemClickListener onHikItemClickListener;
    // 选中要删除的云视频文件
    private HashMap<String, String> selectedCloudFiles = new HashMap<String, String>();

    private Context mContext;
    private String mDeviceSerial;

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateTotalCount();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            updateTotalCount();
        };
    };

    private final StandardArrayAdapter linkedAdapter;
    private final Map<String, View> currentViewSections = new HashMap<String, View>();
    private int viewTypeCount;
    protected final LayoutInflater inflater;

    // private View transparentSectionView;

    private OnItemClickListener linkedListener;

    public SectionListAdapter(Context context,final LayoutInflater inflater, final StandardArrayAdapter linkedAdapter,String deviceSerial) {
        this.linkedAdapter = linkedAdapter;
        this.inflater = inflater;
        mContext = context;
        mDeviceSerial = deviceSerial;
        linkedAdapter.registerDataSetObserver(dataSetObserver);

        updateTotalCount();
    }

    private boolean isTheSame(final String previousSection, final String newSection) {
        if (previousSection == null) {
            return newSection == null;
        } else {
            return previousSection.equals(newSection);
        }
    }

    private void fillSections() {
        mSections = new String[mSectionCounts];
        mCounts = new int[mSectionCounts];
        final int count = linkedAdapter.getCount();
        String currentSection = null;
        int newSectionIndex = 0;
        int newSectionCounts = 0;
        String previousSection = null;
        for (int i = 0; i < count; i++) {
            newSectionCounts++;
            currentSection = linkedAdapter.items.get(i).getHeadHour();
            if (currentSection == null) {
                continue;
            }
            if (!isTheSame(previousSection, currentSection)) {
                mSections[newSectionIndex] = currentSection;
                previousSection = currentSection;
                if (newSectionIndex == 1) {
                    mCounts[0] = newSectionCounts - 1;
                } else if (newSectionIndex != 0) {
                    mCounts[newSectionIndex - 1] = newSectionCounts;
                }
                if (i != 0) {
                    newSectionCounts = 0;
                }
                newSectionIndex++;
            } else if (i == count - 1) {
                mCounts[newSectionIndex - 1] = newSectionCounts + 1;
            }

        }
        if (mIndexer != null) {
            mIndexer = null;
        }
        mIndexer = new MySectionIndexer(mSections, mCounts);
    }

    private synchronized void updateTotalCount() {
        mSectionCounts = 0;
        String currentSection = null;
        viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
        final int count = linkedAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final CloudPartInfoFileEx item = (CloudPartInfoFileEx) linkedAdapter.getItem(i);
            if (item != null && !isTheSame(currentSection, item.getHeadHour())) {
                mSectionCounts++;
                currentSection = item.getHeadHour();
            }
        }
        fillSections();
    }

    @Override
    public synchronized int getCount() {
        return linkedAdapter.getCount();
    }

    @Override
    public synchronized Object getItem(final int position) {
        final int linkedItemPosition = getLinkedPosition(position);
        return linkedAdapter.getItem(linkedItemPosition);
    }

    public synchronized String getSectionName(final int position) {
        return null;
    }

    @Override
    public long getItemId(final int position) {
        return linkedAdapter.getItemId(getLinkedPosition(position));
    }

    protected Integer getLinkedPosition(final int position) {
        return position;
    }

    @Override
    public int getItemViewType(final int position) {
        return linkedAdapter.getItemViewType(getLinkedPosition(position));
    }

    protected void setSectionText(final String section, final View sectionView) {
        final TextView textView = (TextView) sectionView.findViewById(R.id.header);
        textView.setText(section);
    }

    protected synchronized void replaceSectionViewsInMaps(final String section, final View theView) {
        if (currentViewSections.containsKey(section)) {
            currentViewSections.remove(section);
        }
        currentViewSections.put(section, theView);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = inflater.inflate(R.layout.section_list_item, null);
            holder.header = (TextView) convertView.findViewById(R.id.header);

            holder.image1 = (ImageView) convertView.findViewById(R.id.image1);
            holder.image2 = (ImageView) convertView.findViewById(R.id.image2);
            holder.image3 = (ImageView) convertView.findViewById(R.id.image3);
            holder.image1.setDrawingCacheEnabled(false);
            holder.image1.setWillNotCacheDrawing(true);
            holder.image2.setDrawingCacheEnabled(false);
            holder.image2.setWillNotCacheDrawing(true);
            holder.image3.setDrawingCacheEnabled(false);
            holder.image3.setWillNotCacheDrawing(true);

            holder.text1 = (TextView) convertView.findViewById(R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(R.id.text2);
            holder.text3 = (TextView) convertView.findViewById(R.id.text3);

            holder.layout1 = (LinearLayout) convertView.findViewById(R.id.local_l1);
            holder.layout2 = (LinearLayout) convertView.findViewById(R.id.local_l2);
            holder.layout3 = (LinearLayout) convertView.findViewById(R.id.local_l3);

            holder.localTimePart1 = (TextView) convertView.findViewById(R.id.local_time_part1);
            holder.localTimePart2 = (TextView) convertView.findViewById(R.id.local_time_part2);
            holder.localTimePart3 = (TextView) convertView.findViewById(R.id.local_time_part3);

            holder.localAlarm1 = (ImageView) convertView.findViewById(R.id.local_alarm1);
            holder.localAlarm2 = (ImageView) convertView.findViewById(R.id.local_alarm2);
            holder.localAlarm3 = (ImageView) convertView.findViewById(R.id.local_alarm3);

            holder.check1 = (CheckBox) convertView.findViewById(R.id.check1);
            holder.check2 = (CheckBox) convertView.findViewById(R.id.check2);
            holder.check3 = (CheckBox) convertView.findViewById(R.id.check3);

            holder.moreBtn = (TextView) convertView.findViewById(R.id.more_btn);
            holder.moreLayout = (LinearLayout) convertView.findViewById(R.id.layout_more_ly);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        // int resId = isExpand ? R.drawable.playback_more_up1 : R.drawable.playback_more_down1;
        // Drawable drawable = linkedAdapter.getContext().getResources().getDrawable(resId);
        // drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        // holder.moreBtn.setCompoundDrawables(null, null, drawable, null);
        CloudPartInfoFileEx fileEx = linkedAdapter.items.get(position);
        if (fileEx.isMore()) {
            convertView.findViewById(R.id.header_parent).setVisibility(View.GONE);
            convertView.findViewById(R.id.layout).setVisibility(View.GONE);
            convertView.findViewById(R.id.header).setVisibility(View.GONE);
            holder.moreBtn.setVisibility(View.VISIBLE);
            holder.moreLayout.setVisibility(View.VISIBLE);
            if (isEdit) {
                holder.moreBtn.setText(R.string.local_video_not_delete);
                holder.moreBtn.setTextColor(inflater.getContext().getResources().getColor(R.color.common_hint_text));
                holder.moreLayout.setOnClickListener(null);
                int resId = R.drawable.put_ico;
                Drawable drawable = linkedAdapter.getContext().getResources().getDrawable(resId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.moreBtn.setCompoundDrawables(null, null, drawable, null);
            } else {
                holder.moreBtn.setText(R.string.more_local_image);
                holder.moreBtn.setTextColor(inflater.getContext().getResources().getColor(R.color.more_color));
                holder.moreLayout.setOnClickListener(new OnHikMoreClickListener(holder.moreBtn));
                int resId = isExpand ? R.drawable.playback_more_up1 : R.drawable.playback_more_down1;
                Drawable drawable = linkedAdapter.getContext().getResources().getDrawable(resId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                holder.moreBtn.setCompoundDrawables(null, null, drawable, null);
            }
            return convertView;
        } else {
            convertView.findViewById(R.id.header_parent).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.layout).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.header).setVisibility(View.VISIBLE);
            holder.moreBtn.setVisibility(View.GONE);
            holder.moreLayout.setVisibility(View.GONE);
        }
        final CloudPartInfoFile dataOne = fileEx.getDataOne();
        final CloudPartInfoFile dataTwo = fileEx.getDataTwo();
        final CloudPartInfoFile dataThree = fileEx.getDataThree();
        String headHour = fileEx.getHeadHour();
        if (headHour != null) {
            holder.header.setText(getSpannableString(headHour));
        }
        if (dataOne != null) {
            if (dataOne.getBitmap() != null) {
                holder.image1.setImageBitmap(dataOne.getBitmap());
            } else {
                holder.image1.setImageResource(R.drawable.playback_cover2);
            }
            if (dataOne.isCloud()) {
                loadCoverPic(dataOne, holder.image1);
                if (isEdit) {
                    holder.check1.setVisibility(View.VISIBLE);
                } else {
                    holder.check1.setVisibility(View.GONE);
                }
            } else {
                holder.check1.setVisibility(View.GONE);
            }
            holder.text1.setVisibility(View.VISIBLE);
            holder.text1.setText(getDate(Utils.convert14Calender(dataOne.getStartTime())));
            holder.layout1.setVisibility(View.GONE);
            if (dataOne.getPosition() == selPosition && !isEdit) {
                holder.image1.setSelected(true);
            } else {
                holder.image1.setSelected(false);
            }
            holder.image1.setOnClickListener(new OnHikClickListener(dataOne, position, holder.check1));
            holder.image1.setVisibility(View.VISIBLE);
            holder.check1.setOnCheckedChangeListener(new OnHikCheckedChangeListener(dataOne));
            if (selectedCloudFiles.containsKey(dataOne.getFileId())) {
                holder.check1.setChecked(true);
            } else {
                holder.check1.setChecked(false);
            }
        } else {
            holder.image1.setVisibility(View.GONE);
            holder.text1.setVisibility(View.GONE);
            holder.layout1.setVisibility(View.GONE);
            holder.check1.setVisibility(View.GONE);
        }

        if (dataTwo != null) {
            if (dataTwo.getBitmap() != null) {
                holder.image2.setImageBitmap(dataTwo.getBitmap());
            } else {
                holder.image2.setImageResource(R.drawable.playback_cover2);
            }
            if (dataTwo.isCloud()) {
                loadCoverPic(dataTwo, holder.image2);
                if (isEdit) {
                    holder.check2.setVisibility(View.VISIBLE);
                } else {
                    holder.check2.setVisibility(View.GONE);
                }
            } else {
                holder.check2.setVisibility(View.GONE);
            }
            holder.text2.setVisibility(View.VISIBLE);
            holder.text2.setText(getDate(Utils.convert14Calender(dataTwo.getStartTime())));
            holder.layout2.setVisibility(View.GONE);
            holder.image2.setVisibility(View.VISIBLE);
            holder.image2.setOnClickListener(new OnHikClickListener(dataTwo, position, holder.check2));
            holder.check2.setOnCheckedChangeListener(new OnHikCheckedChangeListener(dataTwo));
            if (selectedCloudFiles.containsKey(dataTwo.getFileId())) {
                holder.check2.setChecked(true);
            } else {
                holder.check2.setChecked(false);
            }
            if (dataTwo.getPosition() == selPosition && !isEdit) {
                holder.image2.setSelected(true);
            } else {
                holder.image2.setSelected(false);
            }
        } else {
            holder.image2.setVisibility(View.GONE);
            holder.text2.setVisibility(View.GONE);
            holder.layout2.setVisibility(View.GONE);
            holder.check2.setVisibility(View.GONE);
        }

        if (dataThree != null) {
            if (dataThree.getBitmap() != null) {
                holder.image3.setImageBitmap(dataThree.getBitmap());
            } else {
                holder.image3.setImageResource(R.drawable.playback_cover2);
            }
            if (dataThree.isCloud()) {
                loadCoverPic(dataThree, holder.image3);
                if (isEdit) {
                    holder.check3.setVisibility(View.VISIBLE);
                } else {
                    holder.check3.setVisibility(View.GONE);
                }
            } else {
                holder.check3.setVisibility(View.GONE);
            }
            holder.text3.setVisibility(View.VISIBLE);
            holder.text3.setText(getDate(Utils.convert14Calender(dataThree.getStartTime())));
            holder.layout3.setVisibility(View.GONE);
            holder.image3.setVisibility(View.VISIBLE);
            holder.image3.setOnClickListener(new OnHikClickListener(dataThree, position, holder.check3));
            holder.check3.setOnCheckedChangeListener(new OnHikCheckedChangeListener(dataThree));
            if (selectedCloudFiles.containsKey(dataThree.getFileId())) {
                holder.check3.setChecked(true);
            } else {
                holder.check3.setChecked(false);
            }
            if (dataThree.getPosition() == selPosition && !isEdit) {
                holder.image3.setSelected(true);
            } else {
                holder.image3.setSelected(false);
            }
        } else {
            holder.image3.setVisibility(View.GONE);
            holder.text3.setVisibility(View.GONE);
            holder.layout3.setVisibility(View.GONE);
            holder.check3.setVisibility(View.GONE);
        }

        int section = getSectionForPosition(position);
        int indexPosition = getPositionForSection(section);
        // 如果是分组所在行则显示标题
        if (indexPosition == position) {
            // 显示标题
            holder.header.setVisibility(View.VISIBLE);
        } else {
            // 隐藏标题
            holder.header.setVisibility(View.GONE);
        }

        return convertView;
    }

    private SpannableString getSpannableString(String headHour) {
        headHour = headHour.replace("~", "");
        SpannableString msp = new SpannableString(headHour);
        msp.setSpan(new RelativeSizeSpan(1.7f), 0, headHour.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return msp;
    }

    private void loadCoverPic(CloudPartInfoFile cloudPartInfoFile, ImageView imageView) {
            String cloudListItemPicUrl = RemoteListUtil.getCloudListItemPicUrl(cloudPartInfoFile.getPicUrl(),
                    cloudPartInfoFile.getKeyCheckSum(), DataManager.getInstance().getDeviceSerialVerifyCode(mDeviceSerial));
            imageLoader(cloudListItemPicUrl, imageView);
    }

    private void imageLoader(String picUrl, ImageView imageView) {
        EZAlarmInfo alarmInfo = new EZAlarmInfo();
        alarmInfo.setDeviceSerial(mDeviceSerial);
        alarmInfo.setAlarmPicUrl(picUrl);
        alarmInfo.setCrypt(1);
        EZUtils.loadImage(mContext,imageView,alarmInfo,null);
    }


    class OnHikCheckedChangeListener implements OnCheckedChangeListener {

        private CloudPartInfoFile cloudFile;

        public OnHikCheckedChangeListener(CloudPartInfoFile cloudFile) {
            this.cloudFile = cloudFile;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (!selectedCloudFiles.containsKey(cloudFile.getFileId())) {
                    selectedCloudFiles.put(cloudFile.getFileId(), cloudFile.getFileId());
                }
            } else {
                if (selectedCloudFiles.containsKey(cloudFile.getFileId())) {
                    selectedCloudFiles.remove(cloudFile.getFileId());
                }
            }

            if (onHikItemClickListener != null) {
                onHikItemClickListener.onSelectedChangeListener(selectedCloudFiles.size());
            }
        }
    }

    class OnHikClickListener implements OnClickListener {

        private CloudPartInfoFile cloudFile;

        private ClickedListItem clickedListItem;

        private CheckBox checkBox;

        public OnHikClickListener(CloudPartInfoFile cloudFile, int position, CheckBox checkBox) {
            this.cloudFile = cloudFile;
            this.checkBox = checkBox;
            int type = cloudFile.isCloud() ? RemoteListContant.TYPE_CLOUD : RemoteListContant.TYPE_LOCAL;
            clickedListItem = new ClickedListItem(cloudFile.getPosition(), type, cloudFile.getStartMillis(),
                    cloudFile.getEndMillis(), position);
            clickedListItem.setFileSize(cloudFile.getFileSize());
        }

        @Override
        public void onClick(View v) {
            if (isEdit) {
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                } else {
                    checkBox.setChecked(true);
                }
            } else {
                if (onHikItemClickListener != null) {
                    onHikItemClickListener.onHikItemClickListener(cloudFile, clickedListItem);
                }
            }
        }

    }

    class OnHikMoreClickListener implements OnClickListener {

        private TextView moreBtn;

        private int resId;

        public OnHikMoreClickListener(TextView moreBtn) {
            this.moreBtn = moreBtn;
        }

        @Override
        public void onClick(View v) {
            if (onHikItemClickListener != null) {
                isExpand = isExpand ? false : true;
                resId = isExpand ? R.drawable.playback_more_up1 : R.drawable.playback_more_down1;
                Drawable drawable = linkedAdapter.getContext().getResources().getDrawable(resId);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                moreBtn.setCompoundDrawables(null, null, drawable, null);
                onHikItemClickListener.onHikMoreClickListener(isExpand);
            }
        }

    }

    public HashMap<String, String> getSelectedCloudFiles() {
        return selectedCloudFiles;
    }

    public void clearAllSelectedCloudFiles() {
        selectedCloudFiles.clear();
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
    }

    public interface OnHikItemClickListener {
        void onHikItemClickListener(CloudPartInfoFile cloudFile, ClickedListItem clickedListItem);

        void onHikMoreClickListener(boolean isExpand);

        void onSelectedChangeListener(int total);
    }

    private String getDate(Calendar calender) {
        int hour = calender.get(Calendar.HOUR_OF_DAY);
        int minute = calender.get(Calendar.MINUTE);
        StringBuffer sb = new StringBuffer();
        if (hour < 10) {
            sb.append("0").append(hour);
        } else {
            sb.append(hour);
        }
        sb.append(":");
        if (minute < 10) {
            sb.append("0").append(minute);
        } else {
            sb.append(minute);
        }
        return sb.toString();
    }

    public void setSelection(int position) {
        selPosition = position;
        linkedAdapter.notifyDataSetChanged();
    }

    class Holder {
        TextView header;

        ImageView image1;
        TextView text1;
        LinearLayout layout1;
        TextView localTimePart1;
        ImageView localAlarm1;
        CheckBox check1;

        ImageView image2;
        TextView text2;
        LinearLayout layout2;
        TextView localTimePart2;
        ImageView localAlarm2;
        CheckBox check2;

        ImageView image3;
        TextView text3;
        LinearLayout layout3;
        TextView localTimePart3;
        ImageView localAlarm3;
        CheckBox check3;

        TextView moreBtn;
        LinearLayout moreLayout;
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeCount;
    }

    @Override
    public boolean hasStableIds() {
        return linkedAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return linkedAdapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return linkedAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(final int position) {
        return linkedAdapter.isEnabled(getLinkedPosition(position));
    }

    public int getRealPosition(int pos) {
        return pos - 1;
    }

    // public synchronized View getTransparentSectionView() {
    // if (transparentSectionView == null) {
    // // transparentSectionView = createNewSectionView();
    // }
    // return transparentSectionView;
    // }

    protected void sectionClicked(final String section) {
        // do nothing
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        if (linkedListener != null) {
            linkedListener.onItemClick(parent, view, getLinkedPosition(position), id);
        }

    }

    public void setOnItemClickListener(final OnItemClickListener linkedListener) {
        this.linkedListener = linkedListener;
    }

    @Override
    public int getPinnedHeaderState(int position) {
        int realPosition = position;

        if (mIndexer == null) {
            return PINNED_HEADER_GONE;
        }
        if (realPosition < 0) {
            return PINNED_HEADER_GONE;
        }
        int section = getSectionForPosition(realPosition);
        int nextSectionPosition = getPositionForSection(section + 1);
        if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {

        int realPosition = position;
        int section = getSectionForPosition(realPosition);
        if (section < 0) {
            header.findViewById(R.id.header_text).setVisibility(View.GONE);
            header.findViewById(R.id.timeline_image).setVisibility(View.GONE);
            header.findViewById(R.id.separate_line).setVisibility(View.GONE);
        } else {
            if (section >= mIndexer.getSections().length) {
                return;
            }
            String title = (String) mIndexer.getSections()[section];
            TextView headerText = (TextView) header.findViewById(R.id.header_text);
            headerText.setText(getSpannableString(title));
            headerText.setVisibility(View.VISIBLE);
            ImageView timeLine = (ImageView) header.findViewById(R.id.timeline_image);
            timeLine.setVisibility(View.VISIBLE);
            header.findViewById(R.id.separate_line).setVisibility(View.VISIBLE);
            headerText.invalidate();
            timeLine.invalidate();
            header.invalidate();
        }
    }

    @Override
    public Object[] getSections() {
        if (mIndexer == null) {
            return new String[] {""};
        } else {
            return mIndexer.getSections();
        }
    }

    @Override
    public int getPositionForSection(int section) {
        if (mIndexer == null) {
            return -1;
        }
        return mIndexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
        if (mIndexer == null) {
            return -1;
        }
        return mIndexer.getSectionForPosition(position);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem < 0 || firstVisibleItem >= linkedAdapter.items.size()) {
            return;
        }
        if (view instanceof PinnedHeaderListView) {
            ((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
        }

    }

    public void setOnHikItemClickListener(OnHikItemClickListener onHikItemClickListener) {
        this.onHikItemClickListener = onHikItemClickListener;
    }

}
