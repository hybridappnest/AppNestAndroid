/*
 * Copyright (C) 2019 Peng fei Pan <panpfpanpf@outlook.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikaelzero.mojito.view.sketch.core.viewfun;

import android.view.View;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;


class OnClickListenerProxy implements View.OnClickListener {
    @NonNull
    private WeakReference<FunctionCallbackView> viewWeakReference;

    OnClickListenerProxy(@NonNull FunctionCallbackView view) {
        this.viewWeakReference = new WeakReference<>(view);
    }

    @Override
    public void onClick(@NonNull View v) {
        FunctionCallbackView view = viewWeakReference.get();
        if (view == null) {
            return;
        }

        if (view.getFunctions().clickRetryFunction != null && view.getFunctions().clickRetryFunction.onClick(v)) {
            return;
        }

        if (view.getFunctions().clickPlayGifFunction != null && view.getFunctions().clickPlayGifFunction.onClick(v)) {
            return;
        }

        if (view.wrappedClickListener != null) {
            view.wrappedClickListener.onClick(v);
        }
    }

    boolean isClickable() {
        FunctionCallbackView view = viewWeakReference.get();
        if (view == null) {
            return false;
        }

        if (view.getFunctions().clickRetryFunction != null && view.getFunctions().clickRetryFunction.isClickable()) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (view.getFunctions().clickPlayGifFunction != null && view.getFunctions().clickPlayGifFunction.isClickable()) {
            return true;
        }

        return view.wrappedClickListener != null;
    }
}
