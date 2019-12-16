package com.videogo.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.videogo.widget.CheckTextButton;

public class ScreenOrientationHelper implements SensorEventListener {

    private final static String TAG = ScreenOrientationHelper.class.getSimpleName();

    private Activity mActivity;
    private CheckTextButton mButton1, mButton2;
    private int mOriginOrientation;
    private Boolean mPortraitOrLandscape;

    private SensorManager mSensorManager;
    private Sensor[] mSensors;

    private float[] mAccelerometerValues = new float[3];
    private float[] mMagneticFieldValues = new float[3];

    public ScreenOrientationHelper(Activity activity) {
        this(activity, null, null);
    }

    public ScreenOrientationHelper(Activity activity, CheckTextButton button) {
        this(activity, button, null);
    }

    public ScreenOrientationHelper(Activity activity, CheckTextButton button1, CheckTextButton button2) {
        mActivity = activity;
        mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);

        mButton1 = button1;
        if (mButton1 != null) {
            mButton1.setEnabled(false);
            mButton1.setToggleEnable(false);
            mButton1.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSensors != null) {
                        if (mButton1.isChecked())
                            portrait();
                        else
                            landscape();
                    }
                }
            });
        }

        mButton2 = button2;
        if (mButton2 != null) {
            mButton2.setToggleEnable(false);
            mButton2.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSensors != null) {
                        if (mButton2.isChecked())
                            portrait();
                        else
                            landscape();
                    }
                }
            });
        }
    }

    public void enableSensorOrientation() {
        if (mSensors == null) {
            mOriginOrientation = mActivity.getRequestedOrientation();

            mSensors = new Sensor[2];
            mSensors[0] = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensors[1] = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mSensorManager.registerListener(this, mSensors[0], SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensors[1], SensorManager.SENSOR_DELAY_NORMAL);
        }
        mButton1.setEnabled(true);
    }

    public void disableSensorOrientation(boolean reset) {
        if (mSensors != null) {
            mSensorManager.unregisterListener(this, mSensors[0]);
            mSensorManager.unregisterListener(this, mSensors[1]);
            mSensors = null;

            if (reset == true) {
                mActivity.setRequestedOrientation(mOriginOrientation);
            }
        }
        mButton1.setEnabled(false);
    }

    public void disableSensorOrientation() {
        disableSensorOrientation(true);
    }

    public void landscape() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setButtonChecked(true);
        mPortraitOrLandscape = false;
    }

    public void portrait() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setButtonChecked(false);
        mPortraitOrLandscape = true;
    }

    public void setButtonChecked(boolean checked) {
        if (mButton1 != null)
            mButton1.setChecked(checked);

        if (mButton2 != null)
            mButton2.setChecked(checked);
    }

    public void postOnStart() {
        if (mSensors != null) {
            mSensorManager.registerListener(this, mSensors[0], SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mSensors[1], SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void postOnStop() {
        if (mSensors != null) {
            mSensorManager.unregisterListener(this, mSensors[0]);
            mSensorManager.unregisterListener(this, mSensors[1]);
        }
    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];

        SensorManager.getRotationMatrix(R, null, mAccelerometerValues, mMagneticFieldValues);
        SensorManager.getOrientation(R, values);

        if (mSensors != null) {
            if (mSensors[1] == null)
                calculateByAccelerometer(mAccelerometerValues);
            else
                calculateByOrientation(values);
        }
    }

    private void calculateByAccelerometer(float[] values) {
        int orientation = mActivity.getRequestedOrientation();

        if ((-2f < values[1] && values[1] <= 2f) && values[0] < 0) {// 向左
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                setButtonChecked(true);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (4f < values[1] && values[1] < 10f) { // 正南
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                setButtonChecked(false);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (-10f < values[1] && values[1] < -4f) { // 正北
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setButtonChecked(false);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if ((-2f < values[1] && values[1] <= 2f) && values[0] > 0) { // 向右
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                setButtonChecked(true);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;
        }
    }

    private void calculateByOrientation(float[] values) {
        values[0] = (float) Math.toDegrees(values[0]);
        values[1] = (float) Math.toDegrees(values[1]);
        values[2] = (float) Math.toDegrees(values[2]);

        int orientation = mActivity.getRequestedOrientation();

        if ((-10.0f < values[1] && values[1] <= 10f) && values[2] < -40f) {// 向左
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                setButtonChecked(true);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (40.0f < values[1] && values[1] < 90.0f) { // 正南
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                setButtonChecked(false);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if (-90.0f < values[1] && values[1] < -40.0f) { // 正北
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    && (mPortraitOrLandscape == null || mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                setButtonChecked(false);
            }

            if (mPortraitOrLandscape != null && mPortraitOrLandscape)
                mPortraitOrLandscape = null;

        } else if ((-10.0f < values[1] && values[1] <= 10f) && values[2] > 40f) { // 向右
            if (orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    && (mPortraitOrLandscape == null || !mPortraitOrLandscape)) {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                setButtonChecked(true);
            }

            if (mPortraitOrLandscape != null && !mPortraitOrLandscape)
                mPortraitOrLandscape = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagneticFieldValues = event.values;
                break;

            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerValues = event.values;
                break;

            default:
                break;
        }

        calculateOrientation();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
