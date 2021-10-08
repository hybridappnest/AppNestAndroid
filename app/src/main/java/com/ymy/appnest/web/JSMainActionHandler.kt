package com.ymy.appnest.web

import android.content.Context
import android.content.Intent
import com.ymy.appnest.ui.MainActivity
import com.ymy.appnest.ui.gallery.BigImageDisplayActivity
import com.ymy.appnest.ui.gallery.MaxViewV2Activity
import com.ymy.appnest.ui.gallery.adapter.IGallerySourceModel
import com.ymy.core.bean.MaxBean
import com.ymy.web.custom.JSMainActionHandler

/**
 * Created on 2021/9/29 09:41.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
object JSMainActionHandler : JSMainActionHandler {
    override fun goHomePage(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)
    }

    override fun showBigImage(context: Context, ImageUrl: String) {
        BigImageDisplayActivity.actionStart(
            context,
            ImageUrl,
            ""
        )
    }

    override fun jumpToVideoPlayer(context: Context, urlNoParam: String) {
        val also = MaxBean().Data().also {
            it.coverUrl = ""
            it.type = IGallerySourceModel.video.toString()
            it.url = urlNoParam
        }
        MaxViewV2Activity.invoke(context, MaxBean().apply {
            index = 0
            data = mutableListOf(also)
        })
    }
}