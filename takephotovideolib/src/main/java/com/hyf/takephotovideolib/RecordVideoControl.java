package com.hyf.takephotovideolib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.hyf.takephotovideolib.view.SizeSurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * 录制视频控制类
 * Created by dalong on 2017/1/3.
 */

public class RecordVideoControl implements MediaRecorder.OnInfoListener,
        MediaRecorder.OnErrorListener, Runnable {

    public final String TAG = RecordVideoControl.class.getSimpleName();
    public static final int FLASH_MODE_OFF = 0;
    public static final int FLASH_MODE_ON = 1;
    public int flashType = FLASH_MODE_OFF;
    private int previewWidth = 640;//预览宽
    private int previewHeight = 480;//预览高

    private int pictureWidth = 1920; // 拍照宽
    private int pictureHeight = 1080; // 拍照高

    private int videoWidth = 1280; // 录像宽
    private int videoHeight = 720; // 录像高

    private int maxTime = 10000;//最大录制时间
    private long maxSize = 30 * 1024 * 1024;//最大录制大小 默认30m
    public Activity mActivity;
    private String savePath;
    public String videoPath;//保存的位置
    public SizeSurfaceView mSizeSurfaceView;
    public RecordVideoInterface mRecordVideoInterface;
    private SurfaceHolder mSurfaceHolder;
    private int mCameraId;//摄像头方向id
    private boolean isRecording;//是否录制中
    private boolean isTakeing; // 是否拍照中
    private Camera mCamera;//camera对象
    private boolean mIsPreviewing;  //是否预览
    private MediaRecorder mediaRecorder;
    private int defaultVideoFrameRate = 10;    //默认的视频帧率
    private int mCountTime;//当前录制时间

    public RecordVideoControl(Activity mActivity, String savePath, SizeSurfaceView mSizeSurfaceView, RecordVideoInterface mRecordVideoInterface) {
        this.mActivity = mActivity;
        this.savePath = savePath;
        this.mSizeSurfaceView = mSizeSurfaceView;
        this.mRecordVideoInterface = mRecordVideoInterface;
        this.mSizeSurfaceView.setUserSize(true);
        mSurfaceHolder = this.mSizeSurfaceView.getHolder();
        mSurfaceHolder.addCallback(surfaceCallBack);

        // 判断路径是否存在
        File saveFile = new File(savePath);
        if (!saveFile.exists()) saveFile.mkdirs();
        //这里设置当摄像头数量大于1的时候就直接设置后摄像头  否则就是前摄像头
        if (Build.VERSION.SDK_INT > 8) {
            if (Camera.getNumberOfCameras() > 1) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
        }
    }

    // ————————————————————————开放方法————————————————————————


    /**
     * 切换摄像头
     *
     * @param v 点击切换的view 这里处理了点击事件
     */
    public void changeCamera(final View v) {
        if (v != null)
            v.setEnabled(false);
        changeCamera();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (v != null)
                    v.setEnabled(true);
            }
        }, 1000);
    }

    /**
     * 开始录制
     *
     * @return
     */
    public boolean startRecording() {
        videoPath = savePath + File.separator + System.currentTimeMillis() + ".mp4";
        isRecording = true;
        mCountTime = 0;
        releaseRecorder();
        mCamera.stopPreview();
        mCamera.unlock();
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        int orientation = ((TakePhotoVideoActivity) mActivity).getOrientation();
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 前置
            mediaRecorder.setOrientationHint(270 - orientation);
        } else {
            mediaRecorder.setOrientationHint((90 + orientation) % 360);
        }

