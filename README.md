# ImageSelect
android 经量级选择图片框架，支持拍照，获取相册图片，可以多选，单选

### 声明
 此框架功能点有：

  1. 新增对图片列表展示的个数；
  2. 将图片的加载框架替换替换成了Glide，优化了加载图片速度；
  3. 新增多图选择时，点击图片进入产看图片模式，方便预览；
  4. 集成5.0以上权限处理问题；
  5. 支持最新android N版本。
  

### 效果

<center>
<img src="https://github.com/JackLiaoJH/ImageSelect/blob/master/images/image1.png" width="50%" height="50%" />
$ $
列表效果
</center>
<center>
<img src="https://github.com/JackLiaoJH/ImageSelect/blob/master/images/image2.png" width="50%" height="50%" />
$ $
图片详情
</center>


### Get it

- Step1： Add it in your root build.gradle at the end of repositories:
	
		allprojects {
			repositories {
				...
				maven { url "https://jitpack.io" }
			}
		}

- Step2 ： Add the dependency
	
		dependencies {
		        compile 'com.github.JackLiaoJH:ImageSelect:V1.0.2'
		}

  
### 使用

#### 简单使用

	ImageSelector selector = ImageSelector.create();
        // selector.single();  // single mode
        selector.multi();  // multi mode, default mode;
        selector.origin(mSelectPath) // original select data set, used width #.multi()
                .showCamera(showCamera)   // show camera or not. true by default
                .count(maxNum)   // max select image size, 9 by default. used width #.multi()
                .spanCount(imageSpanCount)  // image span count ，default is 3.
                .start(MainActivity.this, REQUEST_IMAGE); 
				
####  获取结果 override onActivityResult()

	ex：
    	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	        if (requestCode == REQUEST_IMAGE) {
	            if (resultCode == RESULT_OK) {
	                mSelectPath = data.getStringArrayListExtra(ImageSelector.EXTRA_RESULT);
	               // data  ..
	            }
	        }
	    }
		
		
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
