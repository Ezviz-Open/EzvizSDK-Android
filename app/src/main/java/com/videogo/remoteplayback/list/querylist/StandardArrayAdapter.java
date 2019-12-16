package com.videogo.remoteplayback.list.querylist;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.remoteplayback.list.bean.ClickedListItem;
import com.videogo.remoteplayback.list.bean.CloudPartInfoFileEx;
import com.videogo.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ezviz.ezopensdkcommon.R;

public class StandardArrayAdapter extends ArrayAdapter<CloudPartInfoFileEx> {

    private ArrayAdapterChangeListener adapterChangeListener;

    final List<CloudPartInfoFileEx> items;

    private List<CloudPartInfoFileEx> cloudFileExAll = new ArrayList<CloudPartInfoFileEx>();

    private List<CloudPartInfoFileEx> localFileExAll = null;

    private Context context;

    public StandardArrayAdapter(final Context context, final int textViewResourceId,
        final List<CloudPartInfoFileEx> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        this.cloudFileExAll.addAll(items);
        this.context = context;
    }

    public List<CloudPartInfoFileEx> getCloudFileEx() {
        return cloudFileExAll;
    }

    public void removeCloudFileBySelected(HashMap<String, String> selectedCloudFiles) {
        if (cloudFileExAll == null) {
            return;
        }
        Iterator<CloudPartInfoFileEx> it = cloudFileExAll.iterator();
        List<CloudPartInfoFile> cloudInfoFiles = new ArrayList<CloudPartInfoFile>();
        boolean isMore = false;
        while (it.hasNext()) {
            CloudPartInfoFileEx infoFileEx = it.next();
            if (infoFileEx.getDataThree() != null) {
                if (selectedCloudFiles.containsKey(infoFileEx.getDataThree().getFileId())) {
                    infoFileEx.setDataThree(null);
                }
            }

            if (infoFileEx.getDataTwo() != null) {
                if (selectedCloudFiles.containsKey(infoFileEx.getDataTwo().getFileId())) {
                    infoFileEx.setDataTwo(null);
                }
            }

            if (infoFileEx.getDataOne() != null) {
                if (selectedCloudFiles.containsKey(infoFileEx.getDataOne().getFileId())) {
                    infoFileEx.setDataOne(null);
                }
            }

            if (infoFileEx.getDataOne() != null) {
                cloudInfoFiles.add(infoFileEx.getDataOne());
            }

            if (infoFileEx.getDataTwo() != null) {
                cloudInfoFiles.add(infoFileEx.getDataTwo());
            }

            if (infoFileEx.getDataThree() != null) {
                cloudInfoFiles.add(infoFileEx.getDataThree());
            }

            if (infoFileEx.isMore()) {
                isMore = true;
            }
        }
        sortCloudFiles(cloudInfoFiles, isMore);
        this.items.clear();
        if (cloudFileExAll.size() == 0) {
            adapterChangeListener.onDeleteCloudFileCompleteListener(false);
        } else if (cloudFileExAll.size() == 1 && cloudFileExAll.get(0).isMore()) {
            cloudFileExAll.clear();
            adapterChangeListener.onDeleteCloudFileCompleteListener(true);
        } else {
            this.items.addAll(cloudFileExAll);
        }
    }

