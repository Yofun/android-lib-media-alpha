# TakePhotoVideoLibrary
自定义Android端`相机拍照`和`视频拍摄`的库，拍照后自动压缩，视频拍摄控制视频分辨率和视频码率，拍出来的视频才小的很多，也能保证其清晰度。该库参考[SmallVideoRecording](https://github.com/dalong982242260/SmallVideoRecording)的开源库，感谢作者[dalong982242260](https://github.com/dalong982242260)
的开源，在此库的基础之上进行了二次扩展和一些bug的修复。

|作者|邮箱|
| :-:| :-: |
|丨Rainbow丨|775183940@qq.com|

**[demo.apk下载地址](demo_release.apk)**

## 一、Download

#### Project build.gradle中

    allprojects {
        repositories {
            jcenter()
            maven {
                url "https://jitpack.io"
            }
        }
    }



#### Module build.gradle中

    dependencies {
        implementation 'com.github.HyfSunshine:TakePhotoVideoLib:0.0.4'
     }

## 二、已依赖
    // 鲁班图片压缩  https://github.com/Curzibn/Luban
    compile 'top.zibin:Luban:1.1.7'
    // GSY播放器   https://github.com/CarGuo/GSYVideoPlayer
    compile 'com.shuyu:GSYVideoPlayer:5.0.1'

## 三、使用注意事项

1. Android 6.0 运行时权限处理
   ```
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.RECORD_AUDIO"/>
    ```

2. 项目完全集成了 GSY播放器  由于ndk的so库比较大，请选择ndk进行过滤，具体使用如下：

    在gradle配置中
    ```
    android{
        ...
        defaultConfig{
            ...
            ...
            ndk {
                //APP的build.gradle设置支持的SO库架构
                //abiFilters 'armeabi', 'armeabi-v7a', 'x86'
                abiFilters 'armeabi'
            }
        }
    }
    ```

3. `minSdkVersion 16`

## 四、功能方法

### 1.自定义拍照/录像

#### 启动
1. 拍照

    ``` java
    TakePhotoVideoHelper.startTakePhoto(this, 100, savePath);
    ```
2. 拍摄视频

    ``` java
    TakePhotoVideoHelper.startTakeVideo(this, 100, savePath, 15000);
    ```
3. 拍照+视频拍摄

    ``` java
    TakePhotoVideoHelper.startTakePhotoVideo(this, 100, savePath, 15000);
    ```

#### 回调


``` java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 100 && resultCode == RESULT_OK) {
        String path = data.getStringExtra(TakePhotoVideoHelper.RESULT_DATA);
        Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
    }
}
```
### 2.开启系统相机拍照
#### 启动
``` java
SystemCapturePhoto mCapturePhoto = new SystemCapturePhoto(getActivity(), RC_OPEN_SYSTEM_CAMERA, savePath);
mCapturePhoto.openCamera();
```
#### 回调
使用压缩
``` java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_OPEN_SYSTEM_CAMERA && resultCode == RESULT_OK) {
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
                        if (dialog != null) dialog.dismiss();
                        Toast.makeText(MainActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        File yt = new File(filePath);
                        if (yt.exists()) yt.delete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (dialog != null) dialog.dismiss();
                        Toast.makeText(MainActivity.this, filePath, Toast.LENGTH_SHORT).show();
                    }
                })
                .launch();
    } else if (requestCode == RC_OPEN_SYSTEM_CAMERA && resultCode == RESULT_CANCELED) {
        // 取消拍摄  删除创建的图片文件
        String filePath = mCapturePhoto.getFileAbsolutePath();
        File file = new File(filePath);
        if (file.exists()) file.delete();
    }
}
```

### 3.调用文件管理器选择文件
#### 启动
```java
TakePhotoVideoHelper.startFileExplorer(this, RC_OPEN_FILE_EXPLORER);
```

#### 回调

``` java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RC_OPEN_FILE_EXPLORER && resultCode == RESULT_OK) {
        Uri uri = data.getData();
        String filePath = FileExplorerUriUtil.getFilePathByUri(getContext(), uri);
        Toast.makeText(this, filePath, Toast.LENGTH_SHORT).show();
    }
}
```

### 4.播放视频
``` java
TakePhotoVideoHelper.startPlayVideo(getContext(), "搁浅 周杰伦", "http://videohy.tc.qq.com/vcloud1049.tc.qq.com/1049_M2100551002SKht50WIKgb1001542292.f20.mp4?vkey=1E1091D340EAF89B357873569097EA16352BBC255E72647C79053BE7612071EC4F8E3DB672EDA93002C56833E8079641BE9C8D834A1C85B5A34E269FEFCC6A697A8EACE7BED93FDBB775DAC90C9774D8725B85524902667C&ocid=332537772");
```

## 历史更新
### 0.0.5
1. 更新文件命名，大写UUID的文件命名方式，使文件唯一

### 0.0.4
1. 更新拍摄视频码率算法
2. 新增使用系统相机拍照、打开系统文件管理器选择文件、视频预览播放
3. 更新界面启动动画

### 0.0.3
1. 修复拍照、录制视频方向的问题
2. 可以横屏、竖屏拍照和录制视频
3. 优化视频拍摄码率，默认使用720P录制





