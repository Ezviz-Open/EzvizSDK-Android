/** 
 * @Title CustomImageView.java 
 * @Package null
 * @Description  null
 * @Copyright null
 * @author  
 * @date 2012-7-12 下午8:43:22 
 * @version 1.0 
 */

package com.videogo.ui.remoteplayback;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.videogo.constant.Constant;
import com.videogo.openapi.bean.resp.CloudFile;
import com.videogo.remoteplayback.RemoteFileInfo;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ezviz.ezopensdkcommon.R;

public class RemoteFileTimeBar extends ImageView {
    private static final String TAG = "RemoteFileTimeBar";
    private List<RemoteFileInfo> mFileList;

    private List<CloudFile> mCloudFileList;

    private Calendar mStartTime;

    private Calendar mEndTime;

    private int mLeftX = 0;

    private int mRightX = 0;

    private float mMeasuredHeight;

    private float mScreenWidth;

    private float mHalfScreenWidth;

    private float mHourWith;

    private ArrayList<String> mClockList;

    private Paint mPaint = new Paint();
    
    private Paint mAlarmPaint = new Paint();
    
    private Paint mTransPaint = new Paint();

    private Paint mTimePaint = new Paint();

    private Context mContext;

    public RemoteFileTimeBar(Context context) {
        super(context);
    }

    public RemoteFileTimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RemoteFileTimeBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init(Context context) {
        mContext = context;
        mClockList = new ArrayList<String>();
        for (int i = 0; i < 25; i++) {
            String temp = String.format("%02d", i) + ":00";
            mClockList.add(temp);
        }
        
        mPaint.setColor(mContext.getResources().getColor(R.color.remotefile_timebar_color));
        mAlarmPaint.setColor(mContext.getResources().getColor(R.color.remotefile_timebar_alarm_color));
        mTransPaint.setColor(mContext.getResources().getColor(R.color.transparent));

        mTimePaint.setColor(mContext.getResources().getColor(R.color.remotefile_timebar_color));
        mTimePaint.setStrokeWidth(Utils.dip2px(context, 2));
        mTimePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTimePaint.setTextSize(Utils.dip2px(context, 12));

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mMeasuredHeight = getMeasuredHeight();
        LogUtil.debugLog(TAG, "init measuredHeight:" + mMeasuredHeight);
        mScreenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        mHalfScreenWidth = mScreenWidth / 2;
        mHourWith = mScreenWidth * 5L / 24L;
        // screenHeight = dm.heightPixels; // 屏幕高（像素，如：800px）
        LogUtil.debugLog(TAG, "init screenWidth:" + mScreenWidth);
    }

    public float getScrollPosByPlayTime(long playTime, int config) {
        if(mStartTime == null) {
            return 0;
        }
        long deltams = playTime - mStartTime.getTimeInMillis();
        float pos = (float) deltams * (getWidthByOrient(config) * 5) / Constant.MILLISSECOND_ONE_DAY;
        return pos;
    }

    public Calendar pos2Calendar(int pos, int config) {
        if(mStartTime == null) {
            return null;
        }
        long time = pos * Constant.MILLISSECOND_ONE_DAY / 5 / getWidthByOrient(config)
                + mStartTime.getTimeInMillis();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(time);
        return startCalendar;
    }
    
    private int getWidthByOrient(int config) {
        if (config == Configuration.ORIENTATION_LANDSCAPE) {
            return LocalInfo.getInstance().getScreenHeight();
        } else {
            return LocalInfo.getInstance().getScreenWidth();
        }
    }
    
    public void drawFileLayout(List<RemoteFileInfo> fileList, List<CloudFile> cloudFileList, Calendar startTime, Calendar endTime) {
        mFileList = fileList;
        mCloudFileList = cloudFileList;
        mStartTime = startTime;
        mEndTime = endTime;

        // 触发调用onDraw
        postInvalidate();
    }