    private void sortCloudFiles(List<CloudPartInfoFile> cloudInfoFiles, boolean isMore) {
        cloudFileExAll.clear();
        int length = cloudInfoFiles.size();
        int i = 0;
        while (i < length) {
            CloudPartInfoFileEx cloudPartInfoFileEx = new CloudPartInfoFileEx();
            CloudPartInfoFile dataOne = cloudInfoFiles.get(i);
            dataOne.setPosition(i);
            Calendar beginCalender = Utils.convert14Calender(dataOne.getStartTime());
            String hour = getHour(beginCalender.get(Calendar.HOUR_OF_DAY));
            cloudPartInfoFileEx.setHeadHour(hour);
            cloudPartInfoFileEx.setDataOne(dataOne);
            i++;
            if (i > length - 1) {
                cloudFileExAll.add(cloudPartInfoFileEx);
                continue;
            }
            CloudPartInfoFile dataTwo = cloudInfoFiles.get(i);
            if (hour.equals(getHour(Utils.convert14Calender(dataTwo.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                dataTwo.setPosition(i);
                cloudPartInfoFileEx.setDataTwo(dataTwo);
                i++;
                if (i > length - 1) {
                    cloudFileExAll.add(cloudPartInfoFileEx);
                    continue;
                }
                CloudPartInfoFile dataThree = cloudInfoFiles.get(i);
                if (hour.equals(getHour(Utils.convert14Calender(dataThree.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                    dataThree.setPosition(i);
                    cloudPartInfoFileEx.setDataThree(dataThree);
                    i++;
                }
            }
            cloudFileExAll.add(cloudPartInfoFileEx);
        }
        if (isMore) {
            CloudPartInfoFileEx partInfoFileEx = new CloudPartInfoFileEx();
            partInfoFileEx.setMore(true);
            cloudFileExAll.add(partInfoFileEx);
        }
    }

    private String getHour(int hourOfDay) {
        // if (hourOfDay < 10) {
        // return "  " + hourOfDay + MINUTE;
        // } else {
        return hourOfDay + getContext().getString(R.string.play_hour);
        // }
    }

    public List<CloudPartInfoFileEx> getLocalFileEx() {
        return localFileExAll;
    }

    public void addLocalFileExAll(List<CloudPartInfoFileEx> localFileExAll) {
        this.localFileExAll = localFileExAll;
        items.addAll(localFileExAll);
        this.notifyDataSetChanged();
    }

    public void addLocalFileExAll() {
        if (localFileExAll != null) {
            items.addAll(localFileExAll);
            this.notifyDataSetChanged();
        }
    }

    public void minusLocalFileExAll() {
        items.clear();
        items.addAll(cloudFileExAll);
        this.notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    public CloudPartInfoFile getNextFile(ClickedListItem playClickItem) {

        CloudPartInfoFile partInfoFile = null;
        List<CloudPartInfoFile> cloudList = new ArrayList<CloudPartInfoFile>();
        for (CloudPartInfoFileEx infoFile : cloudFileExAll) {
            if (infoFile.getDataOne() != null) {
                cloudList.add(infoFile.getDataOne());
            }
            if (infoFile.getDataTwo() != null) {
                cloudList.add(infoFile.getDataTwo());
            }
            if (infoFile.getDataThree() != null) {
                cloudList.add(infoFile.getDataThree());
            }
        }
        if (localFileExAll != null) {
            for (CloudPartInfoFileEx infoFile : localFileExAll) {
                if (infoFile.getDataOne() != null) {
                    cloudList.add(infoFile.getDataOne());
                }
                if (infoFile.getDataTwo() != null) {
                    cloudList.add(infoFile.getDataTwo());
                }
                if (infoFile.getDataThree() != null) {
                    cloudList.add(infoFile.getDataThree());
                }
            }
        }
        if (playClickItem.getIndex() < cloudList.size() - 1) {
            partInfoFile = cloudList.get(playClickItem.getIndex() + 1);
        }
        return partInfoFile;
    }

    public SelectFileInfo getNextFile(int position) {
        CloudPartInfoFile selectedInfoFile = null;
        boolean isSelected = false;
        int selPosition = 0;
        for (CloudPartInfoFileEx infoFile : items) {
            if (infoFile.getDataOne() != null) {
                if (isSelected) {
                    selectedInfoFile = infoFile.getDataOne();
                    break;
                } else {
                    if (infoFile.getDataOne().getPosition() == position) {
                        isSelected = true;
                    }
                }
            }

            if (infoFile.getDataTwo() != null) {
                if (isSelected) {
                    selectedInfoFile = infoFile.getDataTwo();
                    break;
                } else {
                    if (infoFile.getDataTwo().getPosition() == position) {
                        isSelected = true;
                    }
                }
            }

            if (infoFile.getDataThree() != null) {
                if (isSelected) {
                    selectedInfoFile = infoFile.getDataThree();
                    break;
                } else {
                    if (infoFile.getDataThree().getPosition() == position) {
                        isSelected = true;
                    }
                }
            }
            selPosition++;
        }

        return new SelectFileInfo(selectedInfoFile, selPosition);
    }

    public void clearData() {
        if (items != null) {
            items.clear();
        }
        cloudFileExAll.clear();
        if (localFileExAll != null) {
            localFileExAll.clear();
        }
    }

    public class SelectFileInfo {
        private CloudPartInfoFile selectedInfoFile;
        private int selPosition;

        public SelectFileInfo(CloudPartInfoFile selectedInfoFile, int selPosition) {
            this.selectedInfoFile = selectedInfoFile;
            this.selPosition = selPosition;
        }

        public CloudPartInfoFile getSelectedInfoFile() {
            return selectedInfoFile;
        }

        public void setSelectedInfoFile(CloudPartInfoFile selectedInfoFile) {
            this.selectedInfoFile = selectedInfoFile;
        }

        public int getSelPosition() {
            return selPosition;
        }

        public void setSelPosition(int selPosition) {
            this.selPosition = selPosition;
        }

    }

    public void setAdapterChangeListener(ArrayAdapterChangeListener adapterChangeListener) {
        this.adapterChangeListener = adapterChangeListener;
    }

    public interface ArrayAdapterChangeListener {
        void onDeleteCloudFileCompleteListener(boolean isLocal);
    }
}
