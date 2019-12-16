package ezviz.ezopensdk.debug;

import android.util.Log;

import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class VideoFileUtil {

    private final static String TAG = "VideoFileUtil";
    private static FileOutputStream mFos;

    public static void startRecordOriginVideo(EZPlayer player, final String originVideoFileWithPath) {

        stopRecordOriginVideo();

        File originVideoFile = new File(originVideoFileWithPath);

        boolean ret = checkAndMakeParentFolder(originVideoFile);
        if (ret){
            try {
                mFos = new FileOutputStream(originVideoFile);
                ret = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "failed to create file! " + originVideoFileWithPath);
                ret = false;
            }
        }
        if (!ret){
            Log.e(TAG, "check storage permission of your app");
            return;
        }

        player.setOriginDataCallback(new EZOpenSDKListener.OriginDataCallback() {
            @Override
            public void onData(int dataType, byte[] data, int len) {
                if (mFos != null){
                    try {
                        Log.v(TAG, "write origin video to file...");
                        if (data != null){
                            mFos.write(data, 0, len);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "failed to write data to file!");
                    }
                }
            }
        });
    }

    private static boolean checkAndMakeParentFolder(File file) {
        boolean ret;
        File parentFolder = file.getParentFile();
        if (parentFolder.exists()){
            ret = true;
        }else{
            ret = parentFolder.mkdirs();
        }
        return ret;
    }

    public static void stopRecordOriginVideo(){
        if (mFos != null){
            try {
                mFos.flush();
                mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mFos = null;
        }
    }

}
