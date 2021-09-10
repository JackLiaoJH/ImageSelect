package com.jhworks.imageselect.utils

import com.jhworks.library.core.vo.ImageInfoVo
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 * @author jackson
 * @version 1.0
 * @date 2021/9/9 16:59
 */
object DataProvider {
    fun getImageUrls(): ArrayList<String> {
        val list: ArrayList<String> = ArrayList()
        list.add("https://wx3.sinaimg.cn/mw690/006qDXTKgy1fx8q6x78hkj30c80953yt.jpg")
        //网络长图,包括横向和纵向的长图
        list.add("https://wx2.sinaimg.cn/mw690/005MctNqgy1fx674gpkbvj30gf4k2wzp.jpg")
        list.add("https://wx2.sinaimg.cn/mw690/0062Xesrgy1fx8uu4ltdyj30j635g47x.jpg")
        list.add("https://wx1.sinaimg.cn/mw690/0062Xesrgy1fx8uu5d856j30j64v0h7z.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/0062Xesrgy1fx8uu55neej30j63sq4le.jpg")
        list.add("https://wx1.sinaimg.cn/mw690/0062Xesrgy1fx8uu5p4stj30j66d6b29.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/0062Xesrgy1fx8uu5tkicj30j65fu4qp.jpg")
        list.add("https://raw.githubusercontent.com/Awent/PhotoPick-Master/master/pictrue/WechatIMG20.jpeg")
        list.add("https://raw.githubusercontent.com/Awent/PhotoPick-Master/master/pictrue/WechatIMG21.jpeg")

        //网络图片
        list.add("https://wx1.sinaimg.cn/mw690/7325792bly1fx9oma87k1j21900u04jf.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/7325792bly1fx9oma3jhpj21900u04h0.jpg")
        list.add("https://wx2.sinaimg.cn/mw690/7325792bly1fx9oylai59j22040u0hdu.jpg")
        list.add("https://wx4.sinaimg.cn/mw690/0061VhPpgy1fx9x54op3oj30u00pc1kx.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/006cZ2iWgy1fskddvgmwoj30mq0vu7ik.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/006l0mbogy1fi68udt62wj30u010h79j.jpg")
        list.add("https://wx4.sinaimg.cn/mw690/006l0mbogy1fi68ud4uwwj30u00zrtdj.jpg")
        list.add("https://wx1.sinaimg.cn/mw690/006DQg3tly1fuvkrwjforg30b40alb29.gif")
        list.add("https://wx2.sinaimg.cn/mw690/006DQg3tly1fwhen1vuudg30go09e7wi.gif")
        list.add("https://wx1.sinaimg.cn/mw690/006DQg3tly1fuvkrxsntcg30g409xx6q.gif")
        list.add("https://wx1.sinaimg.cn/mw690/006DQg3tly1fuvks859e4g30dw0atqv8.gif")
        return list
    }

    fun getBigImageList(): ArrayList<ImageInfoVo> {
        return getImageUrls().map { ImageInfoVo(it, it) }.toMutableList() as ArrayList<ImageInfoVo>
    }

    /**
     * 大图
     *
     * @return
     */
    fun getBigImgUrls(): ArrayList<String> {
        val list: ArrayList<String> = ArrayList()
        list.add("https://wx3.sinaimg.cn/mw690/0061VhPpgy1fx8w3jn6o8j30u00qd774.jpg")
        list.add("https://wx4.sinaimg.cn/mw690/006mQAf4ly1fx8yoea4zuj30vy1bzk0x.jpg")
        list.add("https://wx1.sinaimg.cn/mw690/006mQAf4ly1fx8yoipc10j30vy1bzwnt.jpg")
        return list
    }

    /**
     * 小图，这里随便找两张小图的
     *
     * @return
     */
    fun getSmallImgUrls(): ArrayList<String> {
        val list: ArrayList<String> = ArrayList()
        list.add("https://wx3.sinaimg.cn/mw690/006qDXTKgy1fx8q6x78hkj30c80953yt.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/006qDXTKgy1fx8q6x78hkj30c80953yt.jpg")
        list.add("https://wx3.sinaimg.cn/mw690/006qDXTKgy1fx8q6x78hkj30c80953yt.jpg")
        return list
    }

    fun getBigImgList(): ArrayList<String> {
        val list: ArrayList<String> = ArrayList()
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG37.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG38.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG39.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG40.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG41.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG42.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG43.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG44.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG45.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG46.jpeg")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG47.jpeg")
        return list
    }

    fun getSmallImgList(): ArrayList<String> {
        val scale = "?imageView2/0/w/200"
        val list: ArrayList<String> = ArrayList()
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG37.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG38.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG39.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG40.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG41.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG42.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG43.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG44.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG45.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG46.jpeg$scale")
        list.add("https://cdn.mango218.com/files/imgs/WechatIMG47.jpeg$scale")
        return list
    }
}