/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.layoutlib.bridge.bars;

import com.android.ide.common.rendering.api.ILayoutLog;
import com.android.ide.common.rendering.api.RenderResources;
import com.android.internal.R;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.android.BridgeContext;
import com.android.layoutlib.bridge.impl.ResourceHelper;
import com.android.resources.Density;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.InsetsFrameProvider;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static android.graphics.Color.WHITE;
import static android.os._Original_Build.VERSION_CODES.M;
import static android.view.Surface.ROTATION_0;
import static android.view.WindowInsets.Type.mandatorySystemGestures;
import static android.view.WindowInsets.Type.statusBars;
import static android.view.WindowInsets.Type.tappableElement;
import static android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
import static android.view.WindowManager.LayoutParams.PRIVATE_FLAG_COLOR_SPACE_AGNOSTIC;
import static com.android.layoutlib.bridge.bars.Config.getTimeColor;
import static com.android.layoutlib.bridge.bars.Config.isGreaterOrEqual;

public class StatusBar extends CustomBar {

    /** Status bar background color attribute name. */
    private static final String ATTR_COLOR = "statusBarColor";
    /** Attribute for translucency property. */
    public static final String ATTR_TRANSLUCENT = "windowTranslucentStatus";

    private DisplayCutout mDisplayCutout;
    private int mStatusBarHeight;

    @SuppressWarnings("UnusedParameters")
    public StatusBar(BridgeContext context, Density density, boolean isRtl, boolean rtlEnabled,
            boolean isEdgeToEdge, int simulatedPlatformVersion) {
        // FIXME: if direction is RTL but it's not enabled in application manifest, mirror this bar.
        super(context, LinearLayout.HORIZONTAL, "status_bar.xml", simulatedPlatformVersion);

        // FIXME: use FILL_H?
        setGravity(Gravity.START | Gravity.TOP | Gravity.RIGHT);

        int backgroundColor;
        if (isEdgeToEdge) {
            backgroundColor = Color.TRANSPARENT;
        } else {
            int color = getBarColor(ATTR_COLOR, ATTR_TRANSLUCENT);
            backgroundColor = color == 0 ? Config.getStatusBarColor(simulatedPlatformVersion) : color;
        }
        setBackgroundColor(backgroundColor);

        List<ImageView> icons = new ArrayList<>(2);
        TextView clockView = null;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            if (child instanceof ImageView) {
                icons.add((ImageView) child);
            } else if (child instanceof TextView) {
                clockView = (TextView) child;
            }
        }

        if (icons.size() != 2 || clockView == null) {
            Bridge.getLog().error(ILayoutLog.TAG_BROKEN, "Unable to initialize statusbar", null,
                    null, null);
            return;
        }

