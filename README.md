# ImageSelect
android 经量级选择图片框架，支持拍照，获取相册图片,获取本地视频，可以多选，单选

### 声明
 此框架功能点有：

  1. 新增对图片列表展示的个数；
  2. 将图片的加载框架默认不处理，需要自己去实现IEngine接口来实现图片加载，详情查看demo里面的GlideEngine.kt类；
  3. 新增多图选择时，点击图片进入产看图片模式，方便预览；
  4. 集成5.0以上权限处理问题,处理android q兼容问题；
  5. 支持Androidx;
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
```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
        jcenter()
        mavenCentral()
        google()
    }
}
```

- step2: Add the dependency
```groovy
implementation 'com.github.JackLiaoJH:ImageSelect:1.2.1'
```

  
### 使用

#### 初始化
```kotlin
// 实现IEngine接口,如glide加载
class GlideEngine : IEngine {
    override fun loadImage(imageView: ImageView, uiConfig: MediaUiConfigVo) {
        Glide.with(imageView)
                .load(uiConfig.path)
                .placeholder(uiConfig.placeholderResId)
                .error(uiConfig.errorResId)
                .override(uiConfig.width, uiConfig.height)
                .centerCrop()
                .into(imageView)
    }
}

//在Application里面初始化框架图片加载逻辑:
override fun onCreate() {
    super.onCreate()
    ImageSelector.setImageEngine(GlideEngine())
}
```

#### 简单使用
```kotlin
//打开照片列表
 ImageSelector.startImageAction(
    this, IMAGE_CODE, MediaSelectConfig.Builder()
        .setShowCamera(true) //是否展示打开摄像头拍照入口，只针对照片，视频列表无效
        .setOpenCameraOnly(isOpneCameraOnly) //是否只是打开摄像头拍照而已
        .setOriginData(mSelectPath) //已选择图片地址
        .setMaxCount(9)//选择最大集合，默认9
        .setImageSpanCount(4) //自定义列表展示个数，默认3
        .setPlaceholderResId(R.mipmap.ic_launcher) //预览图
        .build()
    )

//打开视频列表
 ImageSelector.startVideoAction(
    this, IMAGE_CODE, MediaSelectConfig.Builder()
        .setOriginData(mSelectPath) //已选择图片地址
        .setImageSpanCount(4) //自定义列表展示个数，默认3
        .build()
    )
```
				
### 获取结果，重写onActivityResult()方法
```kotlin
 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                mSelectPath = ImageSelector.getSelectResults(data)
                Log.e("ImageSelect", "结果： $mSelectPath")
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
