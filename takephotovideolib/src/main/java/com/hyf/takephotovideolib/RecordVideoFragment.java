package com.hyf.takephotovideolib;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyf.takephotovideolib.view.RecordStartView;
import com.hyf.takephotovideolib.view.SizeSurfaceView;

import java.io.File;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * Created by  HYF on 2018/6/29.
 * Email：775183940@qq.com
 */

public class RecordVideoFragment extends BaseRecordFragment implements RecordStartView.OnRecordButtonListener, RecordVideoInterface, View.OnClickListener {
    public static final String MODE = "MODE";
    public static final String DURATION = "DURATION";
    public static final String SAVE_PATH = "SAVE_PATH";
    public static final String MAX_SIZE = "MAX_SIZE";

    private int mode;
    private int duration;
    private String savePath;
    private long maxSize;

    private final String TAG = "RecordVideoFragment";
    private SizeSurfaceView mRecordView;
    private RecordStartView mRecorderBtn;//录制按钮

    private ImageView mFacing;//前置后置切换按钮

    private ImageView mFlash;//闪光灯

    private RelativeLayout mBaseLayout;

    private RecordVideoControl mRecordControl;
    private TextView mRecordTV;
    private ImageView mCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mode = bundle.getInt(MODE);
        duration = bundle.getInt(DURATION);
        savePath = bundle.getString(SAVE_PATH);
        maxSize = bundle.getLong(MAX_SIZE);
        View view = inflater.inflate(R.layout.hyf_fragment_record_video, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initView(View view) {
        mRecordView = (SizeSurfaceView) view.findViewById(R.id.hyf_fragment_recorder_video_sv);
        mBaseLayout = (RelativeLayout) view.findViewById(R.id.hyf_fragment_recorder_video_rl_container);
        mRecorderBtn = (RecordStartView) view.findViewById(R.id.hyf_fragment_recorder_video_btn_record);
        mFacing = (ImageView) view.findViewById(R.id.hyf_fragment_recorder_video_ib_switch);
        mFlash = (ImageView) view.findViewById(R.id.hyf_fragment_recorder_video_ib_flash);
        mCancel = (ImageView) view.findViewById(R.id.hyf_fragment_recorder_video_iv_close);
        mRecordTV = (TextView) view.findViewById(R.id.hyf_fragment_recorder_video_tv_des);
        RelativeLayout topContentView = view.findViewById(R.id.hyf_fragment_recorder_video_top_container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            topContentView.setPadding(0, RecordVideoUtils.getStatusBarHeight(getContext()), 0, 0);
        }
    }


    private void initData() {
        mRecordTV.setText(RecordVideoUtils.getDesByMode(mode));
        mRecorderBtn.setMaxTime(duration);
        mRecorderBtn.setMode(mode);

        mRecordControl = new RecordVideoControl(getActivity(), savePath, mRecordView, this);
        mRecordControl.setMaxSize(maxSize);
        mRecordControl.setMaxTime(duration);

        setupFlashMode();
    }


    private void initListener() {
        mRecorderBtn.setOnRecordButtonListener(this);
        mCancel.setOnClickListener(this);
        mFlash.setOnClickListener(this);
        mFacing.setOnClickListener(this);
    }

    public static RecordVideoFragment newInstance(int mode, int duration, String savePath, long maxSize) {
        Bundle args = new Bundle();
        args.putInt(MODE, mode);
        args.putInt(DURATION, duration);
        args.putString(SAVE_PATH, savePath);
        args.putLong(MAX_SIZE, maxSize);
        RecordVideoFragment fragment = new RecordVideoFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * 检测开启闪光灯
     */
    private void setupFlashMode() {
        if (mRecordControl.getCameraFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mFlash.setVisibility(View.GONE);
            return;
        } else {
            mFlash.setVisibility(View.VISIBLE);
        }

        final int res;
        switch (mRecordControl.flashType) {
            case RecordVideoControl.FLASH_MODE_ON:
                res = R.drawable.hyf_ic_take_photo_video_flash_on_24dp;
                break;
            case RecordVideoControl.FLASH_MODE_OFF:
                res = R.drawable.hyf_ic_take_photo_video_flash_off_24dp;
                break;
            default:
                res = R.drawable.hyf_ic_take_photo_video_flash_off_24dp;
        }
        mFlash.setImageResource(res);
    }

    // ——————————————————————————————————————————————————————

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.hyf_fragment_recorder_video_iv_close) {
            finish();
        } else if (i == R.id.hyf_fragment_recorder_video_ib_flash) {
            if (mRecordControl.getCameraFacing() == android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK) {
                mRecordControl.setFlashMode(mRecordControl.flashType == RecordVideoControl.FLASH_MODE_ON
                        ? RecordVideoControl.FLASH_MODE_OFF
                        : RecordVideoControl.FLASH_MODE_ON);
            }
            setupFlashMode();
        } else if (i == R.id.hyf_fragment_recorder_video_ib_switch) {
            mRecordControl.changeCamera(mFacing);
            setupFlashMode();
        }
    }

    @Override
    public void finish() {
        getActivity().finish();
    }

    // ——————————————————————————————————————————————————————

    @Override
    public void onStartRecord() {
        mRecordControl.startRecording();
    }

    @Override
    public void onStopRecord() {
        mRecordControl.stopRecording(true);
    }

    @Override
    public void onTakePhoto() {
        if (!mRecordControl.isTakeing())
            mRecordControl.takePhoto();
    }

    // —————————————————————————————————————————————————————
    @Override
    public void startRecord() {
        Log.v(TAG, "startRecord");
    }

    @Override
    public void onRecording(long recordTime) {
        Log.v(TAG, "onRecording:" + recordTime);
        if (recordTime / 1000 >= 1) {
            mRecordTV.setText(recordTime / 1000 + "秒");
        }
    }

    @Override
    public void onRecordFinish(String videoPath) {
        Log.v(TAG, "onRecordFinish:" + videoPath);
        // 预览刚刚拍摄的视频
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.hyf_take_photo_video_fragment_container,
                        new VideoPlayFragment(videoPath, VideoPlayFragment.FILE_TYPE_VIDEO),
                        VideoPlayFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRecordError() {
        Log.v(TAG, "onRecordError");
    }

    @Override
    public void onTakePhoto(final File photo) {
        Log.v(TAG, "onTakePhoto");
        // 将图片保存在 DIRECTORY_DCIM 内存卡中
        try {
            // 压缩图片
            Luban.with(getContext())
                    .load(photo)
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
                            if (dialog != null) dialog.dismiss();
                            //切换fragment 预览刚刚的拍照
                            getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.hyf_take_photo_video_fragment_container,
                                            new VideoPlayFragment(file.getAbsolutePath(), VideoPlayFragment.FILE_TYPE_PHOTO),
                                            VideoPlayFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                            if (photo.exists()) photo.delete();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (dialog != null) dialog.dismiss();
                            Log.v(TAG, "compress photo error:::::" + e.getMessage());
                            // 如果压缩失败  直接使用原图
                            getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.hyf_take_photo_video_fragment_container,
                                            new VideoPlayFragment(photo.getAbsolutePath(), VideoPlayFragment.FILE_TYPE_PHOTO),
                                            VideoPlayFragment.TAG)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    })
                    .launch();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