    public void setX(int left, int right) {
        mScreenWidth = right / 6f; // 屏幕宽（像素，如：480px）
        mHalfScreenWidth = mScreenWidth / 2;
        mHourWith = mScreenWidth * 5L / 24L;

        mLeftX = (int) (left + mHalfScreenWidth);
        mRightX = (int) (right - mHalfScreenWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawTimeText(canvas);
        drawTimeLine(canvas);
        drawTimePoint(canvas);
        // 计算显示区域
        int top = Utils.dip2px(mContext, 37); 
        List<Rect> fileRect = getFileRect(top, (int) mMeasuredHeight);
        List<Rect> fileCloundRect = getCloudFileRect(top, (int) mMeasuredHeight);
        if ((fileRect == null || fileRect.size() == 0) && (fileCloundRect == null || fileCloundRect.size() == 0)) {
            canvas.drawRect(0, top, mScreenWidth * 6, mMeasuredHeight, mTransPaint);
            return;
        }

        // 背景设置为灰色
        if (fileRect != null && fileRect.size() > 0) {
            for (int i = 0; i < fileRect.size(); i++) {
                Rect aRect = fileRect.get(i);
                if (isAlarmRecord(i)) {
                    canvas.drawRect(aRect, mAlarmPaint);
                } else {
                    canvas.drawRect(aRect, mPaint);
                }
            }
        }
        if (fileCloundRect != null && fileCloundRect.size() > 0) {
            for (int i = 0; i < fileCloundRect.size(); i++) {
                Rect aRect = fileCloundRect.get(i);
                canvas.drawRect(aRect, mAlarmPaint);
            }
        }
    }

    private boolean isAlarmRecord(int i) {
        RemoteFileInfo fileInfo = mFileList.get(i);
        int iFileType = fileInfo.getFileType();
        int oldFileType = fileInfo.getFileType();
        if (iFileType == 1 && oldFileType == 1) {
            return false;
        }
        if (iFileType == 0) {
            return true;
        }
        if (oldFileType != 0) {
            return true;
        }
        return false;
    }

    private void drawTimePoint(Canvas canvas) {
        int startY = Utils.dip2px(mContext, 22);
        int stopY = Utils.dip2px(mContext, 32); 
        int minStopY = Utils.dip2px(mContext, 27); 
        float startX = 0;
        float minStartX = 0;
        mPaint.setStrokeWidth(Utils.dip2px(mContext, 1));
        for (int i = 0; i < 25; i++) {
            startX = mHalfScreenWidth + mHourWith * i;
            canvas.drawLine(startX, startY, startX, stopY, mPaint);
            if(i < 24) {
                for (int j = 1; j <= 3; j++) {
                    minStartX = startX + mHourWith * j / 4;
                    canvas.drawLine(minStartX, startY, minStartX, minStopY, mPaint);                
                }
            }
        }
//        startY = Utils.dip2px(mContext, 21);
//        mPaint.setStrokeWidth(Utils.dip2px(mContext, 2));
//        startX = mHalfScreenWidth;
//        canvas.drawLine(startX, startY, startX, stopY, mPaint);
//        startX = mHalfScreenWidth + mHourWith * 24;
//        canvas.drawLine(startX, startY, startX, stopY, mPaint);
    }

    private void drawTimeLine(Canvas canvas) {
        int startY = Utils.dip2px(mContext, 22);
        mPaint.setStrokeWidth(Utils.dip2px(mContext, 2));
        canvas.drawLine(mLeftX-mHalfScreenWidth, startY, mRightX+mHalfScreenWidth, startY, mPaint);
    }

    private void drawTimeText(Canvas canvas) {
        int textY = Utils.dip2px(mContext, 12); 
        int offsetX = Utils.dip2px(mContext, 14); 
        for (int i = 0; i < 25; i++) {
            canvas.drawText(mClockList.get(i), mHalfScreenWidth - offsetX + mHourWith * i, textY, mTimePaint);
        }
    }

    private List<Rect> getFileRect(int top, int bottom) {
        if (mFileList == null) {
            return null;
        }

        int size = mFileList.size();
        if (size <= 0) {
            return null;
        }

        List<Rect> fileRect = new ArrayList<Rect>(size);
        float width = mRightX - mLeftX;

        for (int i = 0; i < size; i++) {
            RemoteFileInfo info = mFileList.get(i);
            Calendar startTime = info.getStartTime();
            Calendar endTime = info.getStopTime();
            // 计算区域
            Rect aRect = new Rect();
            aRect.top = top;
            aRect.bottom = bottom;
            long referStartTime = mStartTime.getTimeInMillis();
            long referEndTime = mEndTime.getTimeInMillis();
            // LogUtil.debugLog(TAG, "refer time(" + referStartTime + ", " + referEndTime + ")");
            // LogUtil.debugLog(TAG, "real  time(" + startTime + ", " + endTime + ")");
            // LogUtil.debugLog(TAG, "(" + mLeftX + ", " + mRightX + ")");
            if (startTime.getTimeInMillis() <= referStartTime) {
                aRect.left = mLeftX;
            } else {
                // 计算左坐标
                float deltaMillis = startTime.getTimeInMillis() - referStartTime;
                aRect.left = mLeftX + (int) (deltaMillis * width / (float) Constant.MILLISSECOND_ONE_DAY);
            }

            if (endTime.getTimeInMillis() > referEndTime) {
                aRect.right = mRightX;
            } else {
                // 计算右坐标
                float deltaMillis = endTime.getTimeInMillis() - referStartTime;
                aRect.right = mLeftX + (int) ((deltaMillis * width / (float) Constant.MILLISSECOND_ONE_DAY));
            }

            // LogUtil.debugLog(TAG, "LR(" + aRect.left + "," + aRect.right + ")");

            fileRect.add(aRect);

        }

        return fileRect;
    }

    private List<Rect> getCloudFileRect(int top, int bottom) {
        if (mCloudFileList == null) {
            return null;
        }

        int size = mCloudFileList.size();
        if (size <= 0) {
            return null;
        }

        List<Rect> fileRect = new ArrayList<Rect>(size);
        float width = mRightX - mLeftX;

        for (int i = 0; i < size; i++) {
            CloudFile info = mCloudFileList.get(i);
            long startTime = Utils.get19TimeInMillis(info.getStartTime());
            long endTime = Utils.get19TimeInMillis(info.getEndTime());

            // 计算区域
            Rect aRect = new Rect();
            aRect.top = top;
            aRect.bottom = bottom;
            long referStartTime = mStartTime.getTimeInMillis();
            long referEndTime = mEndTime.getTimeInMillis();
            // .LogUtil.debugLog("setStopTime", "refer time(" + referStartTime + ", " + referEndTime
            // + ")");
            // LogUtil.debugLog(TAG, "real  time(" + startTime + ", " + endTime + ")");
            // LogUtil.debugLog(TAG, "(" + mLeftX + ", " + mRightX + ")");
            if (startTime <= referStartTime) {
                aRect.left = mLeftX;
            } else {
                // 计算左坐标
                float deltaMillis = startTime - referStartTime;
                aRect.left = mLeftX + (int) (deltaMillis * width / (float) Constant.MILLISSECOND_ONE_DAY);
            }

            if (endTime > referEndTime) {
                aRect.right = mRightX;
            } else {
                // 计算右坐标
                float deltaMillis = endTime - referStartTime;
                aRect.right = mLeftX + (int) ((deltaMillis * width / (float) Constant.MILLISSECOND_ONE_DAY));
            }

            // LogUtil.debugLog(TAG, "LR(" + aRect.left + "," + aRect.right + ")");

            fileRect.add(aRect);

        }

        return fileRect;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        width = (int) (mScreenWidth * 6);
        height = heightSize;
        mMeasuredHeight = height;
        LogUtil.debugLog(TAG, "onMeasure measuredHeight:" + mMeasuredHeight);
        // MUST CALL THIS
        setMeasuredDimension(width, height);
    }
    
    public Calendar getLastStopTime() {
        if (mCloudFileList == null && mCloudFileList.size() == 0) {
            return mEndTime;
        }
        
        long cloudStopTime = 0;
        if (mCloudFileList.size() > 0) {
            cloudStopTime = Utils.get19TimeInMillis(mCloudFileList.get(mCloudFileList.size() - 1).getEndTime());
        }

        if (cloudStopTime == 0) {
            return mEndTime;
        }

        if(cloudStopTime > mEndTime.getTimeInMillis()) {
            return mEndTime;
        } else {
            Calendar cloudStop = Calendar.getInstance();
            cloudStop.setTimeInMillis(cloudStopTime);
            return cloudStop;
        }
    }
}
