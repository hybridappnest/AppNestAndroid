package com.ymy.image.imagepicker.cover

import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.ymy.image.R
import net.mikaelzero.mojito.interfaces.ActivityCoverLoader
import net.mikaelzero.mojito.interfaces.IMojitoActivity
import net.mikaelzero.mojito.interfaces.IMojitoFragment
import net.mikaelzero.mojito.ui.ImageMojitoActivity

/**
 * @Author:         MikaelZero
 * @CreateDate:     2020/6/17 11:29 AM
 * @Description:
 */
class SaveActivityCoverLoader : ActivityCoverLoader {
    lateinit var view: View
    var saveToLoacl: ImageView? = null
    var iv_close: ImageView? = null
    var mIMojitoActivity: IMojitoActivity? = null
    var fileUrl: String? = null
    override fun attach(context: IMojitoActivity) {
        mIMojitoActivity = context
        view = LayoutInflater.from(context.getContext()).inflate(R.layout.save_cover_layout, null)
        saveToLoacl = view.findViewById(R.id.iv_saveto_loacl)
        iv_close = view.findViewById(R.id.iv_close)
        iv_close?.setOnClickListener {
            context.getCurrentFragment().backToMin()
        }
        saveToLoacl?.setOnClickListener {
            saveToFile()
        }
    }

    private fun saveToFile() {

    }

    override fun providerView(): View {
        return view
    }

    override fun move(moveX: Float, moveY: Float) {

    }

    override fun fingerRelease(isToMax: Boolean, isToMin: Boolean) {

    }

    override fun pageChange(iMojitoFragment: IMojitoFragment, totalSize: Int, position: Int) {
        if (mIMojitoActivity is ImageMojitoActivity) {
            fileUrl = (mIMojitoActivity as ImageMojitoActivity).activityConfig.targetImageUrls?.get(position)
            fileUrl?.run {
                if (this.startsWith("http")) {
                    saveToLoacl?.visibility = View.VISIBLE
                } else {
                    saveToLoacl?.visibility = View.GONE
                }
            }
        }
    }

}