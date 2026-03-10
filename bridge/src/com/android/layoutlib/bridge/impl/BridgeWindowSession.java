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
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.IWindow;
import android.view.IWindowSession;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowRelayoutResult;

public class BridgeWindowSession extends IWindowSession.Default {

    private final DisplayMetrics mMetrics;

    public BridgeWindowSession(DisplayMetrics metrics) {
        mMetrics = metrics;
    }

    private void populateRelayoutResult(@NonNull WindowRelayoutResult result) {
        result.frames.compatScale = 1.0f;
        Configuration config = new Configuration();
        config.windowConfiguration.setBounds(
                new Rect(0, 0, mMetrics.widthPixels, mMetrics.heightPixels));
        config.windowConfiguration.setMaxBounds(
                new Rect(0, 0, mMetrics.widthPixels, mMetrics.heightPixels));
        result.mergedConfiguration.setConfiguration(config, config);
    }

    @Override
    public int addToDisplayAsUser(IWindow window, WindowManager.LayoutParams attrs,
            int viewVisibility, int displayId, int userId, int requestedVisibleTypes,
            android.view.InputChannel outInputChannel, WindowRelayoutResult outRelayoutResult) {
        if (outRelayoutResult != null) {
            populateRelayoutResult(outRelayoutResult);
        }
        return WindowManagerGlobal.ADD_OKAY | WindowManagerGlobal.ADD_FLAG_APP_VISIBLE;
    }

    @Override
    public int relayout(IWindow window, WindowManager.LayoutParams attrs, int requestedWidth,
            int requestedHeight, int viewVisibility, int flags, int seq, int lastSyncSeqId,
            WindowRelayoutResult outRelayoutResult, SurfaceControl outSurfaceControl) {
        if (outRelayoutResult == null) {
            return WindowManagerGlobal.RELAYOUT_RES_SURFACE_CHANGED;
        }
        populateRelayoutResult(outRelayoutResult);
        int layoutX = 0;
        int layoutY = 0;
        if (attrs != null) {
            int gravity = attrs.gravity;
            if (gravity == 0) {
                gravity = Gravity.START | Gravity.TOP;
            }
            Rect outRect = new Rect();
            Rect displayRect = new Rect(0, 0, mMetrics.widthPixels, mMetrics.heightPixels);
            Gravity.apply(gravity, requestedWidth, requestedHeight, displayRect, attrs.x,
                    attrs.y, outRect);
            layoutX = outRect.left;
            layoutY = outRect.top;
        }
        outRelayoutResult.frames.frame.set(layoutX, layoutY, layoutX + requestedWidth,
                layoutY + requestedHeight);
        outRelayoutResult.frames.displayFrame.set(0, 0, mMetrics.widthPixels,
                mMetrics.heightPixels);
        outRelayoutResult.frames.compatScale = 1.0f;
        return WindowManagerGlobal.RELAYOUT_RES_SURFACE_CHANGED;
    }

    @Override
    public int relayout2(IWindow window, WindowManager.LayoutParams attrs, int requestedWidth,
            int requestedHeight, int viewVisibility, int flags, int seq, int lastSyncSeqId,
            SurfaceControl surface, WindowRelayoutResult outRelayoutResult) {
        return relayout(window, attrs, requestedWidth, requestedHeight, viewVisibility, flags, seq,
                lastSyncSeqId, outRelayoutResult, surface);
    }
}
