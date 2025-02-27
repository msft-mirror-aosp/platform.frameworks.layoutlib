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

import android.os.PerfettoTrackEventExtra.CounterDouble;
import android.os.PerfettoTrackEventExtra.CounterInt64;
import android.os.PerfettoTrackEventExtra.Flow;
import android.os.PerfettoTrackEventExtra.Proto;

import com.android.layoutlib.bridge.impl.DelegateManager;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;
import libcore.util.NativeAllocationRegistry_Delegate;

public class PerfettoTrackEventExtra_Delegate {
    // ---- delegate manager ----
    private static final DelegateManager<PerfettoTrackEventExtra_Delegate> sManager =
            new DelegateManager<>(PerfettoTrackEventExtra_Delegate.class);
    private static long sFinalizer = -1;

    @LayoutlibDelegate
    /*package*/ static long native_init() {
        return sManager.addNewDelegate(new PerfettoTrackEventExtra_Delegate());
    }

    @LayoutlibDelegate
    /*package*/ static long native_delete() {
        synchronized (PerfettoTrackEventExtra_Delegate.class) {
            if (sFinalizer == -1) {
                sFinalizer = NativeAllocationRegistry_Delegate.createFinalizer(sManager::removeJavaReferenceFor);
            }
        }
        return sFinalizer;
    }

    @LayoutlibDelegate
    /*package*/ static CounterInt64 getCounterInt64(PerfettoTrackEventExtra thiz) {
        return null;
    }

    @LayoutlibDelegate
    /*package*/ static CounterDouble getCounterDouble(PerfettoTrackEventExtra thiz) {
        return null;
    }

    @LayoutlibDelegate
    /*package*/ static Proto getProto(PerfettoTrackEventExtra thiz) {
        return null;
    }

    @LayoutlibDelegate
    /*package*/ static Flow getFlow(PerfettoTrackEventExtra thiz) {
        return null;
    }

    @LayoutlibDelegate
    /*package*/ static Flow getTerminatingFlow(PerfettoTrackEventExtra thiz) {
        return null;
    }
}
