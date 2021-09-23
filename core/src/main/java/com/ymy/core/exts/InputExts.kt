package com.ymy.core.exts

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * Created on 2020/8/13 16:04.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */

fun showSoftInput(context: Context, editText: EditText) {
    editText.requestFocus()
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(editText, 0)
}

fun hideSoftInput(context: Context, view: View) {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    view.clearFocus()
}