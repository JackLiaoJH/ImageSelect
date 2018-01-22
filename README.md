# ImageSelect
android 经量级选择图片框架，支持拍照，获取相册图片，可以多选，单选

### 声明
 此框架功能点有：

  1. 新增对图片列表展示的个数；
  2. 将图片的加载框架替换替换成了Glide，优化了加载图片速度；
  3. 新增多图选择时，点击图片进入产看图片模式，方便预览；
  4. 集成5.0以上权限处理问题；
  5. 支持最新android N版本;
  6. 支持视频加载展示，优化了api接口调用。
  

### 效果

<center>
列表效果
	
<img src="https://github.com/JackLiaoJH/ImageSelect/blob/master/images/image1.png" width="50%" height="50%" />
</center>

<center>
图片详情
	
<img src="https://github.com/JackLiaoJH/ImageSelect/blob/master/images/image2.png" width="50%" height="50%" />
</center>


### Get it
- step1: add to your project build.gradle
```
buildscript {
    repositories {
     maven { url "https://jitpack.io" }
     ...
     }
}
```

- step2: Add the dependency
```
dependencies {
		        compile 'com.jhworks.library:ImageSelect:1.1.1'
		}
```

  
### 使用

#### 简单使用
```java
MediaSelectConfig config = new MediaSelectConfig()
                .setSelectMode(mChoiceMode.getCheckedRadioButtonId() == R.id.single ?
                        MediaSelectConfig.MODE_SINGLE : MediaSelectConfig.MODE_MULTI) //设置选择图片模式，单选与多选
                .setOriginData(mSelectPath) //已选择图片地址
                .setShowCamera(showCamera) //是否展示打开摄像头拍照入口，只针对照片，视频列表无效
                .setOpenCameraOnly(isOpneCameraOnly) //是否只是打开摄像头拍照而已
                .setMaxCount(maxNum) //选择最大集合，默认9
                .setImageSpanCount(imageSpanCount) //自定义列表展示个数，默认3
;

//打开照片列表
ImageSelector.create()
                .setMediaConfig(config)
                .startImageAction(MainActivity.this, REQUEST_IMAGE);

//打开视频列表
 ImageSelector.create()
                .setMediaConfig(config)
                .startVideoAction(MainActivity.this, REQUEST_IMAGE);
```
				
### 获取结果，重写onActivityResult()方法
```java
 @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     if (requestCode == REQUEST_IMAGE) {
         if (resultCode == RESULT_OK) {
            mSelectPath = data.getStringArrayListExtra(ImageSelector.EXTRA_RESULT);
            ...
        }
    }
}
```

#### License

>       
	/*
	 * Copyright (c) 2016 LiaoJH. 
	 * Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
	 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan. 
	 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna. 
	 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus. 
	 * Vestibulum commodo. Ut rhoncus gravida arcu. 
	 */
