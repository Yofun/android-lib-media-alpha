package com.hyf.takephotovideo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.hyf.takephotovideolib.TakePhotoVideoHelper;
import com.hyf.takephotovideolib.support.SystemCapturePhoto;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    String savePath;
    String[] permiss = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    private SystemCapturePhoto mCapturePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName();
        mCapturePhoto = new SystemCapturePhoto(getActivity(),101,savePath);
    }

    public void startRecord(View view) {
        startRecordPhoto();
    }

    public void startRecordVideo(View view) {
        startRecordVideo();
    }

    public void startRecordPhotoVideo(View view) {
        startRecordPhotoVideo();
    }

    public void startSysCamera(View view) {
        startOpenSysCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String path = data.getStringExtra(TakePhotoVideoHelper.RESULT_DATA);
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        }else if (requestCode == 101 && resultCode == RESULT_OK){
            final String filePath = mCapturePhoto.getFileAbsolutePath();
            Luban.with(this)
                    .load(filePath)
                    .ignoreBy(200)
                    .setTargetDir(savePath)
                    .filter(new CompressionPredicate() {
                        @Override
                        public boolean apply(String path) {
                            return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                        }
                    })
                    .setCompressListener(new OnCompressListener() {
                        ProgressDialog dialog;
                        @Override
                        public void onStart() {
                            dialog = ProgressDialog.show(getContext(), "提示", "正在处理图片中...", false, false);
                        }

                        @Override
                        public void onSuccess(File file) {
                            if (dialog!=null)dialog.dismiss();
                            Toast.makeText(MainActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            File yt = new File(filePath);
                            if (yt.exists())yt.delete();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (dialog!=null)dialog.dismiss();
                            Toast.makeText(MainActivity.this, filePath, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .launch();
        }
    }

    // ——————————————————————————————————————————————————
    private static final int requestCode = 113;

    @AfterPermissionGranted(requestCode)
    private void startRecordPhoto() {
        if (EasyPermissions.hasPermissions(getContext(), permiss))
            TakePhotoVideoHelper.startTakePhoto(this, 100, savePath);
        else
            EasyPermissions.requestPermissions(getActivity(), "申请获取相关权限", requestCode, permiss);
    }

    @AfterPermissionGranted(requestCode)
    private void startRecordVideo() {
        if (EasyPermissions.hasPermissions(getContext(), permiss))
            TakePhotoVideoHelper.startTakeVideo(this, 100, savePath, 15000);
        else
            EasyPermissions.requestPermissions(getActivity(), "申请获取相关权限", requestCode, permiss);
    }

    @AfterPermissionGranted(requestCode)
    private void startRecordPhotoVideo() {
        if (EasyPermissions.hasPermissions(getContext(), permiss))
            TakePhotoVideoHelper.startTakePhotoVideo(this, 100, savePath, 15000);
        else
            EasyPermissions.requestPermissions(getActivity(), "申请获取相关权限", requestCode, permiss);
    }

    @AfterPermissionGranted(requestCode)
    private void startOpenSysCamera() {
        if (EasyPermissions.hasPermissions(getContext(), permiss))
            mCapturePhoto.openCamera();
        else
            EasyPermissions.requestPermissions(getActivity(), "申请获取相关权限", requestCode, permiss);
    }


    public final Activity getActivity() {
        return this;
    }

    public final Context getContext() {
        return this;
    }


}
