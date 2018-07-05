package com.hyf.takephotovideolib.support;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by  HYF on 2018/7/5.
 * Email：775183940@qq.com
 *  使用系统照相机进行拍照
 *  请调用 open camera之前 注意Android 6.0 的授权
 *          Manifest.permission.WRITE_EXTERNAL_STORAGE
 *          Manifest.permission.READ_EXTERNAL_STORAGE
 *          Manifest.permission.CAMERA
 *          Manifest.permission.RECORD_AUDIO
 *
 *  注：返回的图片没有压缩  请自行在回调中处理
 */

public class SystemCapturePhoto {
    private Activity mActivity;
    private int requestCode;
    private String savePath;

    private String fileAbsolutePath;
    private String fileName;

    public SystemCapturePhoto(Activity activity, int requestCode, String savePath) {
        mActivity = activity;
        this.requestCode = requestCode;
        this.savePath = savePath;
    }

    public final void openCamera(){
        File parentFolder = new File(savePath);
        if (!parentFolder.exists()) {
            parentFolder.mkdirs();
        }
        fileName = System.currentTimeMillis()+".jpg";
        File tempFile = new File(savePath,fileName);
        if (tempFile.exists()) {
            tempFile.delete();
        }
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileAbsolutePath = tempFile.getAbsolutePath();

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // 从文件中创建uri
            Uri uri = Uri.fromFile(tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            //兼容android7.0 使用共享文件的形式
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
            Uri uri = mActivity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mActivity.startActivityForResult(intent, requestCode);
    }


    public String getFileName() {
        return fileName;
    }

    public String getFileAbsolutePath() {
        return fileAbsolutePath;
    }

    public final void deleted(){
        File file = new File(fileAbsolutePath);
        if (file.exists())file.delete();
    }
}
