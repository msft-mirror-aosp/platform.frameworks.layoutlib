/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.os;

import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.annotation.NonNull;


public class Trace_Delegate {
    @LayoutlibDelegate
    public static boolean isTagEnabled(long traceTag) {
        return true;
    }

    @LayoutlibDelegate
    public static void traceCounter(long traceTag, @NonNull String counterName, int counterValue) {
    }

    @LayoutlibDelegate
    public static void setAppTracingAllowed(boolean allowed) {
    }

    @LayoutlibDelegate
    public static void setTracingEnabled(boolean enabled, int debugFlags) {
    }

    @LayoutlibDelegate
    public static void traceBegin(long traceTag, @NonNull String methodName) {
    }

    @LayoutlibDelegate
    public static void traceEnd(long traceTag) {
    }

    @LayoutlibDelegate
    public static void asyncTraceBegin(long traceTag, @NonNull String methodName, int cookie) {
    }

    @LayoutlibDelegate
    public static void asyncTraceEnd(long traceTag, @NonNull String methodName, int cookie) {
    }


    @LayoutlibDelegate
    public static void asyncTraceForTrackBegin(long traceTag, @NonNull String trackName,
            @NonNull String methodName, int cookie) {
    }

    @LayoutlibDelegate
    public static void asyncTraceForTrackEnd(long traceTag, @NonNull String trackName, int cookie) {
    }

    @LayoutlibDelegate
    public static void instant(long traceTag, String methodName) {

    }

    @LayoutlibDelegate
    public static void instantForTrack(long traceTag, String trackName, String methodName) {
    }

    @LayoutlibDelegate
    public static boolean isEnabled() {
        return true;
    }
}

