package com.tencent.qcloud.tim.uikit.modules.chat.layout.utils;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created on 3/25/21 15:07.
 *
 * @author:hanxueqiang
 * @version: 1.0.0
 * @desc:
 */
public class LongClickLinkMovementMethod extends LinkMovementMethod {

    public boolean hasInvokeLongClick = false;
    OnLongClickListener mOnLongClickListener;
    long downTime = 0L;

    public LongClickLinkMovementMethod(OnLongClickListener mOnLongClickListener) {
        this.mOnLongClickListener = mOnLongClickListener;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mOnLongClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(TextView widget,
                                Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            hasInvokeLongClick = false;
            downTime = System.currentTimeMillis();
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if ((System.currentTimeMillis() - downTime) > 500L) {
                if (mOnLongClickListener != null && !hasInvokeLongClick) {
                    mOnLongClickListener.onLongClick();
                    hasInvokeLongClick = true;
                }
            }
        }

        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    if (!hasInvokeLongClick) {
                        link[0].onClick(widget);
                    }
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }
                return true;
            } else {
                Selection.removeSelection(buffer);
                return false;
            }
        }
        return super.onTouchEvent(widget, buffer, event);
    }

    public interface OnLongClickListener {
        void onLongClick();
    }
}