package com.hyf.takephotovideolib.support;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.hyf.takephotovideolib.preview.PreviewVideoActivity;
import com.hyf.takephotovideolib.record.TakePhotoVideoActivity;

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
     * 拍摄视频
     *
     * @param activity
     * @param requestCode
     * @param savePath
     * @param duration
     */
    public static final void startTakeVideo(Activity activity, int requestCode, String savePath, int duration) {
        startRecord(activity, Mode.RECORD_MODE_VIDEO, requestCode, duration, savePath);
    }

    /**
     * 两个
     *
     * @param activity
     * @param requestCode
     * @param savePath
     * @param duration
     */
    public static final void startTakePhotoVideo(Activity activity, int requestCode, String savePath, int duration) {
        startRecord(activity, Mode.RECORD_MODE_ALL, requestCode, duration, savePath);
    }

    /**
     * 打开文件管理器进行选择文件  默认选择所有的   可以通过mime类型进行过滤
     *
     * @param activity
     */
    public static final void startFileExplorer(Activity activity, int requestCode) {
        startFileExplorer(activity, requestCode, "*/*");
    }

    /**
     * 打开文件管理器选择文件
     *
     * @param activity
     * @param mimeType
     */
    public static final void startFileExplorer(Activity activity, int requestCode, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType(“image/*”);//选择图片
        //intent.setType(“audio/*”); //选择音频
        //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
        //intent.setType(“video/*;image/*”);//同时选择视频和图片
        intent.setType(mimeType);//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(intent, requestCode);
    }


    public static final void startPlayVideo(Context context, String videoPath) {
        Intent intent = new Intent(context, PreviewVideoActivity.class);
        intent.putExtra(PreviewVideoActivity.FILE_PATH, videoPath);
        context.startActivity(intent);
    }

    public static final void startPlayVideo(Context context, String title, String videoPath) {
        Intent intent = new Intent(context, PreviewVideoActivity.class);
        intent.putExtra(PreviewVideoActivity.TITLE, title);
        intent.putExtra(PreviewVideoActivity.FILE_PATH, videoPath);
        context.startActivity(intent);
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
