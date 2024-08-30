/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.layoutlib.bridge.util;

import com.android.layoutlib.bridge.bars.NavigationHandle;

import android.app.ResourcesManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.InsetsController;
import android.view.InsetsFrameProvider;
import android.view.InsetsSource;
import android.view.InsetsState;
import android.view.Surface;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.util.List;

import static android.app.WindowConfiguration.ROTATION_UNDEFINED;
import static android.inputmethodservice.InputMethodService.ENABLE_HIDE_IME_CAPTION_BAR;
import static android.view.InsetsSource.FLAG_SUPPRESS_SCRIM;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
import static android.view.WindowManager.LayoutParams.TYPE_INPUT_METHOD;

public class InsetUtil {
    public static Rect getCurrentBounds(Context context) {
        synchronized (ResourcesManager.getInstance()) {
            return context.getResources().getConfiguration().windowConfiguration.getBounds();
        }
    }


    /**
     * This applies all insets provided by the System UI.
     * This is a simplified version of what happens in
     * services/core/java/com/android/server/wm/DisplayPolicy.java.
     */
    public static void setupSysUiInsets(Context context, InsetsController insetsController,
            List<InsetsFrameProvider> insetsFrameProviders) {
        Rect currentBounds = getCurrentBounds(context);
        insetsController.onFrameChanged(currentBounds);
        InsetsState insetsState = insetsController.getState();
        // First set the window frame to all inset sources
        for (InsetsFrameProvider provider : insetsFrameProviders) {
            InsetsSource source =
                    insetsState.getOrCreateSource(provider.getId(), provider.getType());
            source.getFrame().set(currentBounds);
        }
        // Then apply the insets
        for (InsetsFrameProvider provider : insetsFrameProviders) {
            Insets insets = provider.getInsetsSize();
            InsetsSource source =
                    insetsState.getOrCreateSource(provider.getId(), provider.getType());
            source.updateSideHint(currentBounds);
            calculateInsetsFrame(source.getFrame(), insets);
        }
    }

    // Copied from services/core/java/com/android/server/wm/DisplayPolicy.java
    private static void calculateInsetsFrame(Rect inOutFrame, Insets insetsSize) {
        if (insetsSize == null) {
            inOutFrame.setEmpty();
            return;
        }
        // Only one side of the provider shall be applied. Check in the order of left - top -
        // right - bottom, only the first non-zero value will be applied.
        if (insetsSize.left != 0) {
            inOutFrame.right = inOutFrame.left + insetsSize.left;
        } else if (insetsSize.top != 0) {
            inOutFrame.bottom = inOutFrame.top + insetsSize.top;
        } else if (insetsSize.right != 0) {
            inOutFrame.left = inOutFrame.right - insetsSize.right;
        } else if (insetsSize.bottom != 0) {
            inOutFrame.top = inOutFrame.bottom - insetsSize.bottom;
        } else {
            inOutFrame.setEmpty();
        }
    }

