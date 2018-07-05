package com.hyf.takephotovideolib;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by  HYF on 2018/6/29.
 * Email：775183940@qq.com
 */

public class TakePhotoVideoHelper {

    public static final String RESULT_DATA = "RESULT_DATA";

    public interface Mode {
        // 三种模式
        int RECORD_MODE_PHOTO = 0;
        int RECORD_MODE_VIDEO = 1;
        int RECORD_MODE_ALL = 2;
    }

    /**
     * 拍照
     *
     * @param activity
     * @param requestCode
     * @param savePath
     */
    public static final void startTakePhoto(Activity activity, int requestCode, String savePath) {
        startRecord(activity, Mode.RECORD_MODE_PHOTO, requestCode, 15000, savePath);
    }

    /**
     *  拍摄视频
     * @param activity
     * @param requestCode
     * @param savePath
     * @param duration
     */
    public static final void startTakeVideo(Activity activity, int requestCode, String savePath, int duration) {
        startRecord(activity, Mode.RECORD_MODE_VIDEO, requestCode, duration, savePath);
    }

    /**
     *  两个
     * @param activity
     * @param requestCode
     * @param savePath
     * @param duration
     */
    public static final void startTakePhotoVideo(Activity activity, int requestCode, String savePath, int duration) {
        startRecord(activity, Mode.RECORD_MODE_ALL, requestCode, duration, savePath);
    }


    // —————————————————————私有方法————————————————————

    private static final void startRecord(Activity activity, int mode, int requestCode, int duration, String savePath) {
        Intent intent = new Intent(activity, TakePhotoVideoActivity.class);
        intent.putExtra(TakePhotoVideoActivity.MODE, mode);
        intent.putExtra(TakePhotoVideoActivity.DURATION, duration);
        intent.putExtra(TakePhotoVideoActivity.SAVE_PATH, savePath);
        activity.startActivityForResult(intent, requestCode);
    }


}
