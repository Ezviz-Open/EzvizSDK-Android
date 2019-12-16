package com.videogo.ui.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.videogo.util.LogUtil;

public class EZGenerateFilePath {
	private static final String TAG = "GenerateFilePath";

	public static String generateFilePath(String rootPath, String cameraName, String deviceSerial) throws IOException {
		if (rootPath == null || cameraName == null) {
			return null;
		}

		// 创建根目录
		File file = new File(rootPath);
		if (!file.exists()) {
			if(!file.mkdir()) {
			    LogUtil.errorLog(TAG, "file.mkdir fail"); 
			}
		}

		Calendar calendar = Calendar.getInstance();

		// 年、月、日
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		String filePath = String.format("%s/%04d%02d%02d", rootPath, year, month, day);
		file = new File(filePath);
		if (!file.exists()) {
			if(!file.mkdir()) {
			    LogUtil.errorLog(TAG, "file.mkdir fail"); 
			}
		}

		// 年、月、日、时、分、秒
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		int milsec = calendar.get(Calendar.MILLISECOND);

		// 文件格式为mnt/sdcard/VideoGo/20120901/20120901141138540_test.jpg
		filePath += String.format("/%04d%02d%02d%02d%02d%02d%03d_%s_%s", year, month, day, hour, min, sec, milsec, cameraName, deviceSerial);

		LogUtil.debugLog(TAG, "generatFilePath file path:" + filePath);
		return filePath;
	}

	public static boolean saveBitmap2file(Bitmap bmp, String filename, String thumbnailPath) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		int qualityThumbnail = 10;
		OutputStream stream = null;
		OutputStream streamThumbnail = null;
		try {
			stream = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			streamThumbnail = new FileOutputStream(thumbnailPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (streamThumbnail != null) {
			bmp.compress(format, qualityThumbnail, streamThumbnail);
		}else {
			return false;
		}
		
		if (stream != null) {
			bmp.compress(format, quality, stream);
		}else {
			return false;
		}
		return true;		
	}
	
	public static String generateThumbnailPath(String filePath) {
        if (filePath == null) {
            return null;
        }

        // 查找'/'
        int pos = filePath.lastIndexOf("/");
        if (pos == -1) {
            return null;
        }

        // 生成目录
        String dir = filePath.substring(0, pos + 1) + "thumbnails";
        File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdir()) {
                LogUtil.errorLog(TAG, "mkdir failed");
            }
        }

        // 缩略图目录
        String thumbnailPath = dir + "/" + filePath.substring(pos + 1);

        LogUtil.debugLog(TAG, "generateThumbnailPath thumbnail path:" + thumbnailPath);
        return thumbnailPath;
    }
}