    // Copied/adapted from packages/SystemUI/src/com/android/systemui/navigationbar/NavigationBar.java
    public static WindowManager.LayoutParams getNavBarLayoutParamsForRotation(Context context,
            View navBar, int rotation) {
        int width = WindowManager.LayoutParams.MATCH_PARENT;
        int height = WindowManager.LayoutParams.MATCH_PARENT;
        int insetsHeight = -1;
        int gravity = Gravity.BOTTOM;
        boolean navBarCanMove = true;
        WindowManager windowManager = context.getSystemService(WindowManager.class);
        if (windowManager != null) {
            Rect displaySize = windowManager.getCurrentWindowMetrics().getBounds();
            navBarCanMove = displaySize.width() != displaySize.height() &&
                    context.getResources().getBoolean(
                            com.android.internal.R.bool.config_navBarCanMove);
        }
        if (!navBarCanMove) {
            height = context.getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_frame_height);
            insetsHeight = context.getResources().getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height);
        } else {
            switch (rotation) {
                case ROTATION_UNDEFINED:
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    height = context.getResources().getDimensionPixelSize(
                            com.android.internal.R.dimen.navigation_bar_frame_height);
                    insetsHeight = context.getResources().getDimensionPixelSize(
                            com.android.internal.R.dimen.navigation_bar_height);
                    break;
                case Surface.ROTATION_90:
                    gravity = Gravity.RIGHT;
                    width = context.getResources().getDimensionPixelSize(
                            com.android.internal.R.dimen.navigation_bar_width);
                    break;
                case Surface.ROTATION_270:
                    gravity = Gravity.LEFT;
                    width = context.getResources().getDimensionPixelSize(
                            com.android.internal.R.dimen.navigation_bar_width);
                    break;
            }
        }
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(width, height,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_SPLIT_TOUCH |
                        WindowManager.LayoutParams.FLAG_SLIPPERY, PixelFormat.TRANSLUCENT);
        lp.gravity = gravity;
        lp.providedInsets = getInsetsFrameProvider(navBar, insetsHeight, context);

        lp.privateFlags |= WindowManager.LayoutParams.PRIVATE_FLAG_COLOR_SPACE_AGNOSTIC |
                WindowManager.LayoutParams.PRIVATE_FLAG_LAYOUT_SIZE_EXTENDED_BY_CUTOUT;
        lp.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        return lp;
    }

    // Copied/adapted from packages/SystemUI/src/com/android/systemui/navigationbar/NavigationBar.java
    private static InsetsFrameProvider[] getInsetsFrameProvider(View navBar, int insetsHeight,
            Context userContext) {
        final InsetsFrameProvider navBarProvider =
                new InsetsFrameProvider(navBar, 0, WindowInsets.Type.navigationBars());
        if (!ENABLE_HIDE_IME_CAPTION_BAR) {
            navBarProvider.setInsetsSizeOverrides(new InsetsFrameProvider.InsetsSizeOverride[]{
                    new InsetsFrameProvider.InsetsSizeOverride(TYPE_INPUT_METHOD, null)});
        }
        if (insetsHeight != -1) {
            navBarProvider.setInsetsSize(Insets.of(0, 0, 0, insetsHeight));
        }
        final boolean needsScrim = userContext.getResources().getBoolean(
                com.android.internal.R.bool.config_navBarNeedsScrim);
        navBarProvider.setFlags(needsScrim ? 0 : FLAG_SUPPRESS_SCRIM, FLAG_SUPPRESS_SCRIM);

        final InsetsFrameProvider tappableElementProvider =
                new InsetsFrameProvider(navBar, 0, WindowInsets.Type.tappableElement());
        final boolean tapThrough = userContext.getResources().getBoolean(
                com.android.internal.R.bool.config_navBarTapThrough);
        if (tapThrough) {
            tappableElementProvider.setInsetsSize(Insets.NONE);
        }

        final int gestureHeight = userContext.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.navigation_bar_gesture_height);
        final boolean handlingGesture = navBar instanceof NavigationHandle;
        final InsetsFrameProvider mandatoryGestureProvider =
                new InsetsFrameProvider(navBar, 0, WindowInsets.Type.mandatorySystemGestures());
        if (handlingGesture) {
            mandatoryGestureProvider.setInsetsSize(Insets.of(0, 0, 0, gestureHeight));
        }
        final int gestureInset = handlingGesture ? getUnscaledInset(userContext.getResources()) : 0;
        return new InsetsFrameProvider[]{navBarProvider, tappableElementProvider,
                mandatoryGestureProvider,
                new InsetsFrameProvider(navBar, 0, WindowInsets.Type.systemGestures()).setSource(
                        InsetsFrameProvider.SOURCE_DISPLAY).setInsetsSize(
                        Insets.of(gestureInset, 0, 0, 0)).setMinimalInsetsSizeInDisplayCutoutSafe(
                        Insets.of(gestureInset, 0, 0, 0)),
                new InsetsFrameProvider(navBar, 1, WindowInsets.Type.systemGestures()).setSource(
                        InsetsFrameProvider.SOURCE_DISPLAY).setInsetsSize(
                        Insets.of(0, 0, gestureInset, 0)).setMinimalInsetsSizeInDisplayCutoutSafe(
                        Insets.of(0, 0, gestureInset, 0))};
    }

    // Copied/adapted from packages/SystemUI/src/com/android/systemui/navigationbar/NavigationBar.java
    private static int getUnscaledInset(Resources userRes) {
        final DisplayMetrics dm = userRes.getDisplayMetrics();
        final float defaultInset =
                userRes.getDimension(com.android.internal.R.dimen.config_backGestureInset) /
                        dm.density;
        final float inset =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, defaultInset, dm);
        return (int) inset;
    }
}
