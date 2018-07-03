package com.hyf.takephotovideolib;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;

/**
 * 视频播放
 */
@SuppressLint("ValidFragment")
public class VideoPlayFragment extends BaseRecordFragment implements View.OnClickListener {
    public static final String TAG = VideoPlayFragment.class.getSimpleName();
    public static final int FILE_TYPE_VIDEO = 0;
    public static final int FILE_TYPE_PHOTO = 1;

    private String filePath;
    private int fileType;
    private ImageView photoPlay, videoUse, videoCancel;
    private StandardGSYVideoPlayer videoView;

    private OrientationUtils orientationUtils;

    @SuppressLint("ValidFragment")
    public VideoPlayFragment(String filePath, int type) {
        this.filePath = filePath;
        this.fileType = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hyf_fragment_video_play, container, false);
        initView(view);
        initData();
        initListener();
        return view;
    }

    private void initView(View view) {
        videoView = view.findViewById(R.id.hyf_fragment_play_video_view);
        photoPlay = (ImageView) view.findViewById(R.id.photo_play);
        videoCancel = (ImageView) view.findViewById(R.id.video_cancel);
        videoUse = (ImageView) view.findViewById(R.id.video_use);
        RelativeLayout topContentView = view.findViewById(R.id.hyf_fragment_play_video_top_content);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            topContentView.setPadding(0, RecordVideoUtils.getStatusBarHeight(getContext()), 0, 0);
        }
    }

    private void initData() {
        if (fileType == FILE_TYPE_VIDEO) {
            videoView.setVisibility(View.VISIBLE);
            //外部辅助的旋转，帮助全屏
            orientationUtils = new OrientationUtils(getActivity(), videoView);
            //初始化不打开外部的旋转
            orientationUtils.setEnable(false);
            videoView.setUp(filePath, true, "");
            videoView.getTitleTextView().setVisibility(View.GONE);
            //设置返回键
            videoView.getBackButton().setVisibility(View.GONE);
            //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
            videoView.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    orientationUtils.resolveByClick();
                }
            });
            //是否可以滑动调整
            videoView.setIsTouchWiget(false);
            // 设置是否循环播放
            videoView.setLooping(true);

            videoView.startPlayLogic();

        } else if (fileType == FILE_TYPE_PHOTO) {
            photoPlay.setVisibility(View.VISIBLE);
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            Matrix m = new Matrix();
            m.setRotate(0, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap bm1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
                photoPlay.setImageBitmap(bm1);
            } catch (OutOfMemoryError ex) {
            }
        }
    }

    private void initListener() {
        videoCancel.setOnClickListener(this);
        videoUse.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.video_use) {
            useVideo();
        } else if (i == R.id.video_cancel) {
            onCancel();
        }
    }

    /**
     * 取消
     */
    public void onCancel() {
        finish();
    }

    @Override
    public void finish() {
        //先返回正常状态
        if (orientationUtils != null && orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoView.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        if (videoView != null)
            videoView.setVideoAllCallBack(null);
        // 删除当前的
        File file = new File(filePath);
        if (file.exists()) file.delete();
        // 重新返回至预览的fragment
        ((TakePhotoVideoActivity) getActivity()).popBackStack();
    }

    /**
     * 使用
     */
    public void useVideo() {
        TakePhotoVideoActivity activity = (TakePhotoVideoActivity) getActivity();
        //防止点击过快
        if (activity != null && !activity.isFinishing()) {
            if (fileType == FILE_TYPE_VIDEO) {
                activity.returnVideoPath(filePath);
            } else {
                // Mediascanner need to scan for the image saved  通知图库刷新
                Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri fileContentUri = Uri.fromFile(new File(filePath));
                mediaScannerIntent.setData(fileContentUri);
                getActivity().sendBroadcast(mediaScannerIntent);
                activity.returnPhotoPath(filePath);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoView != null)
            videoView.onVideoPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoView != null)
            videoView.onVideoResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }
}


