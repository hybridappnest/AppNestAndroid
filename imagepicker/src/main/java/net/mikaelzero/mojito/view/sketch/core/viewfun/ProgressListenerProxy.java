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

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.request.DownloadProgressListener;

import java.lang.ref.WeakReference;


class ProgressListenerProxy implements DownloadProgressListener {
    @NonNull
    private WeakReference<FunctionCallbackView> viewWeakReference;

    ProgressListenerProxy(@NonNull FunctionCallbackView view) {
        this.viewWeakReference = new WeakReference<>(view);
    }

    @Override
    public void onUpdateDownloadProgress(int totalLength, int completedLength) {
        FunctionCallbackView view = viewWeakReference.get();
        if (view == null) {
            return;
        }

        boolean needInvokeInvalidate = view.getFunctions().onUpdateDownloadProgress(totalLength, completedLength);
        if (needInvokeInvalidate) {
            view.invalidate();
        }

        if (view.wrappedProgressListener != null) {
            view.wrappedProgressListener.onUpdateDownloadProgress(totalLength, completedLength);
        }
    }
}
