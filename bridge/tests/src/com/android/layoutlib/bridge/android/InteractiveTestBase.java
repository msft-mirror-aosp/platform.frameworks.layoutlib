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

package com.android.layoutlib.bridge.android;

import com.android.ide.common.rendering.api.RenderSession;
import com.android.ide.common.rendering.api.SessionParams;
import com.android.ide.common.rendering.api.Result;
import com.android.internal.lang.System_Delegate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Base class for interactive layoutlib tests inside the tests module.
 * Extends {@link RenderTestBase} to inherit resource and application paths.
 */
public abstract class InteractiveTestBase extends RenderTestBase {

    protected RenderSession mSession;

    /**
     * Starts an interactive session with the given parameters.
     */
    public void startSession(@NotNull SessionParams params) {
        // Set boot time similar to BridgeClient
        System_Delegate.setBootTimeNanos(TimeUnit.MILLISECONDS.toNanos(871732800000L));
        System_Delegate.setNanosTime(TimeUnit.MILLISECONDS.toNanos(871732800000L));

        mSession = sBridge.createSession(params);
        assertNotNull("Session creation failed", mSession);
        assertTrue("Session creation is not successful: " + mSession.getResult().getErrorMessage(),
                mSession.getResult().isSuccess());

        // Initial render
        Result result = mSession.render(50000);
        assertTrue("Render failed: " + result.getErrorMessage(), result.isSuccess());
    }

    /**
     * Disposes the session. Should be called in cleanup.
     */
    public void endSession() {
        if (mSession != null) {
            mSession.dispose();
            mSession = null;
        }
    }

    /**
     * Forwards a touch event to the layoutlib session.
     */
    public void dispatchTouchEvent(@NotNull RenderSession.TouchEventType type, int x, int y) {
        if (mSession != null) {
            mSession.triggerTouchEvent(type, x, y);
        }
    }

    /**
     * Advances the session clock to simulate passing time.
     * @param durationNanos execution time in nanoseconds
     */
    public void advanceTime(long durationNanos) {
        if (mSession != null) {
            long currentNanos = System_Delegate.nanoTime();
            long newNanos = currentNanos + durationNanos;
            System_Delegate.setNanosTime(newNanos);
            mSession.setElapsedFrameTimeNanos(durationNanos);
            mSession.executeCallbacks(newNanos);
        }
    }
}
