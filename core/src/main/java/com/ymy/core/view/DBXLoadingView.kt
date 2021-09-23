package com.ymy.core.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.wang.avi.AVLoadingIndicatorView
import com.ymy.core.R

/**
 * Created on 2020/7/20 13:27.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class DBXLoadingView constructor(root: FrameLayout) {

    private val context: Context = root.context
    private val loadingView: View
    private var loadingText: TextView
    private var loadingAnim: AVLoadingIndicatorView

    init {
        loadingView = LayoutInflater.from(context).inflate(R.layout.layout_loading, root, false)
        loadingView.setBackgroundColor(context.resources.getColor(R.color.black30tan))
        loadingText = loadingView.findViewById(R.id.tv_loading)
        loadingAnim = loadingView.findViewById(R.id.loadingView)
        root.addView(loadingView)
        hide()
    }

    fun setLoadingText(str: String) {
        loadingText.text = str
    }

    fun hide() {
        loadingAnim.visibility = View.GONE
        loadingView.visibility = View.GONE
    }

    fun show() {
        loadingAnim.visibility = View.VISIBLE
        loadingView.visibility = View.VISIBLE
    }

}