package com.hyf.takephotovideolib;

import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.OrientationEventListener;

import java.util.List;

public class TakePhotoVideoActivity extends AppCompatActivity {
    private final String TAG = "TakePhotoVideoActivity";

    public static final String MODE = "MODE";
    public static final String DURATION = "DURATION";
    public static final String SAVE_PATH = "SAVE_PATH";


    private int mode;
    private int duration;
    private String savePath;


    private CameraOrientationListener orientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo_video);
        RecordVideoUtils.setFullScreen(this);
        initIntent();
        initOrientationListener();
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.hyf_take_photo_video_fragment_container, RecordVideoFragment.newInstance(mode, duration, savePath, 1024 * 1024 * 30L))
                    .commit();
        }
    }

    private void initOrientationListener() {
        orientationListener = new CameraOrientationListener(this);
        orientationListener.enable();
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
            ((BaseRecordFragment) fragmentList.get(fragmentList.size() - 1)).finish();
        } catch (Exception e) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        orientationListener.disable();
        orientationListener = null;
        super.onDestroy();
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

    public final int getOrientation() {
        if (orientationListener != null) {
            return orientationListener.getOrientation();
        }
        return 0;
    }


    // ————————————————————————————————————————————————————

    /**
     * 当方向改变时，将调用侦听器onOrientationChanged(int)
     */
    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation = -1;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(final int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
            // 避免重复
            if (mRememberedNormalOrientation != mCurrentNormalizedOrientation) {
                mRememberedNormalOrientation = mCurrentNormalizedOrientation;
                Log.v(TAG, " current angle is " + orientation + ", orientation is " + mRememberedNormalOrientation);
            }
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }
            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public final int getOrientation() {
            return mRememberedNormalOrientation;
        }
    }

}
