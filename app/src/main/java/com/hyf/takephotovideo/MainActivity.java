package com.hyf.takephotovideo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hyf.takephotovideolib.TakePhotoVideoHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    String savePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName();
    }

    public void startRecord(View view) {
        TakePhotoVideoHelper.startTakePhoto(this, 100, savePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            String path = data.getStringExtra(TakePhotoVideoHelper.RESULT_DATA);
            Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
        }
    }

    public void startRecordVideo(View view) {
        TakePhotoVideoHelper.startTakeVideo(this, 100, savePath, 20000);
    }
}
