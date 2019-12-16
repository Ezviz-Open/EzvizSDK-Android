/* 
 * @ProjectName iVMS-5060_V3.0
 * @Copyright null
 * 
 * @FileName AudioPlayUtil.java
 * @Description 这里对文件进行描述
 * 
 * @author mlianghua
 * @data Jun 28, 2012
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.util;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;

import ezviz.ezopensdkcommon.R;


public class AudioPlayUtil {

    private SoundPool mSoundPool = null;

    public static int CAPTURE_SOUND = 1;

    public static int RECORD_SOUND = 2;

    private boolean mRingerMode = true;

    private int mStreamID = 0;

    private Context mContext = null;

    private HashMap<Integer, Integer> mSoundMap = null;

    private static AudioPlayUtil mAudioPlayUtil = null;

    private AudioPlayUtil(Application application) {
        mContext = application.getApplicationContext();
        mSoundMap = new HashMap<Integer, Integer>();

        mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 100);
        mSoundMap.put(CAPTURE_SOUND, mSoundPool.load(mContext, R.raw.paizhao, 0));
        mSoundMap.put(RECORD_SOUND, mSoundPool.load(mContext, R.raw.record, 0));
    };

    public static AudioPlayUtil getInstance(Application application) {
        if (mAudioPlayUtil == null) {
            mAudioPlayUtil = new AudioPlayUtil(application);
        }

        return mAudioPlayUtil;
    }

    public void playAudioFile(int soundId) {
        stopAudioPlay();
        getAlarmParams();
        if (mRingerMode) {
            mStreamID = mSoundPool.play(mSoundMap.get(soundId), 1, 1, 0, 0, 1);
            if (mStreamID == 0 && (soundId == 3 || soundId == 4)) {
            	mStreamID = mSoundPool.play(mSoundMap.get(soundId + 2), 1, 1, 0, 0, 1);
    		}
        }
    }

    public void stopAudioPlay() { 
        if (mStreamID != 0) {
            mSoundPool.stop(mStreamID);
        }
    }

    private void getAlarmParams() {
        // AudioManager provides access to volume and ringer mode control.
        AudioManager volMgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        switch (volMgr.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
            case AudioManager.RINGER_MODE_VIBRATE:
                mRingerMode = false;
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                mRingerMode = true;
                break;
            default:
                break;
        }
    }
}
