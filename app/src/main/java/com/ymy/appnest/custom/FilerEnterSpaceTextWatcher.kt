package com.ymy.appnest.custom

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Created on 2020/9/8 20:28.
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
class FilerEnterSpaceTextWatcher constructor(val editText: EditText) : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if (s.toString().contains(" ")) {
            val str: List<String> = s.toString().split(" ")
            var str1 = ""
            for (i in str.indices) {
                str1 += str[i]
            }
            editText.setText(str1)
            editText.setSelection(start)
        }
    }

    override fun afterTextChanged(s: Editable?) {
    }

}