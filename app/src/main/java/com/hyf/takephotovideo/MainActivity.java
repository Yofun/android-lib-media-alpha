package com.hyf.takephotovideo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hyf.takephotovideolib.TakePhotoVideoHelper;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    String savePath;
    String[] permiss = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName();
    }

    public void startRecord(View view) {
        startRecordPhoto();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String path = data.getStringExtra(TakePhotoVideoHelper.RESULT_DATA);
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        }
    }

    public void startRecordVideo(View view) {
        startRecordVideo();
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
            TakePhotoVideoHelper.startTakeVideo(this, 100, savePath, 20000);
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
