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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.awt.image.BufferedImage;

/**
 * Tests for {@link LayoutlibRecyclableImage} focusing on resource release via direct close and GC-cleaners.
 */
public class LayoutlibRecyclableImageTest {

    @Test
    public void testCloseReleasesBuffer() {
        SessionBufferPool pool = new SessionBufferPool();
        BufferedImage buffer = pool.acquire(100, 100);
        LayoutlibRecyclableImage recyclableImage = new LayoutlibRecyclableImage(100, 100, buffer, pool);

        recyclableImage.close();

        // Acquiring again should return the exact same buffer instance from the pool
        BufferedImage recycled = pool.acquire(100, 100);
        assertSame(buffer, recycled);
    }

    @Test
    public void testGcCleansUpLeakedImage() throws Exception {
        SessionBufferPool pool = new SessionBufferPool();
        BufferedImage buffer = pool.acquire(100, 100);

        // Create the image and let it go out of scope
        createAndLeakImage(buffer, pool);

        // Force GC repeatedly until the buffer is returned to the pool (up to a timeout)
        boolean recycled = false;
        for (int i = 0; i < 50; i++) {
            System.gc();
            // Try to acquire from the pool
            BufferedImage testAcquire = pool.acquire(100, 100);
            if (testAcquire == buffer) {
                recycled = true;
                break;
            }
            Thread.sleep(10);
        }

        assertTrue("Backing buffer was not recycled by Cleaner after GC", recycled);
    }

    private void createAndLeakImage(BufferedImage buffer, SessionBufferPool pool) {
        LayoutlibRecyclableImage image = new LayoutlibRecyclableImage(100, 100, buffer, pool);
        assertNotNull(image);
    }
}
