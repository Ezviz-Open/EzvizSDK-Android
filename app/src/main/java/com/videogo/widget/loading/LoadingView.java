package com.videogo.widget.loading;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.videogo.util.Utils;

import ezviz.ezopensdk.R;

public class LoadingView extends View {

    private long mTimeMillis;
    private long mDuration = 2400;

    private Bitmap[] mBalls;
    private int mSquareTrackLength;
    private int mBallSize;
    private Paint mBallPaint;

    private int mCenterX;
    private int mCenterY;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mBalls = new Bitmap[4];

        mBalls[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_loading_1);
        mBalls[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_loading_2);
        mBalls[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_loading_3);
        mBalls[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_loading_4);

        mBallSize = mBalls[0].getWidth() / 2;
        mBallPaint = new Paint();
        mBallPaint.setAntiAlias(true);

        mSquareTrackLength = Utils.dip2px(context, 15);

        int minSize = mSquareTrackLength + mBallSize * 2;
        setMinimumHeight(minSize);
        setMinimumWidth(minSize);

        mTimeMillis = System.currentTimeMillis();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int minSize = mSquareTrackLength + mBallSize * 2;

        switch (widthMode) {
            case MeasureSpec.AT_MOST:
                widthSize = minSize;
                break;

            case MeasureSpec.UNSPECIFIED:
                widthSize = minSize;
                break;

            case MeasureSpec.EXACTLY:
                widthSize = Math.max(widthSize, minSize);
                break;

            default:
                break;
        }

        switch (heightMode) {
            case MeasureSpec.AT_MOST:
                heightSize = minSize;
                break;

            case MeasureSpec.UNSPECIFIED:
                heightSize = minSize;
                break;

            case MeasureSpec.EXACTLY:
                heightSize = Math.max(heightSize, minSize);
                break;

            default:
                break;
        }

        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, widthMode),
                MeasureSpec.makeMeasureSpec(heightSize, heightMode));
        mCenterX = getMeasuredWidth() / 2;
        mCenterY = getMeasuredHeight() / 2;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE)
            mTimeMillis = System.currentTimeMillis();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();

        float value = (System.currentTimeMillis() - mTimeMillis) % mDuration * 4 / (float) mDuration;
        float d = 90 * (int) value;

        canvas.rotate(d, mCenterX, mCenterY);
        value -= (int) value;

        float offset = value * mSquareTrackLength - mSquareTrackLength / 2;

        float x = offset + mCenterX;
        float y = mCenterY - mSquareTrackLength / 2;
        for (int i = 0; i < 4; i++) {
            canvas.save();
            canvas.rotate(-d - i * 90, x, y);
            canvas.drawBitmap(mBalls[i], x - mBallSize, y - mBallSize, mBallPaint);
            canvas.restore();
            canvas.rotate(90, mCenterX, mCenterY);
        }

        canvas.restore();

        invalidate();
    }
}
