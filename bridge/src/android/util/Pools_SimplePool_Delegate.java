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

package android.util;

import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.util.Pools.SimplePool;

import java.lang.ref.WeakReference;

public class Pools_SimplePool_Delegate {
    @LayoutlibDelegate
    public static <T> T acquire(SimplePool<T> thiz) {
        if (thiz.mPoolSize > 0) {
            final int lastPooledIndex = thiz.mPoolSize - 1;
            WeakReference<T> instance = (WeakReference<T>) thiz.mPool[lastPooledIndex];
            thiz.mPool[lastPooledIndex] = null;
            thiz.mPoolSize--;
            return instance.get();
        }
        return null;
    }

    @LayoutlibDelegate
    public static <T> boolean release(SimplePool<T> thiz, T instance) {
        if (thiz.mPoolSize < thiz.mPool.length) {
            thiz.mPool[thiz.mPoolSize] = new WeakReference<T>(instance);
            thiz.mPoolSize++;
            return true;
        }
        return false;
    }
}
