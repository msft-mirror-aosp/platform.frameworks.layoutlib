/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.layoutlib.bridge.util

import android.os.SystemClock_Delegate
import android.util.TimeUtils
import android.view.Choreographer
import com.android.ide.common.rendering.api.ILayoutLog

/**
 * Manages [Choreographer] callbacks. Keeps track of the currently active callbacks and allows
 * executing callbacks if their time is due.
 */
class ChoreographerCallbacks {
    // Simple wrapper around ArrayList to be able to use protected removeRange method
    private class RangeList<T> : ArrayList<T>() {
         fun removeFrontElements(n: Int) {
            removeRange(0, n)
        }
    }

    private class Callback(val mAction: Any, val mToken: Any?, val mDueTime: Long)

    private val mCallbacks = RangeList<Callback>()

    fun add(action: Any, token: Any?, delayMillis: Long) {
        synchronized(mCallbacks) {
            var idx = 0
            val now = SystemClock_Delegate.uptimeMillis()
            val dueTime = now + delayMillis
            while (idx < mCallbacks.size) {
                if (mCallbacks[idx].mDueTime > dueTime) {
                    break
                } else {
                    ++idx
                }
            }
            mCallbacks.add(idx, Callback(action, token, dueTime))
        }
    }

    fun remove(action: Any?, token: Any?) {
        synchronized(mCallbacks) {
            mCallbacks.removeIf { el: Callback ->
                ((action == null || el.mAction === action)
                        && (token == null || el.mToken === token))
            }
        }
    }

    fun execute(currentTimeMs: Long, logger: ILayoutLog) {
        val currentTimeNanos = currentTimeMs * TimeUtils.NANOS_PER_MS
        var toExecute: List<Callback>
        synchronized(mCallbacks) {
            var idx = 0
            while (idx < mCallbacks.size) {
                if (mCallbacks[idx].mDueTime > currentTimeMs) {
                    break
                } else {
                    ++idx
                }
            }
            toExecute = ArrayList(mCallbacks.subList(0, idx))
            mCallbacks.removeFrontElements(idx)
        }

        // We run the callbacks outside of the synchronized block to avoid deadlocks caused by
        // callbacks calling back into ChoreographerCallbacks.
        toExecute.forEach { executeSafely(it.mAction, currentTimeNanos, logger) }
    }

    fun clear() {
        synchronized(mCallbacks) { mCallbacks.clear() }
    }

    companion object {
        private fun executeSafely(action: Any, frameTimeNanos: Long, logger: ILayoutLog) {
            try {
                when (action) {
                    is Choreographer.FrameCallback -> action.doFrame(frameTimeNanos)
                    is Runnable -> action.run()
                    else ->
                        logger.error(ILayoutLog.TAG_BROKEN,
                                "Unexpected action as Choreographer callback", null, null)
                }
            } catch (t: Throwable) {
                logger.error(ILayoutLog.TAG_BROKEN, "Failed executing Choreographer callback", t,
                        null, null)
            }
        }
    }
}