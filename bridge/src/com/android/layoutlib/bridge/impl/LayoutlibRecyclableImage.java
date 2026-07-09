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
import com.android.ide.common.rendering.api.RecyclableImage;
import com.android.ide.common.rendering.api.ILayoutLog;
import com.android.layoutlib.bridge.Bridge;

import java.awt.image.BufferedImage;
import java.lang.ref.Cleaner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An implementation of {@link RecyclableImage} for Layoutlib.
 */
public final class LayoutlibRecyclableImage implements RecyclableImage {
    private static final Cleaner sCleaner = Cleaner.create();

    private final int mLogicalWidth;
    private final int mLogicalHeight;
    private final BufferedImage mLogicalSubImage;
    private final Cleaner.Cleanable mCleanable;
    private final CleanUpTask mCleanUpTask;
    private final AtomicBoolean mIsClosed = new AtomicBoolean(false);

    /**
     * A static inner class representing the resource-releasing action.
     * It does NOT reference {@code LayoutlibRecyclableImage} to avoid leaks.
     */
    private static final class CleanUpTask implements Runnable {
        private final BufferedImage mPhysicalBuffer;
        private final SessionBufferPool mBufferPool;
        private final AtomicBoolean mIsReleased = new AtomicBoolean(false);
        private volatile boolean mWarnOnRelease = true;

        private CleanUpTask(@NonNull BufferedImage physicalBuffer,
                @NonNull SessionBufferPool bufferPool) {
            mPhysicalBuffer = physicalBuffer;
            mBufferPool = bufferPool;
        }

        private void disableWarning() {
            mWarnOnRelease = false;
        }

        @Override
        public void run() {
            // Ensure resource is released exactly once, either by direct close() or by the Cleaner thread.
            if (mIsReleased.compareAndSet(false, true)) {
                if (mWarnOnRelease) {
                    Bridge.getLog().warning(
                            ILayoutLog.TAG_BROKEN,
                            "LayoutlibRecyclableImage was not closed before garbage collection. Backing buffer leaked.",
                            null,
                            null
                    );
                }
                mBufferPool.release(mPhysicalBuffer);
            }
        }
    }

    public LayoutlibRecyclableImage(
            int logicalWidth,
            int logicalHeight,
            @NonNull BufferedImage physicalBuffer,
            @NonNull SessionBufferPool bufferPool) {
        mLogicalWidth = logicalWidth;
        mLogicalHeight = logicalHeight;

        // Expose zero-copy sub-image to discard physical padding/oversizing
        mLogicalSubImage = physicalBuffer.getSubimage(0, 0, logicalWidth, logicalHeight);

        mCleanUpTask = new CleanUpTask(physicalBuffer, bufferPool);
        mCleanable = sCleaner.register(this, mCleanUpTask);
    }

    @Override
    public int getWidth() {
        return mLogicalWidth;
    }

    @Override
    public int getHeight() {
        return mLogicalHeight;
    }

    @NonNull
    @Override
    public BufferedImage getImage() {
        return mLogicalSubImage;
    }

    @Override
    public void close() {
        if (mIsClosed.compareAndSet(false, true)) {
            mCleanUpTask.disableWarning();
            mCleanable.clean();
        }
    }
}
