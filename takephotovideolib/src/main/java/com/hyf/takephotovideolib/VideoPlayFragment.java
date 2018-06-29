package com.hyf.takephotovideolib;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

/**
 * 视频播放
 */
public class VideoPlayFragment extends BaseRecordFragment implements View.OnClickListener {
    public static final String TAG = VideoPlayFragment.class.getSimpleName();
    public static final int FILE_TYPE_VIDEO = 0;
    public static final int FILE_TYPE_PHOTO = 1;
    private VideoView videoView;

    private String filePath;
    private int fileType;
    private ImageView photoPlay, videoUse, videoCancel;

    @SuppressLint("ValidFragment")
    public VideoPlayFragment(String filePath, int type) {
        this.filePath = filePath;
        this.fileType = type;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hyf_fragment_video_play, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        videoView = (VideoView) view.findViewById(R.id.video_play);
        photoPlay = (ImageView) view.findViewById(R.id.photo_play);
        videoCancel = (ImageView) view.findViewById(R.id.video_cancel);
        videoUse = (ImageView) view.findViewById(R.id.video_use);
        videoCancel.setOnClickListener(this);
        videoUse.setOnClickListener(this);
        if (fileType == FILE_TYPE_VIDEO) {
            videoView.setVisibility(View.VISIBLE);
            photoPlay.setVisibility(View.GONE);
            videoView.setVideoURI(Uri.parse(filePath));
            videoView.start();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoView.setVideoPath(filePath);
                    videoView.start();
                }
            });
        } else if (fileType == FILE_TYPE_PHOTO) {
            videoView.setVisibility(View.GONE);
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
}


