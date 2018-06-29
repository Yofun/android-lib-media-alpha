package com.hyf.takephotovideolib;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

public class TakePhotoVideoActivity extends AppCompatActivity {
    public static final String MODE = "MODE";
    public static final String DURATION = "DURATION";
    public static final String SAVE_PATH = "SAVE_PATH";


    private int mode;
    private int duration;
    private String savePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_take_photo_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initIntent();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.hyf_take_photo_video_fragment_container, RecordVideoFragment.newInstance(mode, duration, savePath, 1024 * 1024 * 30L))
                    .commit();
        }
    }

    /**
     * 返回上一个fragment
     */
    public void popBackStack() {
        getSupportFragmentManager().popBackStack();
    }

    private void initIntent() {
        Intent intent = getIntent();
        mode = intent.getIntExtra(MODE, TakePhotoVideoHelper.Mode.RECORD_MODE_PHOTO);
        duration = intent.getIntExtra(DURATION, 15000);
        savePath = intent.getStringExtra(SAVE_PATH);
    }

    @Override
    public void onBackPressed() {
        try {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            Log.e("当前的frament", fragmentList.get(fragmentList.size() - 1).toString() + "");
            ((BaseRecordFragment) fragmentList.get(fragmentList.size() - 1)).finish();
        } catch (Exception e) {
            finish();
        }
    }

    /**
     * 返回视频路径
     *
     * @param videoPath
     */
    public void returnVideoPath(String videoPath) {
        Intent data = new Intent();
        data.putExtra(TakePhotoVideoHelper.RESULT_DATA, videoPath);
        if (getParent() == null) {
            setResult(RESULT_OK, data);
        } else {
            getParent().setResult(RESULT_OK, data);
        }
        finish();
    }

    /**
     * 返回图片路径
     *
     * @param photoPath
     */
    public void returnPhotoPath(String photoPath) {
        Intent data = new Intent();
        data.putExtra(TakePhotoVideoHelper.RESULT_DATA, photoPath);
        if (getParent() == null) {
            setResult(RESULT_OK, data);
        } else {
            getParent().setResult(RESULT_OK, data);
        }
        finish();
    }
}
