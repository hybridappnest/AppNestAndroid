/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikaelzero.mojito.view.sketch.core.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SLog;

import java.text.DecimalFormat;


public class Stopwatch {
    @Nullable
    private static Stopwatch instance;
    private long startTime;
    private long lastTime;
    private long decodeCount;
    private long useTimeCount;
    @Nullable
    private StringBuilder builder;
    @Nullable
    private String logName;
    @NonNull
    private DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static Stopwatch with() {
        if (instance == null) {
            synchronized (Stopwatch.class) {
                if (instance == null) {
                    instance = new Stopwatch();
                }
            }
        }
        return instance;
    }

    public void start(@NonNull String logName) {
        this.logName = logName;
        startTime = System.currentTimeMillis();
        lastTime = startTime;
        builder = new StringBuilder();
    }

    public void record(@NonNull String nodeName) {
        if (builder != null) {
            long currentTime = System.currentTimeMillis();
            long useTime = currentTime - lastTime;
            lastTime = currentTime;

            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(nodeName).append(":").append(useTime).append("ms");
        }
    }

    public void print(@NonNull String requestId) {
        if (builder != null) {
            long totalTime = System.currentTimeMillis() - startTime;

            if (builder.length() > 0) {
                builder.append(". ");
            }

            builder.append("useTime=").append(totalTime).append("ms");

            if ((Long.MAX_VALUE - decodeCount) < 1 || (Long.MAX_VALUE - useTimeCount) < totalTime) {
                decodeCount = 0;
                useTimeCount = 0;
            }
            decodeCount++;
            useTimeCount += totalTime;

            if (SLog.isLoggable(SLog.LEVEL_DEBUG | SLog.TYPE_TIME)) {
                SLog.d(logName, "%s, average=%sms. %s",
                        builder.toString(), decimalFormat.format((double) useTimeCount / decodeCount), requestId);
            }
            builder = null;
        }
    }
}
