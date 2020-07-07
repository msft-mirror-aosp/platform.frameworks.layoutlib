/*
 * Copyright (C) 2012 The Android Open Source Project
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
package android.view;

import com.android.ide.common.rendering.api.ILayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.util.CallbacksDisposer;
import com.android.layoutlib.bridge.util.CallbacksDisposer.SessionKey;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;
import com.android.tools.layoutlib.annotations.NotNull;

import android.view.Choreographer.FrameCallback;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.android.layoutlib.bridge.impl.RenderAction.getCurrentContext;

/**
 * Delegate used to provide new implementation of a select few methods of {@link Choreographer}
 *
 * Through the layoutlib_create tool, the original  methods of Choreographer have been
 * replaced by calls to methods of the same name in this delegate class.
 *
 */
public class Choreographer_Delegate {
    static final CallbacksDisposer sCallbacksDisposer = new CallbacksDisposer(
        action -> {
            if (action instanceof FrameCallback) {
                FrameCallback callback = (FrameCallback) action;
                Choreographer.getInstance().removeFrameCallback(callback);
            } else if (action instanceof Runnable) {
                Runnable runnable = (Runnable) action;
                Choreographer.getInstance().removeCallbacksInternal_Original(
                        Choreographer.CALLBACK_ANIMATION, runnable, null);
            } else {
                Bridge.getLog().error(ILayoutLog.TAG_BROKEN,
                        "Unexpected action as " + "ANIMATION_CALLBACK", (Object) null, null);
            }
        }
    );

    @LayoutlibDelegate
    public static float getRefreshRate() {
        return 60.f;
    }

    @LayoutlibDelegate
    public static void postCallbackDelayedInternal(
            Choreographer thiz, int callbackType, Object action, Object token, long delayMillis) {
        BridgeContext context = getCurrentContext();
        if (context == null) {
            return;
        }
        if (callbackType != Choreographer.CALLBACK_ANIMATION) {
            // Ignore non-animation callbacks
            return;
        }
        if (action == null) {
            Bridge.getLog().error(ILayoutLog.TAG_BROKEN,
                    "Callback with null action", (Object) null, null);
        }
        sCallbacksDisposer.onCallbackAdded(new SessionKey(context), action);
        thiz.postCallbackDelayedInternal_Original(callbackType, action, token, delayMillis);
    }

    @LayoutlibDelegate
    public static void removeCallbacksInternal(
            Choreographer thiz, int callbackType, Object action, Object token) {
        BridgeContext context = getCurrentContext();
        if (context == null) {
            return;
        }
        if (callbackType != Choreographer.CALLBACK_ANIMATION) {
            // Ignore non-animation callbacks
            return;
        }
        if (action == null) {
            Bridge.getLog().error(ILayoutLog.TAG_BROKEN,
                    "Callback with null action", (Object) null, null);
        }
        sCallbacksDisposer.onCallbackRemoved(new SessionKey(context), action);
        thiz.removeCallbacksInternal_Original(callbackType, action, token);
    }

    public static void dispose(@NotNull BridgeContext bridgeContext) {
        sCallbacksDisposer.onDispose(new SessionKey(bridgeContext));
    }
}