        int foregroundColor;
        if (isEdgeToEdge) {
            boolean isLightTheme =
                    ResourceHelper.getBooleanThemeFrameworkAttrValue(context.getRenderResources(),
                            "isLightTheme", false);
            foregroundColor = isLightTheme ? DARK_ICON_COLOR : LIGHT_ICON_COLOR;
        } else {
            foregroundColor = getForegroundColor(simulatedPlatformVersion);
        }
        // Cannot access the inside items through id because no R.id values have been
        // created for them.
        // We do know the order though.
        loadIcon(icons.get(0), "stat_sys_wifi_signal_4_fully."
                        + Config.getWifiIconType(simulatedPlatformVersion), density,foregroundColor);
        loadIcon(icons.get(1), "stat_sys_battery_100.png", density, foregroundColor);
        clockView.setText(Config.getTime(simulatedPlatformVersion));
        clockView.setTextColor(foregroundColor);
    }

    private int getForegroundColor(int platformVersion) {
        if (isGreaterOrEqual(platformVersion, M)) {
            RenderResources renderResources = getContext().getRenderResources();
            boolean translucentBackground =
                    ResourceHelper.getBooleanThemeFrameworkAttrValue(renderResources,
                            ATTR_TRANSLUCENT, false);
            if (translucentBackground) {
                return WHITE;
            }
            boolean drawnByWindow =
                    ResourceHelper.getBooleanThemeFrameworkAttrValue(renderResources,
                            "windowDrawsSystemBarBackgrounds", false);
            if (drawnByWindow) {
                boolean lightStatusBar =
                        ResourceHelper.getBooleanThemeFrameworkAttrValue(renderResources,
                                "windowLightStatusBar", false);
                return lightStatusBar ? DARK_ICON_COLOR : LIGHT_ICON_COLOR;
            }
            return WHITE;
        } else {
            return getTimeColor(platformVersion);
        }
    }

    @Override
    protected TextView getStyleableTextView() {
        return null;
    }

    // Copied/adapted from packages/SystemUI/src/com/android/systemui/statusbar/window/StatusBarWindowController.java
    public WindowManager.LayoutParams getBarLayoutParams() {
        return getBarLayoutParamsForRotation(mContext.getDisplay().getRotation());
    }

    // Copied/adapted from packages/SystemUI/src/com/android/systemui/statusbar/window/StatusBarWindowController.java
    private WindowManager.LayoutParams getBarLayoutParamsForRotation(int rotation) {
        int height = getStatusBarHeightForRotation(mContext, rotation);
        WindowManager.LayoutParams lp = createWindowParams(height);
        final InsetsFrameProvider gestureInsetsProvider =
                new InsetsFrameProvider(this, 0, mandatorySystemGestures());
        final int safeTouchRegionHeight = mContext.getResources().getDimensionPixelSize(
                com.android.internal.R.dimen.display_cutout_touchable_region_size);
        if (safeTouchRegionHeight > 0) {
            gestureInsetsProvider.setMinimalInsetsSizeInDisplayCutoutSafe(
                    Insets.of(0, safeTouchRegionHeight, 0, 0));
        }
        lp.providedInsets = new InsetsFrameProvider[]{
                new InsetsFrameProvider(this, 0, statusBars()).setInsetsSize(getInsets(height)),
                new InsetsFrameProvider(this, 0, tappableElement()).setInsetsSize(
                        getInsets(height)), gestureInsetsProvider};
        return lp;

    }

    // Copied/adapted from packages/SystemUI/src/com/android/systemui/statusbar/window/StatusBarWindowController.java
    private static int getStatusBarHeightForRotation(Context context,
            @Surface.Rotation int targetRot) {
        final Display display = context.getDisplay();
        final DisplayCutout cutout = display.getCutout();
        DisplayInfo info = new DisplayInfo();
        display.getDisplayInfo(info);
        Insets insets;
        Insets waterfallInsets;
        if (cutout == null) {
            insets = Insets.NONE;
            waterfallInsets = Insets.NONE;
        } else {
            DisplayCutout rotated =
                    cutout.getRotated(info.logicalWidth, info.logicalHeight, ROTATION_0, targetRot);
            insets = Insets.of(rotated.getSafeInsets());
            waterfallInsets = rotated.getWaterfallInsets();
        }
        final int defaultSize =
                context.getResources().getDimensionPixelSize(R.dimen.status_bar_height_default);
        // The status bar height should be:
        // Max(top cutout size, (status bar default height + waterfall top size))
        return Math.max(insets.top, defaultSize + waterfallInsets.top);
    }

    private static WindowManager.LayoutParams createWindowParams(int height) {
        WindowManager.LayoutParams lp =
                new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, height,
                        WindowManager.LayoutParams.TYPE_STATUS_BAR,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_SPLIT_TOUCH |
                                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                        PixelFormat.TRANSLUCENT);
        lp.privateFlags |= PRIVATE_FLAG_COLOR_SPACE_AGNOSTIC;
        lp.gravity = Gravity.TOP;
        lp.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        return lp;
    }

    private static Insets getInsets(int height) {
        return Insets.of(0, height, 0, 0);
    }

    // ----------------------------------------------------------------------------------------
    // All the methods that follow deal with taking care of the cutout when laying
    // out the Status Bar.
    // Copied/adapted from
    // packages/SystemUI/src/com/android/systemui/statusbar/phone/PhoneStatusBarView.java
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final Display display = getDisplay();
        DisplayInfo info = new DisplayInfo();
        display.getDisplayInfo(info);
        mDisplayCutout = info.displayCutout;
        if (mDisplayCutout != null) {
            updateStatusBarHeight();
            updateSafeInsets();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDisplayCutout = null;
    }

    private void updateStatusBarHeight() {
        final int waterfallTopInset =
                mDisplayCutout == null ? 0 : mDisplayCutout.getWaterfallInsets().top;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        mStatusBarHeight = getStatusBarHeightForRotation(mContext, mContext.getDisplay().getRotation());
        layoutParams.height = mStatusBarHeight - waterfallTopInset;
        setLayoutParams(layoutParams);
    }

    private void updateSafeInsets() {
        Insets insets = getStatusBarContentInsets();
        setPadding(
                insets.left,
                insets.top,
                insets.right,
                getPaddingBottom());
    }

    private Insets getStatusBarContentInsets() {
        Rect screenBounds =
                getContext().getResources().getConfiguration().windowConfiguration.getMaxBounds();
        int width = screenBounds.width();
        List<Rect> cutoutRects = Stream.of(mDisplayCutout.getBoundingRectLeft(),
                mDisplayCutout.getBoundingRectRight(),
                mDisplayCutout.getBoundingRectTop()).filter(rect -> !rect.isEmpty()).toList();
        if (cutoutRects.isEmpty()) {
            return Insets.NONE;
        }

        int leftMargin = 0;
        int rightMargin = 0;
        Rect sbRect = new Rect(0, 0, width, mStatusBarHeight);
        for (Rect cutoutRect : cutoutRects) {
            if (!sbRect.intersects(0, cutoutRect.top, width, cutoutRect.bottom)) {
                continue;
            }
            if (cutoutRect.left == 0) {
                leftMargin = Math.max(leftMargin, cutoutRect.width());
            } else if (cutoutRect.right == width) {
                rightMargin = Math.max(rightMargin, cutoutRect.width());
            }
        }
        return Insets.of(leftMargin, 0, rightMargin, 0);
    }
}
