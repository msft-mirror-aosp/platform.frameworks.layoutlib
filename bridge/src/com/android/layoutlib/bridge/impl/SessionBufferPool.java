/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.layoutlib.bridge.impl;

import android.annotation.NonNull;
import android.annotation.Nullable;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A pool of {@link BufferedImage} instances to be reused during rendering.
 * This avoids frequent allocations and GC pressure.
 */
public class SessionBufferPool {
    private static final int MAX_GLOBAL_POOL_SIZE = 3;

    private final List<BufferedImage> mPoolList = new ArrayList<>();

    @NonNull
    public BufferedImage acquire(int width, int height) {
        synchronized (mPoolList) {
            int bestIndex = -1;
            long bestAreaDiff = Long.MAX_VALUE;

            for (int i = 0; i < mPoolList.size(); i++) {
                BufferedImage buf = mPoolList.get(i);
                if (buf.getWidth() >= width && buf.getHeight() >= height) {
                    long areaDiff = ((long) buf.getWidth() * buf.getHeight()) - ((long) width * height);
                    if (areaDiff < bestAreaDiff) {
                        bestAreaDiff = areaDiff;
                        bestIndex = i;
                    }
                }
            }

            if (bestIndex != -1) {
                return mPoolList.remove(bestIndex);
            }
        }

        // Pool miss or no buffer of sufficient size: allocate with dimension padding (next multiple of 128)
        int paddedWidth = ((width + 127) / 128) * 128;
        int paddedHeight = ((height + 127) / 128) * 128;
        return new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB_PRE);
    }

    public void release(@Nullable BufferedImage buffer) {
        if (buffer == null || buffer.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
            return;
        }

        synchronized (mPoolList) {
            if (mPoolList.size() >= MAX_GLOBAL_POOL_SIZE) {
                // Evict the oldest buffer (FIFO)
                mPoolList.removeFirst();
            }
            mPoolList.add(buffer);
        }
    }

    public void clear() {
        synchronized (mPoolList) {
            mPoolList.clear();
        }
    }
}