//        try {
//            mediaRecorder.setProfile(RecordVideoUtils.getBestCamcorderProfile(mCameraId));
//        } catch (Exception e) {
//            Log.e(TAG, "设置质量出错:" + e.getMessage());
//            customMediaRecorder();
//        }

        try {
            customMediaRecorder();
        } catch (Exception e) {
            Log.e(TAG, "设置质量出错:" + e.getMessage());
            mediaRecorder.setProfile(RecordVideoUtils.getBestCamcorderProfile(mCameraId));
        }

        // 设置帧速率，应设置在格式和编码器设置
        if (defaultVideoFrameRate != -1) {
            mediaRecorder.setVideoFrameRate(defaultVideoFrameRate);
        }
        mediaRecorder.setOnInfoListener(this);
        mediaRecorder.setOnErrorListener(this);
        // 设置最大录制时间
        mediaRecorder.setMaxFileSize(maxSize);
        mediaRecorder.setMaxDuration(maxTime);
        mediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mediaRecorder.setOutputFile(videoPath);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            if (mRecordVideoInterface != null) {
                mRecordVideoInterface.startRecord();
            }
            new Thread(this).start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 自定义的设置mediaeecorder 这里设置视频质量最低  录制出来的视频体积很小 对质量不是要求不高的可以使用
     */
    private void customMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            //设置分辨率，应设置在格式和编码器设置之后
            mediaRecorder.setVideoSize(videoWidth, videoHeight);
            mediaRecorder.setVideoEncodingBitRate(52 * defaultVideoFrameRate * 1024);
            mediaRecorder.setAudioEncodingBitRate(64100);
            mediaRecorder.setAudioSamplingRate(44100);
        }
    }


    /**
     * 停止录制
     */
    public void stopRecording(boolean isSucessed) {
        if (!isRecording) {
            return;
        }
        try {
            if (mediaRecorder != null && isRecording) {
                isRecording = false;
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                mCountTime = 0;
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
                if (isSucessed) {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onRecordFinish(videoPath);
                    }
                } else {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onRecordError();
                    }
                    updateCallBack(0);
                }

            }
        } catch (Exception e) {
            updateCallBack(0);
            Log.e(TAG, "stopRecording error:" + e.getMessage());
        }
    }


    /**
     * 拍照
     */
    public void takePhoto() {
        isTakeing = true;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    int orientation = ((TakePhotoVideoActivity) mActivity).getOrientation();
                    // 将图片保存在 DIRECTORY_DCIM 内存卡中
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Matrix matrix = new Matrix();
                    if (getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        matrix.setRotate(-90 - orientation);
                        matrix.postScale(-1, 1);
                    } else {
                        matrix.setRotate(90 + orientation);
                    }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    // 创建文件
                    File mediaStorageDir = new File(savePath);
                    if (!mediaStorageDir.exists()) {
                        mediaStorageDir.mkdirs();
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
                    FileOutputStream stream = new FileOutputStream(mediaFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    stream.flush();
                    stream.close();
                    isTakeing = false;
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onTakePhoto(mediaFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//            @Override
//            public void onPreviewFrame(byte[] data, Camera camera) {
//                camera.setPreviewCallback(null);
//                if (mCamera == null)
//                    return;
//                Camera.Parameters parameters = camera.getParameters();
//                int width = parameters.getPreviewSize().width;
//                int height = parameters.getPreviewSize().height;
//                YuvImage yuv = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
//                ByteArrayOutputStream out = new ByteArrayOutputStream();
//                yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
//                data = out.toByteArray();
//                try {
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    Matrix matrix = new Matrix();
//                    matrix.setRotate(90);
//                    if (getCameraFacing() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                        matrix.postScale(1, -1);
//                    }
//                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                    // 创建文件
//                    File mediaStorageDir = new File(savePath);
//                    if (!mediaStorageDir.exists()) {
//                        mediaStorageDir.mkdirs();
//                    }
//                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//                    File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
//                    FileOutputStream stream = new FileOutputStream(mediaFile);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                    stream.flush();
//                    stream.close();
//                    if (mRecordVideoInterface != null) {
//                        mRecordVideoInterface.onTakePhoto(mediaFile);
//                    }
//                } catch (IOException exception) {
//                    exception.printStackTrace();
//                }
        //设置这个可以达到预览的效果
//                mCamera.setPreviewCallback(this);
//            }
//        });
    }


    @Override
    public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
        Log.v(TAG, "onInfo");
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v(TAG, "最大录制时间已到");
            stopRecording(true);
        }
    }

    @Override
    public void onError(MediaRecorder mediaRecorder, int i, int i1) {
        Log.e(TAG, "recording onError:");
        Toast.makeText(mActivity, "录制失败，请重试", Toast.LENGTH_SHORT).show();
        stopRecording(false);
    }

    @Override
    public void run() {
        while (isRecording) {
            updateCallBack(mCountTime);
            try {
                mCountTime += 100;
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回调录制时间
     *
     * @param recordTime
     */
    private void updateCallBack(final int recordTime) {
        if (mActivity != null && !mActivity.isFinishing()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRecordVideoInterface != null) {
                        mRecordVideoInterface.onRecording(recordTime);
                    }
                }
            });
        }
    }


    // —————————————————————————surface view 监听方法———————————————————————————

    private SurfaceHolder.Callback surfaceCallBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            try {
                mSurfaceHolder = surfaceHolder;
                if (surfaceHolder.getSurface() == null) {
                    return;
                }
                if (mCamera == null) {
                    if (Build.VERSION.SDK_INT < 9) {
                        mCamera = Camera.open();
                    } else {
                        mCamera = Camera.open(mCameraId);
                    }
                }
                if (mCamera != null)
                    mCamera.stopPreview();
                mIsPreviewing = false;
                handleSurfaceChanged(mCamera);
                startCameraPreview(mSurfaceHolder);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                destroyCamera();
                releaseRecorder();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // —————————————————————————设置方法———————————————————————————

    /**
     * 设置录制时间
     *
     * @param maxTime
     */
    public void setMaxTime(int maxTime) {
        this.maxTime = maxTime;
    }


    /**
     * 设置录制大小
     *
     * @param maxSize
     */
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }


    /**
     * 设置录制保存路径
     *
     * @param videoPath
     */
    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    /**
     * 设置闪光灯模式
     *
     * @param flashType
     */
    public void setFlashMode(int flashType) {
        this.flashType = flashType;
        String flashMode = null;
        switch (flashType) {
            case FLASH_MODE_ON:
                flashMode = Camera.Parameters.FLASH_MODE_TORCH;
                break;
            case FLASH_MODE_OFF:
                flashMode = Camera.Parameters.FLASH_MODE_OFF;
            default:
                flashMode = Camera.Parameters.FLASH_MODE_AUTO;
                break;
        }
        if (flashMode != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFlashMode(flashMode);
            mCamera.setParameters(parameters);
        }
    }


    // —————————————————————————获取方法———————————————————————————


    /**
     * 是否录制
     *
     * @return
     */
    public boolean isRecording() {
        return isRecording;
    }

    public boolean isTakeing() {
        return isTakeing;
    }

    /**
     * 摄像头方向
     *
     * @return
     */
    public int getCameraFacing() {
        return mCameraId;
    }

    /**
     * 获取最大录制时间
     *
     * @return
     */
    public int getMaxTime() {
        return maxTime;
    }

    /**
     * 获取最大录制大小
     *
     * @return
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * 获取录制视频保存路径 文件的路径
     *
     * @return
     */
    public String getVideoPath() {
        return videoPath;
    }


    // —————————————————————内部使用的私有方法———————————————————————


    /**
     * 开启摄像头预览
     *
     * @param holder
     */
    private void startCameraPreview(SurfaceHolder holder) {
        mIsPreviewing = false;
        setCameraParameter();
        mCamera.setDisplayOrientation(90);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            destroyCamera();
            return;
        }
        mCamera.startPreview();
        mIsPreviewing = true;
        mSizeSurfaceView.setVideoDimension(previewHeight, previewWidth);
        mSizeSurfaceView.requestLayout();
    }

    /**
     * 释放 Camera
     */
    private void destroyCamera() {
        if (mCamera != null) {
            if (mIsPreviewing) {
                mCamera.stopPreview();
                mIsPreviewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.setPreviewCallbackWithBuffer(null);
            }
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 释放mediaRecorder
     */
    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    /**
     * 切换摄像头
     */
    @Deprecated
    private void changeCamera() {
        if (isRecording) {
            Toast.makeText(mActivity, "录制中无法切换", Toast.LENGTH_SHORT).show();
            return;
        }
        if (Build.VERSION.SDK_INT < 9) {
            return;
        }
        int cameraid = 0;
        if (Camera.getNumberOfCameras() > 1) {
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraid = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                cameraid = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
        } else {
            cameraid = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        if (mCameraId == cameraid) {
            return;
        } else {
            mCameraId = cameraid;
        }
        destroyCamera();
        try {
            mCamera = Camera.open(mCameraId);
            if (mCamera != null) {
                handleSurfaceChanged(mCamera);
                startCameraPreview(mSurfaceHolder);
            }
        } catch (Exception e) {
            destroyCamera();
        }

    }

    /**
     * 设置camera 的 Parameters
     */
    private void setCameraParameter() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(previewWidth, previewHeight);
        parameters.setPictureSize(pictureWidth, pictureHeight);
        parameters.setJpegQuality(100);
        if (Build.VERSION.SDK_INT < 9) {
            return;
        }
        List<String> supportedFocus = parameters.getSupportedFocusModes();
        boolean isHave = supportedFocus == null ? false :
                supportedFocus.indexOf(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) >= 0;
        if (isHave) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        parameters.setFlashMode(flashType == FLASH_MODE_ON ?
                Camera.Parameters.FLASH_MODE_TORCH :
                Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
    }


    /**
     * 计算出合适屏幕的尺寸
     *
     * @param mCamera
     */
    private void handleSurfaceChanged(Camera mCamera) {
        boolean hasSupportRate = false;
        List<Integer> supportedPreviewFrameRates = RecordVideoUtils.getSupportedPreviewFrameRates(mCamera);
        if (supportedPreviewFrameRates != null
                && supportedPreviewFrameRates.size() > 0) {
            for (int i = 0; i < supportedPreviewFrameRates.size(); i++) {
                int supportRate = supportedPreviewFrameRates.get(i);
                if (supportRate == 30) {
                    defaultVideoFrameRate = 30;
                    hasSupportRate = true;
                    break;
                }
            }
            Log.v(TAG, "supportRate::" + supportedPreviewFrameRates.toString());
            if (!hasSupportRate) {
                for (int i = 0; i < supportedPreviewFrameRates.size(); i++) {
                    int supportRate = supportedPreviewFrameRates.get(i);
                    if (supportRate <= 30) {
                        defaultVideoFrameRate = supportRate;
                        hasSupportRate = true;
                        break;
                    }
                }
            }
        }

        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        Log.v(TAG, "screen wh:" + width + "," + height);
        // 设置预览时的宽高
        {
            List<Camera.Size> resolutionList = RecordVideoUtils.getSupportedPreviewSizes(mCamera);
            if (resolutionList != null && resolutionList.size() > 0) {
                Camera.Size previewSize = null;
                boolean hasSize = false;
                // 手机支持的分辨率 列表
                Log.v(TAG, "--------support preview list-----------");
                for (int i = 0; i < resolutionList.size(); i++) {
                    Camera.Size size = resolutionList.get(i);
                    Log.v(TAG, "width:" + size.width + "   height:" + size.height);
                }

                if (!hasSize)
                    for (int i = 0; i < resolutionList.size(); i++) {
                        Camera.Size size = resolutionList.get(i);
                        if (size != null && size.width == 1920 && size.height == 1080) {
                            previewSize = size;
                            previewWidth = previewSize.width;
                            previewHeight = previewSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < resolutionList.size(); i++) {
                        Camera.Size size = resolutionList.get(i);
                        if (size != null && size.width == height && size.height == width) {
                            previewSize = size;
                            previewWidth = previewSize.width;
                            previewHeight = previewSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < resolutionList.size(); i++) {
                        Camera.Size size = resolutionList.get(i);
                        if (size != null && size.height == width) {
                            previewSize = size;
                            previewWidth = previewSize.width;
                            previewHeight = previewSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                //如果相机不支持上述分辨率，使用中分辨率
                if (!hasSize) {
                    int mediumResolution = resolutionList.size() / 2;
                    if (mediumResolution >= resolutionList.size())
                        mediumResolution = resolutionList.size() - 1;
                    previewSize = resolutionList.get(mediumResolution);
                    previewWidth = previewSize.width;
                    previewHeight = previewSize.height;
                }
            }
        }

        // 设置拍照照片的宽高
        {
            List<Camera.Size> pictureSizeList = RecordVideoUtils.getSupportedPictureSizes(mCamera);
            if (pictureSizeList != null && !pictureSizeList.isEmpty()) {
                Camera.Size pictureSize = null;
                boolean hasSize = false;
                // 手机支持的分辨率 列表
                Log.v(TAG, "---------support picture list----------");
                for (int i = 0; i < pictureSizeList.size(); i++) {
                    Camera.Size size = pictureSizeList.get(i);
                    Log.v(TAG, "width:" + size.width + "   height:" + size.height);
                }

                if (!hasSize)
                    for (int i = 0; i < pictureSizeList.size(); i++) {
                        Camera.Size size = pictureSizeList.get(i);
                        if (size != null && size.width == 1920 && size.height == 1080) {
                            pictureSize = size;
                            pictureWidth = pictureSize.width;
                            pictureHeight = pictureSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < pictureSizeList.size(); i++) {
                        Camera.Size size = pictureSizeList.get(i);
                        float scale = (float) size.height / (float) size.width;
                        if (size != null && scale == 0.5625f) {
                            pictureSize = size;
                            pictureWidth = pictureSize.width;
                            pictureHeight = pictureSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < pictureSizeList.size(); i++) {
                        Camera.Size size = pictureSizeList.get(i);
                        if (size != null && size.width == height && size.height == width) {
                            pictureSize = size;
                            pictureWidth = pictureSize.width;
                            pictureHeight = pictureSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < pictureSizeList.size(); i++) {
                        Camera.Size size = pictureSizeList.get(i);
                        if (size != null && size.height == width) {
                            pictureSize = size;
                            pictureWidth = pictureSize.width;
                            pictureHeight = pictureSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                //如果相机不支持上述分辨率，使用中分辨率
                if (!hasSize) {
                    int mediumResolution = pictureSizeList.size() / 2;
                    if (mediumResolution >= pictureSizeList.size())
                        mediumResolution = pictureSizeList.size() - 1;
                    pictureSize = pictureSizeList.get(mediumResolution);
                    pictureWidth = pictureSize.width;
                    pictureHeight = pictureSize.height;
                }
            }
        }
        // 设置拍摄视频时的宽高
        {
            List<Camera.Size> videoSizeList = RecordVideoUtils.getSupportedVideoSizes(mCamera);
            if (videoSizeList != null && !videoSizeList.isEmpty()) {
                Camera.Size videoSize = null;
                boolean hasSize = false;
                Log.v(TAG, "---------support video list----------");
                for (int i = 0; i < videoSizeList.size(); i++) {
                    Camera.Size size = videoSizeList.get(i);
                    Log.v(TAG, "width:" + size.width + "   height:" + size.height + "   scale:" + ((float) size.height / (float) size.width));
                }

                if (!hasSize)
                    for (int i = 0; i < videoSizeList.size(); i++) {
                        Camera.Size size = videoSizeList.get(i);
                        float scale = (float) size.height / (float) size.width;
                        if (size != null && size.width >= 960 && scale == 0.5625f) {
                            videoSize = size;
                            videoWidth = videoSize.width;
                            videoHeight = videoSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < videoSizeList.size(); i++) {
                        Camera.Size size = videoSizeList.get(i);
                        if (size != null && size.width == 1280 && size.height == 720) {
                            videoSize = size;
                            videoWidth = videoSize.width;
                            videoHeight = videoSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < videoSizeList.size(); i++) {
                        Camera.Size size = videoSizeList.get(i);
                        if (size != null && size.width == height && size.height == width) {
                            videoSize = size;
                            videoWidth = videoSize.width;
                            videoHeight = videoSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < videoSizeList.size(); i++) {
                        Camera.Size size = videoSizeList.get(i);
                        if (size != null && size.height == width) {
                            videoSize = size;
                            videoWidth = videoSize.width;
                            videoHeight = videoSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                if (!hasSize)
                    for (int i = 0; i < videoSizeList.size(); i++) {
                        Camera.Size size = videoSizeList.get(i);
                        float scale = (float) size.height / (float) size.width;
                        if (size != null && scale == 0.5625f) {
                            videoSize = size;
                            videoWidth = videoSize.width;
                            videoHeight = videoSize.height;
                            hasSize = true;
                            break;
                        }
                    }

                //如果相机不支持上述分辨率，使用中分辨率
                if (!hasSize) {
                    int mediumResolution = videoSizeList.size() / 2;
                    if (mediumResolution >= videoSizeList.size())
                        mediumResolution = videoSizeList.size() - 1;
                    videoSize = videoSizeList.get(mediumResolution);
                    videoWidth = videoSize.width;
                    videoHeight = videoSize.height;
                }
            }
        }

        Log.v(TAG, "preview wh:" + previewWidth + "," + previewHeight + "    picture wh:" + pictureWidth + "," + pictureHeight + "    video wh:" + videoWidth + "," + videoHeight + "    defaultVideoFrameRate:" + defaultVideoFrameRate);
    }

}
